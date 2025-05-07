[![Java CI with Gradle](https://github.com/xyzsd/dichotomy/actions/workflows/gradle.yml/badge.svg)](https://github.com/xyzsd/dichotomy/actions/workflows/gradle.yml)

# dichotomy
Sealed monads for Java.

Generally these types are used to return one of two values, such as success or failure. 

All types are sealed (Sum types), and can be used in `switch` expressions and with
pattern matching.

`dichotomy` is svelte, at about ~48 KB.

### `Either`:
A general immutable type that can only be *either* one of two types.
The types are called `Left<L>` and `Right<R>`. By convention, the Left type
indicates failure, while the Right type indicates success.

### `Result`:
Similar to an `Either`, but with success/failure semantics more clearly defined.
An `OK<V>` Result indicates success, and an `Err<E>` Result indicates failure. Failure
types do not need to be Exceptions. 

 ```java 

Result<Double,String> result = Result.<Integer, String>ofOK(3828)  // returns an OK<Integer>
        .map(x -> x*10.0)        // map to Result<Double,String>, after multiplying x 10
        .match(System.out::println)     // print "38280.0" to console
        .matchErr(System.err::println);   // ignored, as this is an OK

switch(result) {
    case OK<Double,String> ok -> System.out.println("value ok! value: "+ok.value());
    case Err<Double,String> err -> System.err.println(err.value());
}

// JDK 21+
switch(result) {
    case OK(Double x) when x > 0 -> System.out.println("positive");
    case OK(Double x) -> System.out.println("0 or negative");
    case Err(String s) -> System.err.println(s);
}

// anotherResult here will be an Err<String>
Result<Double,String> anotherResult = Result.<Integer, String>ofErr("Insufficient entropy")
          .map(x -> x*10.0 )       // ignored, as this is an Err
          .match(System.out::println)     // ignored, as this is an Err
          .matchErr(System.err::println);  // "Insufficient entropy" printed to System.err
```

  
### `Try`:
A specialized type of `Result`. A `Try` wraps a function or block; if 
successful, a `Success` Try is returned; otherwise, a `Failure` Try containing
an Exception is returned. Intermediate operations which return Trys will also
catch generated Exceptions.

```java

final Try<Integer> result = Try.ofSuccess( 777 )
        .map( i -> i * 1000 )           // results in a Try<Integer> with a value of 777000
        .exec( System.out::println )    // prints "777000"
        .map( i -> i / 0 )              // the ArithmeticException is caught as a Try.Failure
        .exec( System.out::println );   // does not exec() because we are a Failure

// prints "ERROR: java.lang.ArithmeticException: / by zero"
switch(result) {
        case Success(Integer i) -> System.out.printf("Operation completed successfully. Value: %d\n", i);
        case Failure(Throwable t) -> System.err.printf("ERROR: %s\n", t);
}
  

```

### `Maybe`:
Analogous to the JDK `Optional` type, but sealed so it may be used in `switch` 
statements and with pattern matching.  


## Updates 

Note that that future versions will target JDK 24, because of JEP-485 (Stream Gatherers) finalization. 
An experimental `ResultStream<V,E>` which can directly operate on `Results` will be the new feature. 

Of course, any feature requests or improvements are always welcome.

### Release 1.1 (May 2025)
Target remains JDK 21.

[JSpecify](https://jspecify.dev/) replaces JSR-305 nullness annotations. 

This release also adds `Result::merge` which simplifies reduction operations on `Result` streams 
(see method documentation and tests for examples).


### Release 1.0 (January 2024)
Refactored and improved from the original version. 

Handling exceptions is substantially better with the new
`Try` type (a specialized type of `Result`), which also
supports the try-with-resources pattern.

Some usage examples are now included... though additional
illustrative examples should be provided!

Documentation
-------------
[Available online][doc_online] or by [download][doc_download].


Download
--------
depend via Maven:

```xml
<dependency>
  <groupId>net.xyzsd</groupId>
  <artifactId>dichotomy</artifactId>
  <version>1.1</version>
  <type>module</type>
</dependency>
```

or Gradle:
```kotlin
implementation("net.xyzsd:dichotomy:1.1")
```

or [download](https://github.com/xyzsd/dichotomy/releases/tag/1.0) from GitHub.

License
-------
Copyright 2022-2025, xyzsd

Many thanks to [@fdelsert](https://github.com/fdelsert) for
suggestions and improvements leading to the 1.0 release.

Licensed under either of:

* Apache License, Version 2.0
  (see LICENSE-APACHE or http://www.apache.org/licenses/LICENSE-2.0)
* MIT license
  (see LICENSE-MIT) or http://opensource.org/licenses/MIT)

at your option.


[doc_online]: https://javadoc.io/doc/net.xyzsd/dichotomy/1.1/index.html
[doc_download]: https://github.com/xyzsd/dichotomy/releases/download/1.1/dichotomy-javadoc-1.1-javadoc.jar

