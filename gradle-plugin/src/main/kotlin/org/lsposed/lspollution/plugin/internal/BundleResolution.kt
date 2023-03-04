package org.lsposed.lspollution.plugin.internal

import com.android.build.gradle.api.ApplicationVariant
import org.gradle.api.Project
import org.gradle.configurationcache.extensions.capitalized
import java.io.File
import java.nio.file.Path

/**
 * Created by YangJing on 2020/01/07 .
 * Email: yangjing.yeoh@bytedance.com
 */
internal fun getBundleFilePath(project: Project, variant: ApplicationVariant): Path {
    val variantCapped = variant.name.capitalized()
    // use FinalizeBundleTask to sign bundle file
    val finalizeBundleTask = project.tasks.getByName("sign${variantCapped}Bundle")
    // FinalizeBundleTask.finalBundleFile is the final bundle path
    val bundleFile = finalizeBundleTask.property("finalBundleFile")
    val regularFile = bundleFile!!::class.java.getMethod("get").invoke(bundleFile)
    val path = regularFile::class.java.getMethod("getAsFile").invoke(regularFile) as File
    return path.toPath()
}
