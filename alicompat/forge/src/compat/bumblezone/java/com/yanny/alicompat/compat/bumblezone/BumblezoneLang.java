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

    static {
        CoreLang.register(TRANSLATION_MAP, Conditions.class);
        CoreLang.register(TRANSLATION_MAP, Functions.class);
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
