package org.lsposed.lspollution.executors;

import static com.android.tools.build.bundletool.model.AppBundle.METADATA_DIRECTORY;
import static com.android.tools.build.bundletool.model.utils.files.FilePreconditions.checkFileExistsAndReadable;
import static org.lsposed.lspollution.utils.FileOperation.getNetFileSizeDescription;

import com.android.bundle.Files;
import com.android.tools.build.bundletool.model.AppBundle;
import com.android.tools.build.bundletool.model.BundleMetadata;
import com.android.tools.build.bundletool.model.BundleModule;
import com.android.tools.build.bundletool.model.BundleModuleName;
import com.android.tools.build.bundletool.model.ModuleEntry;
import com.android.tools.build.bundletool.model.ZipPath;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import org.lsposed.lspollution.bundle.AppBundleUtils;
import org.lsposed.lspollution.bundle.NativeLibrariesOperation;
import org.lsposed.lspollution.utils.TimeClock;
import org.lsposed.lspollution.utils.Utils;

import java.io.IOException;
import java.nio.file.Path;
import java.rmi.UnexpectedException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipFile;

/**
 * Created by YangJing on 2019/10/12 .
 * Email: yangjing.yeoh@bytedance.com
 */
public class BundleFileFilter {

    private static final Set<String> FILE_SIGN = new HashSet<>(
            ImmutableSet.of(
                    "META-INF/*.RSA",
                    "META-INF/*.SF",
                    "META-INF/*.MF"
            )
    );
    private final ZipFile bundleZipFile;
    private final AppBundle rawAppBundle;
    private final Set<String> filterRules;

    private int filterTotalSize = 0;
    private int filterTotalCount = 0;

    public BundleFileFilter(Path bundlePath, AppBundle rawAppBundle, Set<String> filterRules) throws IOException {
        checkFileExistsAndReadable(bundlePath);
        this.bundleZipFile = new ZipFile(bundlePath.toFile());
        this.rawAppBundle = rawAppBundle;
        if (filterRules == null) {
            filterRules = new HashSet<>();
        }
        this.filterRules = filterRules;

        filterRules.addAll(FILE_SIGN);
    }

    public AppBundle filter() throws IOException {
        TimeClock timeClock = new TimeClock();

        // filter bundle module file
        Map<BundleModuleName, BundleModule> bundleModules = new HashMap<>();
        for (Map.Entry<BundleModuleName, BundleModule> entry : rawAppBundle.getModules().entrySet()) {
            bundleModules.put(entry.getKey(), filterBundleModule(entry.getValue()));
        }
        AppBundle appBundle = rawAppBundle.toBuilder()
                .setBundleMetadata(filterMetaData())
                .setModules(ImmutableMap.copyOf(bundleModules))
                .build();
        System.out.printf("""
                        filter bundle files done, coast %s
                        -----------------------------------------
                        Reduce file count: %s
                        Reduce file size: %s
                        -----------------------------------------%n""",
                timeClock.getCoast(),
                filterTotalCount,
                getNetFileSizeDescription(filterTotalSize)
        );
        return appBundle;
    }

    private BundleModule filterBundleModule(BundleModule bundleModule) throws IOException {
        BundleModule.Builder builder = bundleModule.toBuilder();
        List<ModuleEntry> filteredModuleEntries = new ArrayList<>();
        List<ModuleEntry> entries = bundleModule.getEntries().stream()
                .filter(entry -> {
                    String filterRule = getMatchedFilterRule(entry.getPath());
                    if (filterRule != null) {
                        checkFilteredEntry(entry, filterRule);
                        System.out.printf("[filter] metadata file is filtered, path: %s%n", entry.getPath());
                        filteredModuleEntries.add(entry);
                        filterTotalSize += AppBundleUtils.getZipEntrySize(bundleZipFile, entry, bundleModule);
                        return false;
                    }
                    return true;
                })
                .collect(Collectors.toList());
        builder.setRawEntries(entries);
        filterTotalCount += filteredModuleEntries.size();
        // update pb
        Files.NativeLibraries nativeLibraries = updateLibDirectory(bundleModule, filteredModuleEntries);
        if (nativeLibraries != null) {
            builder.setNativeConfig(nativeLibraries);
        }
        return builder.build();
    }

    private Files.NativeLibraries updateLibDirectory(BundleModule bundleModule, List<ModuleEntry> entries) throws UnexpectedException {
        List<ModuleEntry> libEntries = entries.stream()
                .filter(entry -> entry.getPath().startsWith(BundleModule.LIB_DIRECTORY))
                .toList();
        Files.NativeLibraries nativeLibraries = bundleModule.getNativeConfig().orElse(null);
        if (libEntries.isEmpty()) {
            return nativeLibraries;
        }
        if (nativeLibraries == null) {
            throw new UnexpectedException(String.format("can not find nativeLibraries file `native.pb` in %s module", bundleModule.getName().getName()));
        }

        Files.NativeLibraries filteredNativeLibraries = nativeLibraries;
        for (Files.TargetedNativeDirectory directory : nativeLibraries.getDirectoryList()) {
            int directoryNativeSize = libEntries.stream()
                    .filter(entry -> entry.getPath().startsWith(directory.getPath()))
                    .toList().size();
            if (directoryNativeSize > 0) {
                int moduleNativeSize = bundleModule.getEntries().stream()
                        .filter(entry -> entry.getPath().startsWith(directory.getPath()))
                        .toList().size();
                if (directoryNativeSize == moduleNativeSize) {
                    filteredNativeLibraries = NativeLibrariesOperation.removeDirectory(filteredNativeLibraries, directory.getPath());
                }
            }
        }
        return filteredNativeLibraries;
    }

    /**
     * Filter meta data dir and return filtered list.
     */
    private BundleMetadata filterMetaData() {
        BundleMetadata.Builder builder = BundleMetadata.builder();
        Stream.of(rawAppBundle.getBundleMetadata())
                .map(BundleMetadata::getFileContentMap)
                .map(ImmutableMap::entrySet)
                .flatMap(Collection::stream)
                .filter(entry -> {
                    ZipPath entryZipPath = ZipPath.create(AppBundle.METADATA_DIRECTORY + "/" + entry.getKey());
                    if (getMatchedFilterRule(entryZipPath) != null) {
                        System.out.printf("[filter] metadata file is filtered, path: %s%n", entryZipPath);
                        filterTotalCount += 1;
                        filterTotalSize += AppBundleUtils.getZipEntrySize(bundleZipFile, entryZipPath);
                        return false;
                    }
                    return true;
                })
                .forEach(entry -> builder.addFile(entry.getKey(), entry.getValue()));
        return builder.build();
    }

    private void checkFilteredEntry(ModuleEntry entry, String filterRule) {
        if (!entry.getPath().startsWith(BundleModule.LIB_DIRECTORY) &&
                !entry.getPath().startsWith(METADATA_DIRECTORY.toString())) {
            throw new UnsupportedOperationException(String.format("%s entry can not be filtered, please check the filter rule [%s].", entry.getPath(), filterRule));
        }
    }

    private String getMatchedFilterRule(ZipPath zipPath) {
        for (String rule : filterRules) {
            Pattern filterPattern = Pattern.compile(Utils.convertToPatternString(rule));
            if (filterPattern.matcher(zipPath.toString()).matches()) {
                return rule;
            }
        }
        return null;
    }
}
