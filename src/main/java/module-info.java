/**
 * Module declaration for the net.xyzsd.dichotomy module.
 * This module exports three packages:
 * <ul>
 * <li>>net.xyzsd.dichotomy.trying.function</li>
 * <li>net.xyzsd.dichotomy</li>
 * <li>net.xyzsd.dichotomy.trying</li>
 * </ul>
 *
 * This module requires the org.jetbrains.annotations module in a static manner.
 *
 */
module net.xyzsd.dichotomy {
    requires static org.jetbrains.annotations;

    exports net.xyzsd.dichotomy.trying.function;
    exports net.xyzsd.dichotomy;
    exports net.xyzsd.dichotomy.trying;
}