/**
 * Module declaration for the net.xyzsd.dichotomy module.
 * This module exports the following packages:
 * <ul>
 * <li>net.xyzsd.dichotomy</li>
 * <li>net.xyzsd.dichotomy.collectors</li>
 * <li>net.xyzsd.dichotomy.trying</li>
 * <li>net.xyzsd.dichotomy.trying.function</li>
 * </ul>
 *
 */
module net.xyzsd.dichotomy {
    requires static org.jetbrains.annotations;

    exports net.xyzsd.dichotomy;
    exports net.xyzsd.dichotomy.collectors;
    exports net.xyzsd.dichotomy.trying;
    exports net.xyzsd.dichotomy.trying.function;
}