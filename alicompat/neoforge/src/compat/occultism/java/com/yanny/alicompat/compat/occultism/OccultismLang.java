package com.yanny.alicompat.compat.occultism;

import com.yanny.aci.language.CoreLang;
import com.yanny.aci.language.ITooltipKey;
import com.yanny.aci.language.Translation;
import com.yanny.alicompat.ICompatTranslations;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class OccultismLang implements ICompatTranslations {
    static final String MOD_ID = "occultism";

    public static final Map<String, String> TRANSLATION_MAP = new HashMap<>();

    public enum Value implements ITooltipKey {
        WITH_THIRD_EYE("with_third_eye", "Requires Third Eye, Otherworld Goggles or True Sight Staff"),
        WITHOUT_THIRD_EYE("without_third_eye", "Without Third Eye, Otherworld Goggles or True Sight Staff"),
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
