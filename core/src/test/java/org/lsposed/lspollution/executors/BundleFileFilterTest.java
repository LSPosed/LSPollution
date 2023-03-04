package org.lsposed.lspollution.executors;

import com.android.tools.build.bundletool.model.AppBundle;
import org.lsposed.lspollution.BaseTest;
import org.lsposed.lspollution.bundle.AppBundleAnalyzer;
import com.google.common.collect.ImmutableSet;

import org.junit.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashSet;

/**
 * Created by YangJing on 2019/10/14 .
 * Email: yangjing.yeoh@bytedance.com
 */
public class BundleFileFilterTest extends BaseTest {

    @Test
    public void test() throws IOException {
        Path bundlePath = loadResourceFile("demo/demo.aab").toPath();
        AppBundleAnalyzer analyzer = new AppBundleAnalyzer(bundlePath);
        AppBundle appBundle = analyzer.analyze();
        ImmutableSet<String> filterRules = ImmutableSet.of(
                "*/arm64-v8a/*"
        );
        BundleFileFilter fileFilter = new BundleFileFilter(loadResourceFile("demo/demo.aab").toPath(), appBundle, new HashSet<>(filterRules));
        AppBundle filteredAppBundle = fileFilter.filter();
        assert filteredAppBundle != null;
    }
}
