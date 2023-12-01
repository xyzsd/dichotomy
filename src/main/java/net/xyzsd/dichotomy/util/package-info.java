/**
 * static conversion utility methods to convert between monadic types.
 * <p>
 * This is a separate package to allow easier severability of classes without dependencies; e.g., a potential user
 * may want to extract out the {@link net.xyzsd.dichotomy.Result} type; the only dependency would be
 * {@link net.xyzsd.dichotomy.Empty}.
 *
 */
package net.xyzsd.dichotomy.util;