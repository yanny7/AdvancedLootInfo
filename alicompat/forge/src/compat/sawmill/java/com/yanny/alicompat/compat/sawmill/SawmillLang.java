package com.yanny.alicompat.compat.sawmill;

import com.yanny.aci.language.CoreLang;
import com.yanny.aci.language.ITooltipKey;
import com.yanny.aci.language.Translation;
import com.yanny.alicompat.ICompatTranslations;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class SawmillLang implements ICompatTranslations {
    static final String MOD_ID = "sawmill";

    public static final Map<String, String> TRANSLATION_MAP = new HashMap<>();

    public enum Value implements ITooltipKey {
        ANY_WOOD_TYPE("any_wood_type", "Any wood type"),
        VILLAGE_WOOD_TYPE("village_wood_type", "Wood type of the village"),
        ;

        private final Translation translation;

        Value(String k, String e) {
            translation = new Translation("alicompat.property.value." + k, e);
        }

        @NotNull
        @Override
        public Translation getTranslation() {
            return translation;
        }
    }

    static {
        CoreLang.register(TRANSLATION_MAP, Value.class);
    }

    @NotNull
    @Override
    public String targetModId() {
        return MOD_ID;
    }

    @NotNull
    @Override
    public Map<String, String> getTranslations() {
        return TRANSLATION_MAP;
    }
}
