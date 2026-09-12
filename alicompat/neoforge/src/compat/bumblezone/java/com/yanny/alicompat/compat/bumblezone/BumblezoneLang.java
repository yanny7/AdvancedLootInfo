package com.yanny.alicompat.compat.bumblezone;

import com.yanny.aci.language.CoreLang;
import com.yanny.aci.language.ITooltipKey;
import com.yanny.aci.language.Translation;
import com.yanny.alicompat.ICompatTranslations;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class BumblezoneLang implements ICompatTranslations {
    static final String MOD_ID = "the_bumblezone";

    public static final Map<String, String> TRANSLATION_MAP = new HashMap<>();

    public enum Conditions implements ITooltipKey {
        ESSENCE_ONLY_SPAWN("essence_only_spawn", "Player carries an Essence of the Bees"),
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
        DROP_CONTAINER_ITEMS("drop_container_items", "Drop Container Items:"),
        HONEY_COMPASS_LOCATE_STRUCTURE("honey_compass_locate_structure", "Honey Compass Locate Structure:"),
        TAG_ITEM_REMOVALS("tag_item_removals", "Tag Item Removals:"),
        UNIQUIFY_IF_HAS_ITEMS("uniquify_if_has_items", "Uniquify If Has Items:"),
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

    public enum EntitySubPredicates implements ITooltipKey {
        HONEY_SLIME("honey_slime", "Honey Slime:"),
        ;

        private final Translation translation;

        EntitySubPredicates(String k, String e) {
            translation = new Translation("alicompat.type.entity_sub_predicate." + k, e);
        }

        @NotNull
        @Override
        public Translation getTranslation() {
            return translation;
        }
    }

    public enum Value implements ITooltipKey {
        IS_FAILED("is_failed", "Is Failed: %s"),
        IS_LOADING("is_loading", "Is Loading: %s"),
        LOCATED_SPECIAL_STRUCTURE("located_special_structure", "Located Special Structure: %s"),
        LOCKED("locked", "Locked: %s"),
        SEARCH_ID("search_id", "Search Id: %s"),
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
        CoreLang.register(TRANSLATION_MAP, EntitySubPredicates.class);
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
