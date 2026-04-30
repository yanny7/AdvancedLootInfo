package com.yanny.awi.datagen;

import java.util.HashMap;
import java.util.Map;

public class LanguageHolder {
    public static final Map<String, String> TRANSLATION_MAP = new HashMap<>();

    static {
        TRANSLATION_MAP.put("awi.util.value.missing", "Not implemented: %s");

        TRANSLATION_MAP.put("awi.property.value.count", "Count: %s");
        TRANSLATION_MAP.put("awi.property.value.null", "%s");

        TRANSLATION_MAP.put("awi.property.branch.values", "Values:");
    }
}
