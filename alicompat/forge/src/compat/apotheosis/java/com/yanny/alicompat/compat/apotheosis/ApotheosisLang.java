package com.yanny.alicompat.compat.apotheosis;

import com.yanny.aci.language.CoreLang;
import com.yanny.aci.language.ITooltipKey;
import com.yanny.aci.language.Translation;
import com.yanny.alicompat.ICompatTranslations;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class ApotheosisLang implements ICompatTranslations {
    static final String MOD_ID = "apotheosis";

    public static final Map<String, String> TRANSLATION_MAP = new HashMap<>();

    public enum Conditions implements ITooltipKey {
        REQUIRES_PLAYER("requires_player", "Requires a player"),
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

    public enum Entry implements ITooltipKey {
        RANDOM_AFFIX_ITEM("random_affix_item", "Random Affix Item:"),
        RANDOM_GEM("random_gem", "Random Gem:"),
        ;

        private final Translation translation;

        Entry(String k, String e) {
            translation = new Translation("alicompat.type.entry." + k, e);
        }

        @NotNull
        @Override
        public Translation getTranslation() {
            return translation;
        }
    }

    public enum Ingredient implements ITooltipKey {
        AFFIX_ITEM("affix_item", "Any Affix Item:"),
        GEM("gem", "Any Gem:"),
        ;

        private final Translation translation;

        Ingredient(String k, String e) {
            translation = new Translation("alicompat.type.ingredient." + k, e);
        }

        @NotNull
        @Override
        public Translation getTranslation() {
            return translation;
        }
    }

    public enum GlobalLootModifier implements ITooltipKey {
        AFFIX_CONVERSION("affix_conversion", "Affix Conversion:"),
        RANDOM_AFFIX_ITEM("random_affix_item", "Random Affix Item:"),
        ;

        private final Translation translation;

        GlobalLootModifier(String k, String e) {
            translation = new Translation("alicompat.type.global_loot_modifier." + k, e);
        }

        @NotNull
        @Override
        public Translation getTranslation() {
            return translation;
        }
    }

    public enum Branch implements ITooltipKey {
        RARITY("rarity", "Rarity:"),
        STAGES("stages", "Stages:"),
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

    public enum Value implements ITooltipKey {
        MAX_RARITY("max_rarity", "Max Rarity: %s"),
        MIN_RARITY("min_rarity", "Min Rarity: %s"),
        RARITY("rarity", "Rarity: %s"),
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
        CoreLang.register(TRANSLATION_MAP, Entry.class);
        CoreLang.register(TRANSLATION_MAP, Ingredient.class);
        CoreLang.register(TRANSLATION_MAP, GlobalLootModifier.class);
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
