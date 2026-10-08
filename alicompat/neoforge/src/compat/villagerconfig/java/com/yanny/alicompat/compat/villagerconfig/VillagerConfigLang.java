package com.yanny.alicompat.compat.villagerconfig;

import com.yanny.aci.language.CoreLang;
import com.yanny.aci.language.ITooltipKey;
import com.yanny.aci.language.Translation;
import com.yanny.alicompat.ICompatTranslations;
import com.yanny.alicompat.Utils;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class VillagerConfigLang implements ICompatTranslations {
    static final String MOD_ID = "villagerconfig";

    public static final Map<String, String> TRANSLATION_MAP = new HashMap<>();

    public enum Functions implements ITooltipKey {
        ENCHANT_RANDOMLY("enchant_randomly", "Enchant Randomly:"),
        SET_DYE("set_dye", "Set Dye:"),
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
        DYE_COLORS("dye_colors", "Dye Colors:"),
        EXCLUDE("exclude", "Exclude:"),
        INCLUDE("include", "Include:"),
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
        MAX_LEVEL("max_level", "Max Level: %s"),
        MIN_LEVEL("min_level", "Min Level: %s"),
        PRICE_MULTIPLIER("price_multiplier", "Price Multiplier: %s"),
        REWARD_EXPERIENCE("reward_experience", "Reward Experience: %s"),
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
