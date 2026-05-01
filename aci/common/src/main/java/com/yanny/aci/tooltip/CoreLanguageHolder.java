package com.yanny.aci.tooltip;

import java.util.HashMap;
import java.util.Map;

public class CoreLanguageHolder {
    public static final Map<String, String> TRANSLATION_MAP = new HashMap<>();

    static {
        TRANSLATION_MAP.put("aci.util.auto_detected", "Auto-detected: %s");
        TRANSLATION_MAP.put("aci.util.missing", "Not implemented: %s");
        TRANSLATION_MAP.put("aci.util.two_values", "%s%s");
        TRANSLATION_MAP.put("aci.util.two_values_with_space", "%s %s");
        TRANSLATION_MAP.put("aci.util.pad.1", "  ->");
        TRANSLATION_MAP.put("aci.util.pad.2", "    ->");
        TRANSLATION_MAP.put("aci.util.pad.3", "      ->");
        TRANSLATION_MAP.put("aci.util.pad.4", "        ->");
        TRANSLATION_MAP.put("aci.util.pad.5", "          ->");
        TRANSLATION_MAP.put("aci.util.pad.6", "            ->");
        TRANSLATION_MAP.put("aci.util.pad.7", "              ->");
        TRANSLATION_MAP.put("aci.util.pad.8", "                ->");
        TRANSLATION_MAP.put("aci.util.pad.9", "                  ->");
        TRANSLATION_MAP.put("aci.util.pad.10", "                    ->");
        TRANSLATION_MAP.put("aci.util.pad.11", "                      ->");
        TRANSLATION_MAP.put("aci.util.pad.12", "                        ->");
        TRANSLATION_MAP.put("aci.util.pad.13", "                          ->");
        TRANSLATION_MAP.put("aci.util.pad.14", "                            ->");
        TRANSLATION_MAP.put("aci.util.pad.15", "                              ->");
        TRANSLATION_MAP.put("aci.util.pad.16", "                                ->");
        TRANSLATION_MAP.put("aci.util.pad.17", "                                  ->");
        TRANSLATION_MAP.put("aci.util.pad.18", "                                    ->");
        TRANSLATION_MAP.put("aci.util.pad.19", "                                      ->");
        TRANSLATION_MAP.put("aci.util.pad.20", "                                        ->");
        TRANSLATION_MAP.put("aci.util.key_value", "%s: %s");
        TRANSLATION_MAP.put("aci.util.null", "%s");
        TRANSLATION_MAP.put("aci.util.values", "Values:");
        TRANSLATION_MAP.put("aci.util.removed", "REMOVED");
    }
}
