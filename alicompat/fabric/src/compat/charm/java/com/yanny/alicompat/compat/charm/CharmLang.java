package com.yanny.alicompat.compat.charm;

import com.yanny.aci.language.CoreLang;
import com.yanny.aci.language.ITooltipKey;
import com.yanny.aci.language.Translation;
import com.yanny.alicompat.ICompatTranslations;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class CharmLang implements ICompatTranslations {
    static final String MOD_ID = "charm";

    public static final Map<String, String> TRANSLATION_MAP = new HashMap<>();

    public enum Branch implements ITooltipKey {
        ALTERNATIVE("alternative", "Alternative:"),
        ;

        private final Translation translation;

        Branch(String k, String e) {
            translation = new Translation("alicompat.property.branch." + k, e);
        }

        @NotNull
        @Override
        public Translation getTranslation() {
            return translation;
        }
    }

    static {
        CoreLang.register(TRANSLATION_MAP, Branch.class);
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
