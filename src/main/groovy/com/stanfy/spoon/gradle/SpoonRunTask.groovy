package com.stanfy.spoon.gradle

import com.android.ddmlib.testrunner.IRemoteAndroidTestRunner
import com.squareup.spoon.SpoonRunner
import groovy.transform.PackageScope
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.Duration

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

  /** Extra arguments to pass to instrumentation. */
  List<String> instrumentationArgs

  /** Whether or not animations are enabled */
  boolean noAnimations
  
  /** Size of test to be run ('small' / 'medium' / 'large'). */
  @Input
  String testSize

  /** Instrumentation APK. */
  @InputFile
  File instrumentationApk

  /** Application APK. */
  File applicationApk

  /** Output directory. */
  @OutputDirectory
  File output

  /** Use all the connected devices flag. */
  boolean allDevices

  /** Devices to run on. */
  Set<String> devices

  /** Devices to skip running on. */
  Set<String> skipDevices

  /** The number of separate shards to create. */
  int numShards

  /** The shardIndex option to specify which shard to run. */
  int shardIndex

  /** The codeCoverage option to calculate code coverage. */
  boolean codeCoverage;

  /** The shard option to specify whether to shard tests or not. */
  boolean shard;

  /** Execute the tests device by device */
  boolean sequential

  /** Grants all permisions for Android M >= devices */
  boolean grantAllPermissions

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
    LOG.debug("numShards: $numShards")
    LOG.debug("shardIndex: $shardIndex")

    SpoonRunner.Builder runBuilder = new SpoonRunner.Builder()
        .setTerminateAdb(false)
        .setTitle(title)
        .addOtherApk(applicationApk)
        .setTestApk(instrumentationApk)
        .setOutputDirectory(output)
        .setAllowNoDevices(!failIfNoDeviceConnected)
        .setDebug(debug)
        .setClassName(className)
        .setMethodName(methodName)
        .setAndroidSdk(project.android.sdkDirectory)
        .setNoAnimations(noAnimations)
        .setCodeCoverage(codeCoverage)
        .setShard(shard)
        .setSequential(sequential)
        .setGrantAll(grantAllPermissions)
    def instrumentationArgs = this.instrumentationArgs
    if (instrumentationArgs == null) {
      instrumentationArgs = []
    }

    if (numShards > 0) {
      instrumentationArgs.add "numShards=${numShards}".toString()
      instrumentationArgs.add "shardIndex=${shardIndex}".toString()
    }
    if (instrumentationArgs) {
      runBuilder.setInstrumentationArgs(instrumentationArgs)
    }

    if (testSize != TEST_SIZE_ALL) {
      // Will throw exception with informative message if provided size is illegal
      runBuilder.setTestSize(IRemoteAndroidTestRunner.TestSize.getTestSize(testSize))
    }  
    
    if (adbTimeout != -1) {
      LOG.info("ADB timeout $adbTimeout")
      runBuilder.setAdbTimeout(Duration.ofSeconds(adbTimeout))
    }

    if (skipDevices != null && !skipDevices.isEmpty()) {
      skipDevices.each {
        runBuilder.skipDevice(it);
      }
    }
    if (allDevices) {
      LOG.info("Using all the attached devices")
    } else {
      if (!devices) {
        throw new GradleException("No devices specified to run the tests on");
      }
      devices.each {
        runBuilder.addDevice(it)
      }

      LOG.info("Using devices $devices")
      LOG.info("Skipping following devices: $skipDevices")
    }

    boolean success = runBuilder.build().run()

    if (!success && !ignoreFailures) {
      throw new GradleException("Tests failed! See ${output}/index.html")
    }
  }
}
