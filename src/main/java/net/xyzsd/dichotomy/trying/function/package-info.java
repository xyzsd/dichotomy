/**
 * Exception-producing functional interfaces.
 * <p>
 *     The functional interfaces in this package are analogous to the {@link java.util.function} interfaces,
 *     except that they can throw exceptions--in particular, checked exceptions.
 * </p>
 * <p>
 *     These interfaces are primarily provided for use with {@link net.xyzsd.dichotomy.trying.Try}, to
 *     allow functional code to wrap and use exception-generating methods in a convenient manner.
 * </p>
 * <p>
 *     These interfaces need be used if Runtime exceptions are thrown; instead, standard JDK
 *     functional interfaces (e.g., {@code Runnable, Consumer, Supplier, Function, BiFunction})
 *     can be used instead.
 * </p>
 */
package net.xyzsd.dichotomy.trying.function;