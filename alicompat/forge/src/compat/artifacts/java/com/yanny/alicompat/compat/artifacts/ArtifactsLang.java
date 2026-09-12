package com.yanny.alicompat.compat.artifacts;

import com.yanny.aci.language.CoreLang;
import com.yanny.aci.language.ITooltipKey;
import com.yanny.aci.language.Translation;
import com.yanny.alicompat.ICompatTranslations;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class ArtifactsLang implements ICompatTranslations {
    static final String MOD_ID = "artifacts";

    public static final Map<String, String> TRANSLATION_MAP = new HashMap<>();

    public enum Conditions implements ITooltipKey {
        ARTIFACT_RARITY_ADJUSTED_CHANCE("artifact_rarity_adjusted_chance", "Artifact Rarity Adjusted Chance:"),
        CONFIG_VALUE_CHANCE("config_value_chance", "Config Value Chance:"),
        IS_APRIL_FOOLS("is_april_fools", "Is April Fools"),
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

    public enum Functions implements ITooltipKey {
        REPLACE_WITH_LOOT_TABLE("replace_with_loot_table", "Replace With Loot Table:"),
        ;

        private final Translation translation;

        Functions(String k, String e) {
            translation = new Translation("alicompat.type.function." + k, e);
        }

        @NotNull
        @Override
        public Translation getTranslation() {
            return translation;
        }
    }

    public enum Value implements ITooltipKey {
        CONFIG("config", "Config: %s"),
        DEFAULT_PROBABILITY("default_probability", "Default Probability: %s"),
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
        CoreLang.register(TRANSLATION_MAP, Conditions.class);
        CoreLang.register(TRANSLATION_MAP, Functions.class);
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
