package com.yanny.awi.datagen;

import com.yanny.aci.language.CoreLang;

import java.util.HashMap;
import java.util.Map;

public class LanguageHolder {
    public static final Map<String, String> TRANSLATION_MAP = new HashMap<>(CoreLang.TRANSLATION_MAP);

    static {
        TRANSLATION_MAP.put("awi.property.value.discard_chance_on_air_exposure", "Discard Chance On Air Exposure: %s");
        TRANSLATION_MAP.put("awi.property.value.size", "Size: %s");
        TRANSLATION_MAP.put("awi.property.value.block", "Block: %s");

        TRANSLATION_MAP.put("awi.property.branch.target_states", "Target States:");
        TRANSLATION_MAP.put("awi.property.branch.state", "State:");
        TRANSLATION_MAP.put("awi.property.branch.target", "Target:");
        TRANSLATION_MAP.put("awi.property.branch.properties", "Properties:");

        TRANSLATION_MAP.put("awi.feature_configuration.count", "Count: %s");
        TRANSLATION_MAP.put("awi.feature_configuration.ore", "Ore:");

        TRANSLATION_MAP.put("awi.rule_test.always_true", "Always True");
    }
}
