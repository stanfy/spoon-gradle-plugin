package com.stanfy.spoon.gradle

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

/**
 * Spec for SpoonRunTask.
 */
class SpoonPluginSpec extends Specification {

  def "should require Android plugin"() {
    when:
    Project p = ProjectBuilder.builder().build()
    p.apply plugin: 'spoon'

    then:
    def e = thrown(IllegalStateException.class)
    e.message == "Android plugin is not found"
  }

  def "should add spoon task"() {
    when:
    Project p = ProjectBuilder.builder().build()
    p.apply plugin: 'android'
    p.apply plugin: 'spoon'

    then:
    p.tasks.findByName('spoon') != null
  }

  // TODO: still do not know how to properly make integration tests; use tooling API?

}
