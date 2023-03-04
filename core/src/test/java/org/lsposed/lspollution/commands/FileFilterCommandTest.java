package org.lsposed.lspollution.commands;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import com.android.tools.build.bundletool.flags.Flag;
import com.android.tools.build.bundletool.flags.FlagParser;
import org.lsposed.lspollution.BaseTest;

import org.junit.Test;
import org.lsposed.lspollution.utils.FileOperation;

import java.io.File;
import java.io.IOException;

/**
 * Created by YangJing on 2019/10/14 .
 * Email: yangjing.yeoh@bytedance.com
 */
public class FileFilterCommandTest extends BaseTest {
    @Test
    public void test_noFlag() {
        Flag.RequiredFlagNotSetException flagsException = assertThrows(Flag.RequiredFlagNotSetException.class,
                () -> FileFilterCommand.fromFlags(
                        new FlagParser().parse(
                                ""
                        )
                ).execute());
        assertEquals("Missing the required --bundle flag.", flagsException.getMessage());
    }

    @Test
    public void test_no_bundle() {
        Flag.RequiredFlagNotSetException flagsException = assertThrows(Flag.RequiredFlagNotSetException.class,
                () -> FileFilterCommand.fromFlags(
                        new FlagParser().parse(
                                "--output=" + getTempDirFilePath()
                        )
                ).execute());
        assertEquals("Missing the required --bundle flag.", flagsException.getMessage());
    }

    @Test
    public void test_no_Config() {
        Flag.RequiredFlagNotSetException flagsException = assertThrows(Flag.RequiredFlagNotSetException.class,
                () -> FileFilterCommand.fromFlags(
                        new FlagParser().parse(
                                "--bundle=" + loadResourcePath("demo/demo.aab")
                        )
                ).execute());
        assertEquals("Missing the required --config flag.", flagsException.getMessage());
    }


    @Test
    public void test_wrong_params() {
        Flag.RequiredFlagNotSetException flagsException = assertThrows(Flag.RequiredFlagNotSetException.class,
                () -> FileFilterCommand.fromFlags(
                        new FlagParser().parse(
                                "--abc=a"
                        )
                ).execute());
        assertEquals("Missing the required --bundle flag.", flagsException.getMessage());
    }

    @Test
    public void test_disableSign() throws IOException, InterruptedException {
        File rawAabFile = loadResourceFile("demo/demo.aab");
        File outputFile = new File(getTempDirPath().toFile(), "filtered.aab");
        FileFilterCommand.fromFlags(
                new FlagParser().parse(
                        "--bundle=" + rawAabFile.getAbsolutePath(),
                        "--output=" + outputFile.getAbsolutePath(),
                        "--config="+loadResourcePath("demo/config-filter.xml"),
                        "--disable-sign=true"
                )
        ).execute();
        assert outputFile.exists();
    }

    @Test
    public void testPass() throws IOException, InterruptedException {
        File rawAabFile = loadResourceFile("demo/demo.aab");
        File outputFile = new File(getTempDirPath().toFile(), "filtered.aab");
        FileFilterCommand.fromFlags(
                new FlagParser().parse(
                        "--bundle=" + rawAabFile.getAbsolutePath(),
                        "--output=" + outputFile.getAbsolutePath(),
                        "--config="+loadResourcePath("demo/config-filter.xml")
                )
        ).execute();
        assert outputFile.exists();
        assert FileOperation.getFileSizes(rawAabFile) > FileOperation.getFileSizes(outputFile);
    }
}
