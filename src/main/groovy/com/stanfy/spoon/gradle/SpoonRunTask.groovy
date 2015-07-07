package com.stanfy.spoon.gradle

import com.android.ddmlib.testrunner.IRemoteAndroidTestRunner
import com.squareup.spoon.SpoonRunner
import groovy.transform.PackageScope
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.artifacts.ResolvedDependency
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

  /** The number of separate shards to create. */
  int numShards

  /** The shardIndex option to specify which shard to run. */
  int shardIndex

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

    String cp = getClasspath()
    LOG.debug("Classpath: $cp")

    SpoonRunner.Builder runBuilder = new SpoonRunner.Builder()
        .setTerminateAdb(false)
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

    if (numShards > 0) {
      runBuilder.setInstrumentationArgs(["numShards=${numShards}".toString(), "shardIndex=${shardIndex}".toString()])
    }

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

  private static def findCpDependency(final Project project, final String prefix) {
    def configuration = project.buildscript.configurations.classpath
    return configuration.resolvedConfiguration.firstLevelModuleDependencies.find {
      it.name.startsWith prefix
    }
  }

  private def lookupDependency(final String prefix) {
    def dep = null
    def p = project, pp = null
    while (!dep && p) {
      pp = p
      dep = findCpDependency(p, prefix)
      p = p.parent
    }
    return [dep, pp]
  }

  private static void collectDependencies(ResolvedDependency root, def classpath, def allDeps) {
    root.children.each { dep ->
      def conflict = allDeps.find { it.moduleName == dep.moduleName && it.moduleGroup == dep.moduleGroup }
      if (conflict && conflict.moduleVersion != dep.moduleVersion) {
        LOG.warn("There is a dependencies conflict for ${dep.moduleGroup}:${dep.modeuleName}. "
            + "Versions: ${conflict.modeuleVersion} and ${dep.modeuleVersion}")
      } else {
        allDeps.add dep
      }
    }
    classpath.addAll root.allModuleArtifacts.collect { artifact -> artifact.file }
    root.children.each {
      collectDependencies it, classpath, allDeps
    }
  }

  private static void checkDependencies(ResolvedDependency root, def classpath, def allDeps) {
    root.children.each { dep ->
      def existing = allDeps.find { it.moduleName == dep.moduleName && it.moduleGroup == dep.moduleGroup }
      if (existing) {
        collectDependencies(dep, classpath, allDeps)
      }
    }
    root.children.each { dep ->
      checkDependencies(dep, classpath, allDeps)
    }
  }

  private String getClasspath() {
    def (pluginDep, usedProject) = lookupDependency(PLUGIN_DEP_NAME)
    if (!pluginDep) {
      throw new IllegalStateException("Could not resolve spoon dependencies")
    }

    ResolvedDependency spoon = pluginDep.children.find { it.name.startsWith SpoonRunTask.SPOON_DEP_NAME } as ResolvedDependency
    if (!spoon) { throw new IllegalStateException("Cannot find spoon-runner in dependencies") }
    // Collect spoon dependencies.
    def classpath = new HashSet(), allDeps = []
    collectDependencies(spoon, classpath, allDeps)
    // Add spoon runner.
    classpath += pluginDep.allModuleArtifacts.find { it.name == SpoonRunTask.SPOON_RUNNER_ARTIFACT }.file

    /*
     * XXX Due to how Gradle dependencies resolution works we can get not all the required deps from the step above.
     *     For example spoon dep is com.android.tools.ddms:ddmlib:23.2.1.
     *     If you use Android plugin 1.1.3, ddmlib will be resolved to 24.1.3. And its dependencies will not traversed yet.
     *     Hence, we traverse the whole tree again and pick up those parts of the tree that we miss.
     */
    usedProject.buildscript.configurations.classpath.resolvedConfiguration.firstLevelModuleDependencies.each {
      SpoonRunTask.checkDependencies((ResolvedDependency) it, classpath, allDeps)
    }

    return project.files(classpath).asPath
  }

}
