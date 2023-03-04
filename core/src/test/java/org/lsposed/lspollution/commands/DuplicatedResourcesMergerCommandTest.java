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
 * Created by YangJing on 2019/10/11 .
 * Email: yangjing.yeoh@bytedance.com
 */
public class DuplicatedResourcesMergerCommandTest extends BaseTest {

    @Test
    public void test_noFlag() {
        Flag.RequiredFlagNotSetException flagsException = assertThrows(Flag.RequiredFlagNotSetException.class,
                () -> DuplicatedResourcesMergerCommand.fromFlags(
                        new FlagParser().parse(
                                ""
                        )
                ).execute());
        assertEquals("Missing the required --bundle flag.", flagsException.getMessage());
    }

    @Test
    public void test_no_bundle() {
        Flag.RequiredFlagNotSetException flagsException = assertThrows(Flag.RequiredFlagNotSetException.class,
                () -> DuplicatedResourcesMergerCommand.fromFlags(
                        new FlagParser().parse(
                                "--output=" + getTempDirFilePath()
                        )
                ).execute());
        assertEquals("Missing the required --bundle flag.", flagsException.getMessage());
    }

    @Test
    public void test_no_Output() {
        Flag.RequiredFlagNotSetException flagsException = assertThrows(Flag.RequiredFlagNotSetException.class,
                () -> DuplicatedResourcesMergerCommand.fromFlags(
                        new FlagParser().parse(
                                "--bundle=" + loadResourcePath("demo/demo.aab")
                        )
                ).execute());
        assertEquals("Missing the required --output flag.", flagsException.getMessage());
    }

    @Test
    public void test_notExists_bundle() {
        String tempPath = getTempDirFilePath();
        File apkFile = new File(tempPath + "abc.apk");
        IllegalArgumentException flagsException = assertThrows(IllegalArgumentException.class,
                () -> DuplicatedResourcesMergerCommand.fromFlags(
                        new FlagParser().parse(
                                "--bundle=" + apkFile.getAbsolutePath(),
                                "--output=" + getTempDirFilePath()
                        )
                ).execute());
        assertEquals(String.format("File '%s' was not found.", apkFile.getAbsolutePath()), flagsException.getMessage());
    }

    @Test
    public void test_wrong_params() {
        Flag.RequiredFlagNotSetException flagsException = assertThrows(Flag.RequiredFlagNotSetException.class,
                () -> DuplicatedResourcesMergerCommand.fromFlags(
                        new FlagParser().parse(
                                "--abc=a"
                        )
                ).execute());
        assertEquals("Missing the required --bundle flag.", flagsException.getMessage());
    }

    @Test
    public void test_disableSign() throws IOException, InterruptedException {
        File rawAabFile = loadResourceFile("demo/demo.aab");
        File outputFile = new File(getTempDirPath().toFile(), "duplicated.aab");
        DuplicatedResourcesMergerCommand.fromFlags(
                new FlagParser().parse(
                        "--bundle=" + rawAabFile.getAbsolutePath(),
                        "--output=" + outputFile.getAbsolutePath(),
                        "--disable-sign=true"
                )
        ).execute();
        assert outputFile.exists();
    }

    @Test
    public void testMergeDuplicatedRes() throws IOException, InterruptedException {
        File rawAabFile = loadResourceFile("demo/demo.aab");
        File outputFile = new File(getTempDirPath().toFile(), "duplicated.aab");
        DuplicatedResourcesMergerCommand.fromFlags(
                new FlagParser().parse(
                        "--bundle=" + rawAabFile.getAbsolutePath(),
                        "--output=" + outputFile.getAbsolutePath()
                )
        ).execute();
        assert outputFile.exists();
        assert FileOperation.getFileSizes(rawAabFile) > FileOperation.getFileSizes(outputFile);
    }
}
