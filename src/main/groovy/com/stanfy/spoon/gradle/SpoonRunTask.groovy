package com.stanfy.spoon.gradle

import com.android.ddmlib.testrunner.IRemoteAndroidTestRunner
import com.squareup.spoon.SpoonRunner
import groovy.transform.PackageScope
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Task for using SpoonRunner.
 */
class SpoonRunTask extends DefaultTask implements VerificationTask {

  /** All sizes should be run. */
  @PackageScope
  static final String TEST_SIZE_ALL = "all";

  /** Plugin dependency name. */
  private static final String PLUGIN_DEP_NAME = "com.stanfy.spoon:spoon-gradle-plugin"
  /** Spoon runner artifact name. */
  private static final String SPOON_RUNNER_ARTIFACT = "spoon-runner"
  /** Spoon dependency name. */
  private static final String SPOON_DEP_NAME = "com.squareup.spoon:$SPOON_RUNNER_ARTIFACT"

  /** Logger. */
  private static final Logger LOG = LoggerFactory.getLogger(SpoonRunTask.class)

  /** A title for the output website. */
  @Input
  String title

  /** If true then test failures do not cause a build failure. */
  boolean ignoreFailures

  /** If true, tests will fail if no devices are connected. */
  boolean failIfNoDeviceConnected

  /** Debug logging switcher. */
  boolean debug
  
  /** ADB timeout in ms. */
  int adbTimeout = -1

  /** Name of the one test to run. */
  String className

  /** Name of the one test method to run. */
  String methodName

  /** Whether or not animations are enabled */
  boolean noAnimations
  
  /** Size of test to be run ('small' / 'medium' / 'large'). */
  @Input
  String testSize

  /** Instrumentation APK. */
  @InputFile
  File instrumentationApk

  /** Application APK. */
  @InputFile
  File applicationApk

  /** Output directory. */
  @OutputDirectory
  File output

  /** Use all the connected devices flag. */
  boolean allDevices

  /** Devices to run on. */
  Set<String> devices

  @TaskAction
  void runSpoon() {
    LOG.info("Run instrumentation tests $instrumentationApk for app $applicationApk")

    LOG.debug("Title: $title")
    LOG.debug("Output: $output")

    LOG.debug("Ignore failures: $ignoreFailures")
    LOG.debug("Fail if no device connected: $failIfNoDeviceConnected")
    LOG.debug("Debug mode: $debug")

    if (className) {
      LOG.debug("Class name: $className")
      if (methodName) {
        LOG.debug("Method name: $methodName")
      }
    }

    LOG.debug("No animations: $noAnimations")

    LOG.debug("Test size: $testSize")

    String cp = getClasspath()
    LOG.debug("Classpath: $cp")

    SpoonRunner.Builder runBuilder = new SpoonRunner.Builder()    
        .setTitle(title)
        .setApplicationApk(applicationApk)
        .setInstrumentationApk(instrumentationApk)
        .setOutputDirectory(output)
        .setFailIfNoDeviceConnected(failIfNoDeviceConnected)
        .setDebug(debug)
        .setClassName(className)
        .setMethodName(methodName)
        .setAndroidSdk(project.android.sdkDirectory)
        .setClasspath(cp)
        .setNoAnimations(noAnimations)

    if (testSize != TEST_SIZE_ALL) {
      // Will throw exception with informative message if provided size is illegal
      runBuilder.setTestSize(IRemoteAndroidTestRunner.TestSize.getTestSize(testSize))
    }  
    
    if (adbTimeout != -1) {
      LOG.info("ADB timeout $adbTimeout")
      runBuilder.setAdbTimeout(adbTimeout)
    }  
        
    if (allDevices) {
      runBuilder.useAllAttachedDevices()
      LOG.info("Using all the attached devices")
    } else {
      if (!devices) {
        throw new GradleException("No devices specified to run the tests on");
      }
      devices.each {
        runBuilder.addDevice(it)
      }
      LOG.info("Using devices $devices")
    }

    boolean success = runBuilder.build().run()

    if (!success && !ignoreFailures) {
      throw new GradleException("Tests failed! See ${output}/index.html")
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
