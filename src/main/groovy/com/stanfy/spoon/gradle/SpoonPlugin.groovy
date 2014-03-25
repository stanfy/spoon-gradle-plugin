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

    if (!project.plugins.withType(AppPlugin)) {
      throw new IllegalStateException("Android plugin is not found")
    }

    project.extensions.add "spoon", SpoonExtension

    def spoonTask = project.task("spoon") {
      group = JavaBasePlugin.VERIFICATION_GROUP
      description = "Runs all the instrumentation test variations on all the connected devices"
    }

    AppExtension android = project.android
    android.testVariants.all { TestVariant variant ->

      String taskName = "${TASK_PREFIX}${variant.name.capitalize()}"

      SpoonRunTask task = project.tasks.create(taskName, SpoonRunTask)
      SpoonExtension config = project.spoon
      task.configure {
        group = JavaBasePlugin.VERIFICATION_GROUP
        description = "Runs instrumentation tests on all the connected devices for '${variant.name}' variation and generates a report with screenshots"
        applicationApk = variant.testedVariant.outputFile
        instrumentationApk = variant.outputFile
        title = "$project.name $variant.name"

        File outputBase = config.baseOutputDir
        if (!outputBase) {
          outputBase = new File(project.buildDir, "spoon")
        }
        output = new File(outputBase, variant.testedVariant.dirName)

        debug = config.debug
        ignoreFailures = config.ignoreFailures
        devices = config.devices
        allDevices = !config.devices
        noAnimations = config.noAnimations

        if (project.spoon.className) {
          className = project.spoon.className
          if (project.spoon.methodName) {
            methodName = project.spoon.methodName
          }          
        }

        /** If command line command contains testSize parameter then it is used
         * to decide what kind of test set to run based on it's size - small, medium or large */
        String testSizeParameter = project.spoon.testSize
        if (testSizeParameter){
          testSizeParameter = testSizeParameter.toLowerCase()
          testSize = testSizeParameter
        } else {
          testSize = "all"
        }

        dependsOn variant.assemble, variant.testedVariant.assemble
      }

      spoonTask.dependsOn task
    }

  }

}
