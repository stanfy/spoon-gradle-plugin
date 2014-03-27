Spoon Gradle Plugin
===================

Gradle plugin for [Spoon](https://github.com/square/spoon).
Allows you to run spoon with almost no effort under new Android build system.

[![Build Status](https://travis-ci.org/stanfy/spoon-gradle-plugin.png?branch=master)](https://travis-ci.org/stanfy/spoon-gradle-plugin)

Plugin generates `spoon${TEST_VARIANT}` tasks for every test variation in your application project.

Usage
-----
```groovy
buildscript {
  repositories {
    mavenCentral()
  }

  dependencies {
    classpath 'com.stanfy.spoon:spoon-gradle-plugin:0.9.3'
  }
}

apply plugin: 'spoon'

// This section is optional
spoon {
  // for debug output
  debug = true

  // If enabled, will create tasks for running subsets of all tests -
  // one for each test size annotation (@SmallTest, @MediumTest, @LargeTest)
  useTestSizes = true

  // To run a single test class
  className = 'fully.qualified.TestCase'

  // To run a single method in TestCase
  methodName = 'testMyApp'
}
```

After applying the plugin you'll find `spoon${TEST_VARIANT}` tasks in your project.

You may run all the test variations with
```
gradle spoon
```

Generated reports are available at `build/spoon/${TEST_VARIANT}` folder.

For making screenshots add `spoon-client` dependency to your tests compile configuration:
```groovy
dependencies {

  instrumentTestCompile 'com.squareup.spoon:spoon-client:1.1.0'

}
```

By default the plugin runs tests on all the connected devices.
In order to run them on some concrete devices instead, you may specify their serial numbers:
```
spoon {
  devices = ['333236E9AE5800EC']
}
```

It is also allowed to specify specify size of tests that should be run. You may run all the tests
annotated as `@SmallTest` with the following line:
```bash
gradle spoonSmall
```
Run `gradle tasks` for details about size rules.


You may also setup your project to take parameters for class/method to be run from command line. E.g.:

```bash
gradle spoon -PspoonClassName=fully.qualified.TestCase
```

And project configuration:

```groovy
spoon {
  if (project.hasProperty('spoonClassName')) {
    className = project.spoonClassName  
  }
}
```

License
-------

    Copyright 2013 Stanfy Corp.

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
