package com.yanny.alicompat.compat.apotheosis;

import com.yanny.aci.language.CoreLang;
import com.yanny.aci.language.ITooltipKey;
import com.yanny.aci.language.Translation;
import com.yanny.alicompat.ICompatTranslations;
import com.yanny.alicompat.Utils;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class ApotheosisLang implements ICompatTranslations {
    static final String MOD_ID = "apotheosis";

    public static final Map<String, String> TRANSLATION_MAP = new HashMap<>();

    public enum Conditions implements ITooltipKey {
        KILLED_BY_REAL_PLAYER("killed_by_real_player", "Killed by real player"),
        LOOT_TABLE_ID_PATTERN("loot_table_id_pattern", "Loot Table Id Pattern:"),
        MATCHES_BLOCK("matches_block", "Matches Block:"),
        REQUIRES_PLAYER("requires_player", "Requires a player"),
        WORLD_TIER("world_tier", "World Tier:"),
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

    public enum EntitySubPredicates implements ITooltipKey {
        IS_INVADER("is_invader", "Is Invader"),
        IS_MONSTER("is_monster", "Is Monster"),
        ;

        private final Translation translation;

        EntitySubPredicates(String k, String e) {
            translation = new Translation(Utils.langKey(MOD_ID, "type.entity_sub_predicate", k), e);
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
            translation = new Translation(Utils.langKey(MOD_ID, "type.entry", k), e);
        }

        @NotNull
        @Override
        public Translation getTranslation() {
            return translation;
        }
    }

    public enum Functions implements ITooltipKey {
        AUTOMATIC_AFFIX_TRADE("automatic_affix_trade", "Random Affix Item:"),
        REFORGE_ITEM("reforge_item", "Reforge Item:"),
        TIER_GATED_TRADE("tier_gated_trade", "Tier Gated Trade:"),
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

    public enum Ingredient implements ITooltipKey {
        AFFIX_ITEM("affix_item", "Any Affix Item:"),
        GEM("gem", "Any Gem:"),
        SPAWN_EGG("spawn_egg", "Any Spawn Egg"),
        ;

        private final Translation translation;

        Ingredient(String k, String e) {
            translation = new Translation(Utils.langKey(MOD_ID, "type.ingredient", k), e);
        }

        @NotNull
        @Override
        public Translation getTranslation() {
            return translation;
        }
    }

    public enum ItemSubPredicates implements ITooltipKey {
        AFFIXED_ITEM("affixed_item", "Affixed Item"),
        ITEM_WITH_PURITY("item_with_purity", "Item With Purity:"),
        ITEM_WITH_RARITY("item_with_rarity", "Item With Rarity:"),
        SOCKETED_ITEM("socketed_item", "Socketed Item"),
        ;

        private final Translation translation;

        ItemSubPredicates(String k, String e) {
            translation = new Translation(Utils.langKey(MOD_ID, "type.item_sub_predicate", k), e);
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
            translation = new Translation(Utils.langKey(MOD_ID, "type.global_loot_modifier", k), e);
        }

        @NotNull
        @Override
        public Translation getTranslation() {
            return translation;
        }
    }

    public enum Branch implements ITooltipKey {
        PURITY("purity", "Purity:"),
        RARITY("rarity", "Rarity:"),
        STAGES("stages", "Stages:"),
        WORLD_TIERS("world_tiers", "World Tiers:"),
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
        DOMAIN("domain", "Domain: %s"),
        MAX_RARITY("max_rarity", "Max Rarity: %s"),
        MIN_RARITY("min_rarity", "Min Rarity: %s"),
        MIN_WORLD_TIER("min_world_tier", "Min World Tier: %s"),
        PURITY("purity", "Purity: %s"),
        RARITY("rarity", "Rarity: %s"),
        WORLD_TIER("world_tier", "World Tier: %s"),
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
        CoreLang.register(TRANSLATION_MAP, EntitySubPredicates.class);
        CoreLang.register(TRANSLATION_MAP, Entry.class);
        CoreLang.register(TRANSLATION_MAP, Functions.class);
        CoreLang.register(TRANSLATION_MAP, Ingredient.class);
        CoreLang.register(TRANSLATION_MAP, ItemSubPredicates.class);
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
