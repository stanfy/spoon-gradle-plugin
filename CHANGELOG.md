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
