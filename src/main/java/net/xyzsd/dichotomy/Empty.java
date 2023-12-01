package net.xyzsd.dichotomy;

/**
 * {@link Void} type alternative.
 * <p>
 * {@link Void}  is problematic when as used as a type parameter because of its association with {@code null},
 * as it cannot be instantiated, and extensive null-checking is in place.
 * </p>
 * <p>
 * Instead, the {@link Empty} type can be used. All {@link Empty} types are considered equivalent.
 * </p>
 */
public record Empty() {}
// 'Empty' instead of None to differ from Maybe.None. 'Nil' could be a consideration. or maybe all caps to denote singleton ('NONE')
