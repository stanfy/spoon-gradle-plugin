package com.stanfy.spoon.gradle

import org.gradle.api.Project
import org.gradle.api.internal.plugins.PluginApplicationException
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
    def e = thrown(PluginApplicationException.class)
    e.cause.message == "Android plugin is not found"
  }

  def "can be applied to library"() {
    when:
    Project p = ProjectBuilder.builder().build()
    p.apply plugin: 'com.android.library'
    p.apply plugin: 'spoon'
    then:
    p.spoon
  }

  def "can be applied to application"() {
    when:
    Project p = ProjectBuilder.builder().build()
    p.apply plugin: 'com.android.application'
    p.apply plugin: 'spoon'
    then:
    p.spoon
  }

}
