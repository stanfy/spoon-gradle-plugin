package com.stanfy.spoon.gradle

import com.android.build.gradle.AppPlugin
import com.squareup.spoon.SpoonRunner
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.VerificationTask
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Task for using SpoonRunner.
 */
class SpoonRunTask extends DefaultTask implements VerificationTask {

  /** Plugin dependency name. */
  private static final String PLUGIN_DEP_NAME = "com.stanfy.spoon:spoon-gradle-plugin"
  /** Spoon runner artifact name. */
  private static final String SPOON_RUNNER_ARTIFACT = "spoon-runner"
  /** Spoon dependency name. */
  private static final String SPOON_DEP_NAME = "com.squareup.spoon:$SPOON_RUNNER_ARTIFACT"

  /** Logger. */
  private static final Logger LOG = LoggerFactory.getLogger(SpoonRunTask.class)

  /** A title for the output website. */
  String title

  /** If true then test failures do not cause a build failure. */
  boolean ignoreFailures

  /** Debug logging switcher. */
  boolean debug

  /** Name of the one test to run. */
  String className

  /** Name of the one test method to run. */
  String methodName

  /** Instrumentation APK. */
  @InputFile
  File instrumentationApk

  /** Application APK. */
  @InputFile
  File applicationApk

  /** Output directory. */
  @OutputDirectory
  File output

  @TaskAction
  void runSpoon() {
    LOG.info("Run instrumentation tests $instrumentationApk for app $applicationApk")

    LOG.debug("Title: $title")
    LOG.debug("Output: $output")

    LOG.debug("Ignore failures: $ignoreFailures")
    LOG.debug("Debug mode: $debug")

    if (className) {
      LOG.debug("Class name: $className")
      if (methodName) {
        LOG.debug("Method name: $methodName")
      }
    }

    String cp = getClasspath()
    LOG.debug("Classpath: $cp")

    boolean success = new SpoonRunner.Builder()
      .setTitle(title)
      .setApplicationApk(applicationApk)
      .setInstrumentationApk(instrumentationApk)
      .setOutputDirectory(output)
      .setDebug(debug)
      .setClassName(className)
      .setMethodName(methodName)
      .setAndroidSdk(project.plugins.findPlugin(AppPlugin).sdkDirectory)
      .setClasspath(cp)
      .useAllAttachedDevices()
      .build()
      .run()

    if (!success && !ignoreFailures) {
      throw new GradleException("Tests failed!")
    }
  }

  private String getClasspath() {
    def pluginDep = null
    def classpath = []
    project.buildscript.configurations.each {
      if (pluginDep) { return }

      pluginDep = it.resolvedConfiguration.firstLevelModuleDependencies.find { it.name.startsWith SpoonRunTask.PLUGIN_DEP_NAME }
      if (pluginDep) {
        def spoon = pluginDep.children.find { it.name.startsWith SpoonRunTask.SPOON_DEP_NAME }
        if (!spoon) { throw new IllegalStateException("Cannot find spoon-runner in dependencies") }
        classpath = spoon.allModuleArtifacts.collect { it.file }
        classpath += pluginDep.allModuleArtifacts.find { it.name == SpoonRunTask.SPOON_RUNNER_ARTIFACT }.file
      }

    }

    if (!pluginDep) { throw new IllegalStateException("Could not resolve spoon dependencies") }
    return project.files(classpath).asPath
  }

}
