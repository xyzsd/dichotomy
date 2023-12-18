[![Java CI with Gradle](https://github.com/xyzsd/dichotomy/actions/workflows/gradle.yml/badge.svg)](https://github.com/xyzsd/dichotomy/actions/workflows/gradle.yml)

# dichotomy
Either and Result monadic types for Java.
Includes specialized types Try and Maybe.

All types are sealed, and can be used in switch statements and
pattern matching.

## UPDATE (18-Dec-2023):
    * Substantially restructured and improved for the pending 1.0 version
    * Markedly improved Try type, added Maybe type
    * Tests near-complete
    * Improved documentation
    * still needs: a nice intro (with synopsis and illustrative examples)


Download
--------
depend via Maven:

```xml
<dependency>
  <groupId>net.xyzsd</groupId>
  <artifactId>dichotomy</artifactId>
  <version>0.9</version>
  <type>module</type>
</dependency>
```

or Gradle:
```kotlin
implementation("net.xyzsd:dichotomy:0.9")
```



License
-------
Copyright 2022-2023, xyzsd

Licensed under either of:

* Apache License, Version 2.0
  (see LICENSE-APACHE or http://www.apache.org/licenses/LICENSE-2.0)
* MIT license
  (see LICENSE-MIT) or http://opensource.org/licenses/MIT)

at your option.


    
    

