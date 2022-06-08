package net.xyzsd.dichotomy;

/**
 *  {@link Void} type alternative.
 *  <p>
 *      {@link Void}  is problematic when as used as a type parameter because of its association with {@code null},
 *      as it cannot be instantiated, and extensive null-checking in place.
 *  </p>
 *  <p>
 *      Instead, the {@link None} type can be used. All {@link None} types are considered equivalent.
 *  </p>
 *
 */
public record None() {}
