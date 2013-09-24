Spoon Gradle Plugin
===================

Gradle plugin for [Spoon](https://github.com/square/spoon).
Allows you to run spoon with almost no effort under new Android build system.

Plugin generates `spoon${TEST_VARIANT}` tasks for every test variation in your application project.

Usage
-----
```groovy
buildscript {
  repositories {
    mavenCentral()
  }

  dependencies {
    classpath 'com.stanfy.spoon:spoon-gradle-plugin:0.9.+'
  }
}

apply plugin: 'spoon'

// for debug output
spoon {
  debug = true
}
```

After applying the plugin you'll find `spoon${TEST_VARIANT}` tasks in your project.

For default build configuration tests may be run like
```
gradle spoon
```

Generated reports are available at `build/spoon/${TEST_VARIANT}` folder.

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
