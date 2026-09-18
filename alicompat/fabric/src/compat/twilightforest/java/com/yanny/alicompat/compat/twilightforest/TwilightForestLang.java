package com.yanny.alicompat.compat.twilightforest;

import com.yanny.aci.language.CoreLang;
import com.yanny.aci.language.ITooltipKey;
import com.yanny.aci.language.Translation;
import com.yanny.alicompat.ICompatTranslations;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class TwilightForestLang implements ICompatTranslations {
    static final String MOD_ID = "twilightforest";

    public static final Map<String, String> TRANSLATION_MAP = new HashMap<>();

    public enum Conditions implements ITooltipKey {
        GIANT_PICK_USED("giant_pick_used", "Giant Pick Used:"),
        IS_MINION("is_minion", "Is Minion:"),
        MOD_EXISTS("mod_exists", "Mod Exists:"),
        UNCRAFTING_TABLE_ENABLED("uncrafting_table_enabled", "Uncrafting Table Enabled"),
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
        MULTIPLAYER_ADDITION("multiplayer_addition", "Multiplayer Based Addition:"),
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

    public enum ItemSubPredicates implements ITooltipKey {
        ITEM_COLOR("item_color", "Item Color: %s"),
        ;

        private final Translation translation;

        ItemSubPredicates(String k, String e) {
            translation = new Translation("alicompat.type.item_sub_predicate." + k, e);
        }

        @NotNull
        @Override
        public Translation getTranslation() {
            return translation;
        }
    }

    public enum Value implements ITooltipKey {
        EXTRA_COUNT_PER_PLAYER("extra_count_per_player", "Extra Count Per Player: %s"),
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
        CoreLang.register(TRANSLATION_MAP, ItemSubPredicates.class);
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
