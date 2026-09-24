package com.yanny.alicompat.compat.kaleidoscopecookery;

import com.yanny.aci.language.CoreLang;
import com.yanny.aci.language.ITooltipKey;
import com.yanny.aci.language.Translation;
import com.yanny.alicompat.ICompatTranslations;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class KaleidoscopeCookeryLang implements ICompatTranslations {
    static final String MOD_ID = "kaleidoscope_cookery";

    public static final Map<String, String> TRANSLATION_MAP = new HashMap<>();

    public enum Conditions implements ITooltipKey {
        ADVANCE_BLOCK_MATCH_TOOL("advance_block_match_tool", "Breaker Equipment:"),
        ADVANCE_ENTITY_MATCH_TOOL("advance_entity_match_tool", "Killer Equipment:"),
        ;

        private final Translation translation;

        Conditions(String k, String e) {
            translation = new Translation("alicompat.type.condition." + k, e);
        }

        @NotNull
        @Override
        public Translation getTranslation() {
            return translation;
        }
    }

    static {
        CoreLang.register(TRANSLATION_MAP, Conditions.class);
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
