package org.lsposed.lspollution.android

import com.android.tools.build.bundletool.model.utils.files.FilePreconditions
import org.lsposed.lspollution.utils.FileUtils
import java.io.File
import java.io.IOException
import java.util.logging.Logger

/**
 * Created by YangJing on 2019/10/18 .
 * Email: yangjing.yeoh@bytedance.com
 */
class OpenJDKJarSigner {
    @Throws(IOException::class, InterruptedException::class)
    fun sign(toBeSigned: File, signature: JarSigner.Signature) {
        FilePreconditions.checkFileExistsAndReadable(toBeSigned.toPath())
        FilePreconditions.checkFileExistsAndReadable(signature.storeFile)
        val jarSigner = locatedJarSigner()
        val args: MutableList<String> = ArrayList()
        if (jarSigner != null) {
            args.add(jarSigner.absolutePath)
        } else {
            args.add(jarSignerExecutable)
        }
        args.add("-keystore")
        args.add(signature.storeFile.toFile().absolutePath)
        var keyStorePasswordFile: File? = null
        var aliasPasswordFile: File? = null

        // write passwords to a file so it cannot be spied on.
        if (signature.storePassword != null) {
            keyStorePasswordFile = File.createTempFile("store", "prv")
            FileUtils.writeToFile(keyStorePasswordFile, signature.storePassword)
            args.add("-storepass:file")
            args.add(keyStorePasswordFile.absolutePath)
        }
        if (signature.keyPassword != null) {
            aliasPasswordFile = File.createTempFile("alias", "prv")
            FileUtils.writeToFile(aliasPasswordFile, signature.keyPassword)
            args.add("--keypass:file")
            args.add(aliasPasswordFile.absolutePath)
        }
        args.add(toBeSigned.absolutePath)
        if (signature.keyAlias != null) {
            args.add(signature.keyAlias)
        }
        val errorLog = File.createTempFile("error", ".log")
        val outputLog = File.createTempFile("output", ".log")
        logger.fine("Invoking " + args.joinToString(" "))
        val process = start(ProcessBuilder(args).redirectError(errorLog).redirectOutput(outputLog))
        val exitCode = process.waitFor()
        if (exitCode != 0) {
            val errors = FileUtils.loadFileWithUnixLineSeparators(errorLog)
            val output = FileUtils.loadFileWithUnixLineSeparators(outputLog)
            throw RuntimeException(
                String.format(
                    "%s failed with exit code %d: \n %s",
                    jarSignerExecutable, exitCode,
                    if (errors.trim { it <= ' ' }.isEmpty()) output else errors
                )
            )
        }
        keyStorePasswordFile?.delete()
        aliasPasswordFile?.delete()
    }

    @Throws(IOException::class)
    private fun start(builder: ProcessBuilder): Process {
        return builder.start()
    }

    /**
     * Return the "jarsigner" tool location or null if it cannot be determined.
     */
    private fun locatedJarSigner(): File? {
        // Look in the java.home bin folder, on jdk installations or Mac OS X, this is where the
        // javasigner will be located.
        val javaHome = File(System.getProperty("java.home"))
        var jarSigner = getJarSigner(javaHome)
        return if (jarSigner.exists()) {
            jarSigner
        } else {
            // if not in java.home bin, it's probable that the java.home points to a JRE
            // installation, we should then look one folder up and in the bin folder.
            jarSigner = getJarSigner(javaHome.parentFile)
            // if still cant' find it, give up.
            if (jarSigner.exists()) jarSigner else null
        }
    }

    /**
     * Returns the jarsigner tool location with the bin folder.
     */
    private fun getJarSigner(parentDir: File): File {
        return File(File(parentDir, "bin"), jarSignerExecutable)
    }

    companion object {
        private val jarSignerExecutable = if (SdkConstants.currentPlatform() == SdkConstants.PLATFORM_WINDOWS) "jarsigner.exe" else "jarsigner"
        private val logger = Logger.getLogger(OpenJDKJarSigner::class.java.name)
    }
}
