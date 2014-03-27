package com.stanfy.spoon.gradle

import com.android.build.gradle.AppExtension
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.api.TestVariant
import org.gradle.api.Plugin
import org.gradle.api.Project
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
      SpoonRunTask task = createTask(taskName, variant, project)

      task.configure {
        title = "$project.name $variant.name"
        description = "Runs instrumentation tests on all the connected devices for '${variant.name}' variation and generates a report with screenshots"
      }

      spoonTask.dependsOn task

      project.tasks.addRule(patternString(taskName)) { String ruleTaskName ->
        if (ruleTaskName.startsWith(taskName)) {
          String size = (ruleTaskName - taskName).toLowerCase(Locale.US)
          if (isValidSize(size)) {
            SpoonRunTask sizeTask = createTask(ruleTaskName, variant, project)
            sizeTask.configure {
              title = "$project.name $variant.name - $size tests"
              testSize = size
            }
          }
        }
      }
    }

    project.tasks.addRule(patternString("spoon")) { String ruleTaskName ->
      if (ruleTaskName.startsWith("spoon")) {
        String suffix = lowercase(ruleTaskName - "spoon")
        if (android.testVariants.find { suffix.startsWith(it.name) } != null) {
          // variant specific, not our case
          return
        }
        String size = suffix.toLowerCase(Locale.US)
        if (isValidSize(size)) {
          def variantTaskNames = spoonTask.taskDependencies.getDependencies(spoonTask).collect() { it.name }
          project.task(ruleTaskName, dependsOn: variantTaskNames.collect() { "${it}${size}" })
        }
      }
    }
  }

  private static boolean isValidSize(String size) {
    return size in ['small', 'medium', 'large']
  }

  private static String lowercase(final String s) {
    return s[0].toLowerCase(Locale.US) + s.substring(1)
  }

  private static String patternString(final String taskName) {
    return "Pattern: $taskName<TestSize>: run instrumentation tests of particular size"
  }

  private static SpoonRunTask createTask(final String name, final TestVariant variant, final Project project) {
    SpoonExtension config = project.spoon
    SpoonRunTask task = project.tasks.create(name, SpoonRunTask)

    task.configure {
      group = JavaBasePlugin.VERIFICATION_GROUP
      applicationApk = variant.testedVariant.outputFile
      instrumentationApk = variant.outputFile

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
      failIfNoDeviceConnected = project.spoon.failIfNoDeviceConnected

      testSize = SpoonRunTask.TEST_SIZE_ALL

      if (project.spoon.className) {
        className = project.spoon.className
        if (project.spoon.methodName) {
          methodName = project.spoon.methodName
        }
      }

      dependsOn variant.assemble, variant.testedVariant.assemble
    }
  }

}
