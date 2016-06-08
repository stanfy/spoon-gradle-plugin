Version 1.2.0
-------------
*2016-06-08*

- Added 'shard' option to divide test suite across all devices.
- Spoon runner dependency is changed to 1.5.4.

Version 1.1.0
-------------
*2016-04-28*

- Provide coverage report (spoon runner 1.5.0 is used)

Version 1.0.4
-------------
*2016-01-08*

- Adapt to Android plugin 1.5.0
- Spoon runner dependency is changed to 1.3.1.

Version 1.0.3
-------------
*2015-07-09*

- Support for test sharding and custom instrumentation arguments.
- Spoon runner dependency is changed to 1.1.10.

Version 1.0.2
-------------
*2015-04-09*

- Spoon runner dependency is changed to 1.1.9.

Version 1.0.1
-------------
*2015-03-22*

- Spoon runner dependency is changed to 1.1.8.

Version 1.0.0
-------------
*2015-03-19*

- Spoon runner dependency is changed to 1.1.7.
  Now we can run spoon for multiple flavors/subprojects without adb issues
  (see [spoon changelog](https://github.com/square/spoon/blob/master/CHANGELOG.md)).
- It's ok now to specify the plugin dependency in a parent gradle project (fixed #1).
- Fixed classpath issues for multiple devices.

Version 0.14.1
--------------
*2014-11-10*

- Binaries are compiled for Java 6. 0.14.0 was compiled for Java 7.

Version 0.14.0
--------------
*2014-11-08*

- Adaptation to multiple APK outputs introduced in the Android Gradle plugin 0.13.x.
This plugin does not use deprecated methods. Yet, be warned, density/abi splits are not really supported.
- Spoon runner dependency bump. This makes it possible to use the plugin with Android plugin 0.14.x.
- Support library projects.


Version 0.10.0
--------------
Change log is lost. Sorry.
