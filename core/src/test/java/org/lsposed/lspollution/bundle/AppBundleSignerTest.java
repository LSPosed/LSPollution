package org.lsposed.lspollution.bundle;

import org.lsposed.lspollution.BaseTest;
import org.lsposed.lspollution.utils.FileOperation;

import org.junit.Test;

import java.io.File;
import java.io.IOException;

/**
 * Created by YangJing on 2019/10/11 .
 * Email: yangjing.yeoh@bytedance.com
 */
public class AppBundleSignerTest extends BaseTest {

    @Test
    public void testAppBundleSigner() throws IOException, InterruptedException {
        File output = new File(getTempDirPath().toFile(), "signed.aab");
        FileOperation.copyFileUsingStream(loadResourceFile("demo/demo.aab"), output);
        AppBundleSigner signer = new AppBundleSigner(output.toPath());
        signer.execute();
        assert output.exists();
    }
}
