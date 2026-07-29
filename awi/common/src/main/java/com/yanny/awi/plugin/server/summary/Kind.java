package com.yanny.awi.plugin.server.summary;

/**
 * Shape of a value distribution (int count or height provider). Used purely as a label so the
 * client / test can tell how the value is distributed across its range.
 */
public enum Kind {
    CONSTANT,
    UNIFORM,
    BIASED_TO_BOTTOM,
    VERY_BIASED_TO_BOTTOM,
    TRAPEZOID,
    CLAMPED,
    CLAMPED_NORMAL,
    WEIGHTED,
    RELATIVE_TO_HEIGHTMAP,
    UNKNOWN
}
