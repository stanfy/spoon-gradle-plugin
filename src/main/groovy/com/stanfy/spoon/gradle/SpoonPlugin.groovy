package com.stanfy.spoon.gradle

import com.android.build.gradle.AppExtension
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.api.TestVariant
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.logging.LogLevel
import org.gradle.api.plugins.BasePlugin
import org.gradle.api.plugins.JavaBasePlugin

/**
 * Gradle plugin for Spoon.
 */
class SpoonPlugin implements Plugin<Project> {

  /** Task name prefix. */
  private static final String TASK_PREFIX = "spoon"

  @Override
  void apply(final Project project) {

    if (!project.plugins.hasPlugin(AppPlugin)) {
      throw new IllegalStateException("Android plugin is not found")
    }

    project.extensions.add "spoon", SpoonExtension

    AppExtension android = project.android
    android.testVariants.all { TestVariant variant ->

      String taskName = "${TASK_PREFIX}${variant.name}"

      SpoonRunTask task = project.tasks.create(taskName, SpoonRunTask)
      task.group = JavaBasePlugin.VERIFICATION_GROUP
      task.description = "Runs instrumentation tests on all the connected devices for '${variant.name}' variation and generates a report with screenshots"
      task.applicationApk = variant.testedVariant.outputFile
      task.instrumentationApk = variant.outputFile
      task.title = "$project.name $variant.name"
      task.output = new File(project.buildDir, "spoon/${variant.name}")
      task.debug = project.spoon.debug
      task.dependsOn variant.assemble, variant.testedVariant.assemble
    }

  }

}
