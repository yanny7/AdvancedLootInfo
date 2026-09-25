package com.yanny.alicompat.compat.kaleidoscopecookery;

import com.yanny.aci.language.CoreLang;
import com.yanny.aci.language.ITooltipKey;
import com.yanny.aci.language.Translation;
import com.yanny.alicompat.ICompatTranslations;
import com.yanny.alicompat.Utils;
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
            translation = new Translation(Utils.langKey(MOD_ID, "type.condition", k), e);
        }

        @NotNull
        @Override
        public Translation getTranslation() {
            return translation;
        }
    }

    public enum Functions implements ITooltipKey {
        RECIPE_RANDOMLY("recipe_randomly", "Random Recipe:"),
        ;

        private final Translation translation;

        Functions(String k, String e) {
            translation = new Translation(Utils.langKey(MOD_ID, "type.function", k), e);
        }

        @NotNull
        @Override
        public Translation getTranslation() {
            return translation;
        }
    }

    public enum Branch implements ITooltipKey {
        INPUTS("inputs", "Inputs:"),
        OUTPUT("output", "Output:"),
        ;

        private final Translation translation;

        Branch(String k, String e) {
            translation = new Translation(Utils.langKey(MOD_ID, "property.branch", k), e);
        }

        @NotNull
        @Override
        public Translation getTranslation() {
            return translation;
        }
    }

    public enum Value implements ITooltipKey {
        FLEX_RECIPE("flex_recipe", "Flexible Recipe: %s"),
        RECIPE_TYPE("recipe_type", "Recipe Type: %s"),
        ;

        private final Translation translation;

        Value(String k, String e) {
            translation = new Translation(Utils.langKey(MOD_ID, "property.value", k), e);
        }

        @NotNull
        @Override
        public Translation getTranslation() {
            return translation;
        }
    }

    static {
        CoreLang.register(TRANSLATION_MAP, Conditions.class);
        CoreLang.register(TRANSLATION_MAP, Functions.class);
        CoreLang.register(TRANSLATION_MAP, Branch.class);
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
