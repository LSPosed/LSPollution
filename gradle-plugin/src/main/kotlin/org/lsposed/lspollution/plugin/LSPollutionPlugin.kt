package org.lsposed.lspollution.plugin

import com.android.build.gradle.AppExtension
import com.android.build.gradle.api.ApplicationVariant
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.configurationcache.extensions.capitalized

/**
 * Created by YangJing on 2019/10/15 .
 * Email: yangjing.yeoh@bytedance.com
 */
class LSPollutionPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        checkApplicationPlugin(project)
        project.extensions.create("lspollution", LSPollutionExtension::class.java)

        val android = project.extensions.getByName("android") as AppExtension
        project.afterEvaluate {
            android.applicationVariants.all { variant ->
                createLSPollutionTask(project, variant)
            }
        }
    }

    private fun createLSPollutionTask(project: Project, variant: ApplicationVariant) {
        val variantName = variant.name.capitalized()
        val bundleTaskName = "bundle$variantName"
        if (project.tasks.findByName(bundleTaskName) == null) {
            return
        }
        val lspollutionTaskName = "lspollution$variantName"
        val lspollutionTask = if (project.tasks.findByName(lspollutionTaskName) == null) {
            project.tasks.create(lspollutionTaskName, LSPollutionTask::class.java)
        } else {
            project.tasks.getByName(lspollutionTaskName) as LSPollutionTask
        }
        lspollutionTask.setVariantScope(variant)

        val bundleTask: Task = project.tasks.getByName(bundleTaskName)
        val bundlePackageTask: Task = project.tasks.getByName("package${variantName}Bundle")
        bundleTask.dependsOn(lspollutionTask)
        lspollutionTask.dependsOn(bundlePackageTask)
        // AGP-4.0.0-alpha07: use FinalizeBundleTask to sign bundle file
        // FinalizeBundleTask is executed after PackageBundleTask
        val finalizeBundleTaskName = "sign${variantName}Bundle"
        if (project.tasks.findByName(finalizeBundleTaskName) != null) {
            lspollutionTask.dependsOn(project.tasks.getByName(finalizeBundleTaskName))
        }
    }

    private fun checkApplicationPlugin(project: Project) {
        if (!project.plugins.hasPlugin("com.android.application")) {
            throw  GradleException("Android Application plugin required")
        }
    }
}
