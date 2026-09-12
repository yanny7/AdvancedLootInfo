package com.yanny.alicompat.compat.moonlight;

import com.yanny.aci.language.CoreLang;
import com.yanny.aci.language.ITooltipKey;
import com.yanny.aci.language.Translation;
import com.yanny.alicompat.ICompatTranslations;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class MoonlightLang implements ICompatTranslations {
    static final String MOD_ID = "moonlight";

    public static final Map<String, String> TRANSLATION_MAP = new HashMap<>();

    public enum Conditions implements ITooltipKey {
        DATA_CONDITIONS("data_conditions", "Data Conditions:"),
        LOOT_TABLE_ID_PATTERN("loot_table_id_pattern", "Loot Table Id Pattern:"),
        OPTIONAL_BLOCK_STATE_PROPERTY("optional_block_state_property", "Optional Block State Property:"),
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
        OPTIONAL_ITEM("optional_item", "Optional Item:"),
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

    static {
        CoreLang.register(TRANSLATION_MAP, Conditions.class);
        CoreLang.register(TRANSLATION_MAP, Entry.class);
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
