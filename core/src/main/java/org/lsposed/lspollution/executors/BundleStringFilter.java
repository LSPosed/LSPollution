package org.lsposed.lspollution.executors;

import com.android.aapt.Resources;
import com.android.tools.build.bundletool.model.AppBundle;
import com.android.tools.build.bundletool.model.BundleModule;
import com.android.tools.build.bundletool.model.BundleModuleName;
import org.lsposed.lspollution.bundle.ResourcesTableBuilder;
import org.lsposed.lspollution.utils.TimeClock;
import com.google.common.collect.ImmutableMap;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.ZipFile;

import static com.android.tools.build.bundletool.model.utils.files.FilePreconditions.checkFileExistsAndReadable;

/**
 * Created by jiangzilai on 2019-10-20.
 */
public class BundleStringFilter {
    private final ZipFile bundleZipFile;
    private final AppBundle rawAppBundle;
    private final String unusedStrPath;
    private final Set<String> languageWhiteList;
    private final Set<String> unUsedNameSet = new HashSet<>(5000);

    private static final String replaceValue = "[value removed]";

    public BundleStringFilter(Path bundlePath, AppBundle rawAppBundle, String unusedStrPath, Set<String> languageWhiteList)
            throws IOException {
        checkFileExistsAndReadable(bundlePath);
        this.bundleZipFile = new ZipFile(bundlePath.toFile());
        this.rawAppBundle = rawAppBundle;
        this.unusedStrPath = unusedStrPath;
        this.languageWhiteList = languageWhiteList;
    }

    public AppBundle filter() throws IOException {
        TimeClock timeClock = new TimeClock();

        File unusedStrFile = new File(unusedStrPath);
        Map<BundleModuleName, BundleModule> obfuscatedModules = new HashMap<>();

        if (unusedStrFile.exists()) {
            //shrink结果
            unUsedNameSet.addAll(Files.readAllLines(Paths.get(unusedStrPath)));
            System.out.println("无用字符串 : " + unUsedNameSet.size());
        }

        if (!unUsedNameSet.isEmpty() || !languageWhiteList.isEmpty()) {
            for (Map.Entry<BundleModuleName, BundleModule> entry : rawAppBundle.getModules().entrySet()) {
                BundleModule bundleModule = entry.getValue();
                BundleModuleName bundleModuleName = entry.getKey();
                // obfuscate bundle module
                BundleModule obfuscatedModule = obfuscateBundleModule(bundleModule);
                obfuscatedModules.put(bundleModuleName, obfuscatedModule);
            }
        } else {
            return rawAppBundle;
        }

        AppBundle appBundle = rawAppBundle.toBuilder()
                .setModules(ImmutableMap.copyOf(obfuscatedModules))
                .build();

        System.out.printf("filtering strings done, coast %s\n%n", timeClock.getCoast());

        return appBundle;
    }

    private BundleModule obfuscateBundleModule(BundleModule bundleModule) {
        BundleModule.Builder builder = bundleModule.toBuilder();

        // obfuscate resourceTable
        Resources.ResourceTable obfuscatedResTable = obfuscateResourceTable(bundleModule);
        if (obfuscatedResTable != null) {
            builder.setResourceTable(obfuscatedResTable);
        }
        return builder.build();
    }

    private Resources.ResourceTable obfuscateResourceTable(BundleModule bundleModule) {
        if (bundleModule.getResourceTable().isEmpty()) {
            return null;
        }
        Resources.ResourceTable rawTable = bundleModule.getResourceTable().get();

        ResourcesTableBuilder tableBuilder = new ResourcesTableBuilder();
        List<Resources.Package> packageList = rawTable.getPackageList();


        if (packageList.isEmpty()) {
            return tableBuilder.build();
        }

        for (Resources.Package resPackage : packageList) {
            if (resPackage == null) {
                continue;
            }
            ResourcesTableBuilder.PackageBuilder packageBuilder = tableBuilder.addPackage(resPackage);
            List<Resources.Type> typeList = resPackage.getTypeList();
            Set<String> languageFilterSet = new HashSet<>(100);
            List<String> nameFilterList = new ArrayList<>(3000);
            for (Resources.Type resType : typeList) {
                if (resType == null) {
                    continue;
                }
                List<Resources.Entry> entryList = resType.getEntryList();
                for (Resources.Entry resEntry : entryList) {
                    if (resEntry == null) {
                        continue;
                    }

                    if (resPackage.getPackageId().getId() == 127 && resType.getName().equals("string") &&
                            languageWhiteList != null && !languageWhiteList.isEmpty()) {
                        //删除语言
                        List<Resources.ConfigValue> languageValue = resEntry.getConfigValueList().stream()
                                .filter(Objects::nonNull)
                                .filter(configValue -> {
                                    String locale = configValue.getConfig().getLocale();
                                    if (keepLanguage(locale)) {
                                        return true;
                                    }
                                    languageFilterSet.add(locale);
                                    return false;
                                }).collect(Collectors.toList());
                        resEntry = resEntry.toBuilder().clearConfigValue().addAllConfigValue(languageValue).build();
                    }

                    // 删除shrink扫描出的无用字符串
                    if (resPackage.getPackageId().getId() == 127 && resType.getName().equals("string")
                            && unUsedNameSet.size() > 0 && unUsedNameSet.contains(resEntry.getName())) {
                        List<Resources.ConfigValue> proguardConfigValue = resEntry.getConfigValueList().stream()
                                .filter(Objects::nonNull)
                                .map(configValue -> {
                                    Resources.ConfigValue.Builder rcb = configValue.toBuilder();
                                    Resources.Value.Builder rvb = rcb.getValueBuilder();
                                    Resources.Item.Builder rib = rvb.getItemBuilder();
                                    Resources.String.Builder rfb = rib.getStrBuilder();
                                    return rcb.setValue(
                                            rvb.setItem(rib.setStr(rfb.setValue(replaceValue).build()).build()).build()
                                    ).build();
                                }).collect(Collectors.toList());
                        nameFilterList.add(resEntry.getName());
                        resEntry = resEntry.toBuilder().clearConfigValue().addAllConfigValue(proguardConfigValue).build();
                    }
                    packageBuilder.addResource(resType, resEntry);
                }
            }
            System.out.println("filtering " + resPackage.getPackageName() + " id:" + resPackage.getPackageId().getId());
            StringBuilder l = new StringBuilder();
            for (String lan : languageFilterSet) {
                l.append("[remove language] : ").append(lan).append("\n");
            }
            System.out.println(l);
            l = new StringBuilder();
            for (String name : nameFilterList) {
                l.append("[delete name] ").append(name).append("\n");
            }
            System.out.println(l);
            System.out.println("-----------");
            packageBuilder.build();
        }
        return tableBuilder.build();
    }

    private boolean keepLanguage(String lan) {
        if (lan == null || lan.equals(" ") || lan.isEmpty()) {
            return true;
        }
        if (lan.contains("-")) {
            int index = lan.indexOf("-");
            if (index != -1) {
                String language = lan.substring(0, index);
                return languageWhiteList.contains(language);
            }
        } else {
            return languageWhiteList.contains(lan);
        }
        return false;
    }
}
