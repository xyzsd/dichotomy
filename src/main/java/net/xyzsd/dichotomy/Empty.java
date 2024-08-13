package net.xyzsd.dichotomy;

import org.jspecify.annotations.NullMarked;

/**
 * {@link Void} type alternative.
 * <p>
 * {@link Void}  is problematic when as used as a type parameter because of its association with {@code null},
 * as it cannot be instantiated, and extensive null-checking is in place.
 * </p>
 * <p>
 * Instead, the {@link Empty} type can be used.
 * </p>
 */
@NullMarked
public final class Empty {

    private static final Empty INSTANCE = new Empty();

    private Empty() {
    }

    /**
     * An instance of the Empty singleton.
     * @return the Empty instance
     */
    public static Empty getInstance() {
        return INSTANCE;
    }
}
