package com.stanfy.spoon.gradle

import com.android.build.gradle.AppPlugin
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.LibraryPlugin
import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.internal.api.TestedVariant
import com.android.build.gradle.internal.variant.ApkVariantOutputData
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.internal.DefaultDomainObjectSet
import org.gradle.api.plugins.JavaBasePlugin

import java.lang.reflect.Method

/**
 * Gradle plugin for Spoon.
 */
class SpoonPlugin implements Plugin<Project> {

  /** Task name prefix. */
  private static final String TASK_PREFIX = "spoon"
  /** Assemble task holder from other variants. */
  private static Task holder;
  @Override
  void apply(final Project project) {

    if (!project.plugins.findPlugin(AppPlugin) && !project.plugins.findPlugin(LibraryPlugin)) {
      throw new IllegalStateException("Android plugin is not found")
    }

    project.extensions.add "spoon", SpoonExtension

    def spoonTask = project.task(TASK_PREFIX) {
      group = JavaBasePlugin.VERIFICATION_GROUP
      description = "Runs all the instrumentation test variations on all the connected devices"
    }

    BaseExtension android = project.android

    DefaultDomainObjectSet variantSet

    if(project.plugins.findPlugin(AppPlugin))
      variantSet = android.applicationVariants
    if(project.plugins.findPlugin(LibraryPlugin))
      variantSet = android.libraryVariants



    variantSet.all { TestedVariant variant ->
      holder = variant.assemble
      if(variant.testVariant != null) {

        String taskName = "${TASK_PREFIX}${variant.name.capitalize()}"
        List<SpoonRunTask> tasks = createTask(variant, project, "")
        tasks.each {
          it.configure {
            title = "$project.name $variant.name"
            description = "Runs instrumentation tests on all the connected devices for '${variant.name}' variation and generates a report with screenshots"
          }
        }

        spoonTask.dependsOn tasks

        project.tasks.addRule(patternString(taskName)) { String ruleTaskName ->
          if (ruleTaskName.startsWith(taskName)) {
            String size = (ruleTaskName - taskName).toLowerCase(Locale.US)
            if (isValidSize(size)) {
              List<SpoonRunTask> sizeTasks = createTask(variant, project, size.capitalize())
              sizeTasks.each {
                it.configure {
                  title = "$project.name $variant.name - $size tests"
                  testSize = size
                }
              }
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
          project.task(ruleTaskName, dependsOn: variantTaskNames.collect() { "${it}${size.capitalize()}" })
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

  private static List<SpoonRunTask> createTask(final TestedVariant testedVariant, final Project project, final String suffix) {
    if (testedVariant.testVariant.outputs.size() > 1) {
      throw new UnsupportedOperationException("Spoon plugin for gradle currently does not support abi/density splits for test apks")
    }

    SpoonExtension config = project.spoon
    return testedVariant.testVariant.outputs.collect { def projectOutput ->
      SpoonRunTask task = project.tasks.create("${TASK_PREFIX}${testedVariant.testVariant.name.capitalize()}${suffix}", SpoonRunTask)
      task.configure {
        group = JavaBasePlugin.VERIFICATION_GROUP

        def instrumentationPackage = testedVariant.testVariant.outputs[0].outputFile

        if (testedVariant instanceof BaseVariant) {
          applicationApk = testedVariant.outputs[0].outputFile
        }  else {
          // This is a hack for library projects.
          // We supply the same apk as an application and instrumentation to the spoon runner.
          applicationApk = instrumentationPackage
        }
        instrumentationApk = instrumentationPackage

        File outputBase = config.baseOutputDir
        if (!outputBase) {
          outputBase = new File(project.buildDir, "spoon")
        }
        output = new File(outputBase, projectOutput.dirName)

        debug = config.debug
        ignoreFailures = config.ignoreFailures
        devices = config.devices
        allDevices = !config.devices
        noAnimations = config.noAnimations
        failIfNoDeviceConnected = config.failIfNoDeviceConnected
        codeCoverage = config.codeCoverage
        shard = config.shard
        if (config.adbTimeout != -1) {
          // Timeout is defined in seconds in the config.
          adbTimeout = config.adbTimeout * 1000
        }

        testSize = SpoonRunTask.TEST_SIZE_ALL

        if (config.className) {
          className = config.className
          if (config.methodName) {
            methodName = config.methodName
          }
        }
        if (config.instrumentationArgs) {
            instrumentationArgs = config.instrumentationArgs
        }

        if (config.numShards > 0) {
          if (config.shardIndex >= config.numShards) {
            throw new UnsupportedOperationException("shardIndex needs to be < numShards");
          }
          numShards = config.numShards
          shardIndex = config.shardIndex
        }
        //using reflection to get the variant output data, maybe there is a better way?
        Class c = projectOutput.getClass()
        Method method = c.getDeclaredMethod("getVariantOutputData")
        final ApkVariantOutputData data = method.invoke(projectOutput)
        //replacing the null assemble task using android gradle plugin 2.2-alpha2
        data.assembleTask = holder


        if (!testedVariant.testVariant.assemble) {
          throw new IllegalStateException("Assemble task is not provided for test variant $testedVariant.name")
        }
        if (!projectOutput.assemble) {
          throw new IllegalStateException("Assemble task is not provided for tested variant $testedVariant.testVariant.name")
        }

        dependsOn projectOutput.assemble, testedVariant.testVariant.assemble
      }
      task.outputs.upToDateWhen { false }
      return task
    } as List<SpoonRunTask>
  }

}
