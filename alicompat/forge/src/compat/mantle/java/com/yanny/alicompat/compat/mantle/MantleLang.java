package com.yanny.alicompat.compat.mantle;

import com.yanny.aci.language.CoreLang;
import com.yanny.aci.language.ITooltipKey;
import com.yanny.aci.language.Translation;
import com.yanny.alicompat.ICompatTranslations;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class MantleLang implements ICompatTranslations {
    static final String MOD_ID = "mantle";

    public static final Map<String, String> TRANSLATION_MAP = new HashMap<>();

    public enum Branch implements ITooltipKey {
        CONTAINER("container", "Container:"),
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

    public enum Conditions implements ITooltipKey {
        BLOCK_TAG("block_tag", "Block Tag:"),
        HAS_LOOT_CONTEXT_SET("has_loot_context_set", "Has Loot Context Set:"),
        TAG_EMPTY("tag_empty", "Tag Empty:"),
        TAG_FILLED("tag_filled", "Tag Filled:"),
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
        TAG_PREFERENCE("tag_preference", "Tag Preference:"),
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

    public enum Functions implements ITooltipKey {
        RETEXTURED("retextured", "Retextured:"),
        SET_FLUID("set_fluid", "Set Fluid:"),
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

    public enum Ingredient implements ITooltipKey {
        FLUID_CONTAINER("fluid_container", "Fluid Container:"),
        ITEM_NAME("item_name", "Item Name:"),
        NBT_NAME("nbt_name", "Item With NBT:"),
        POTION("potion", "Potion Ingredient:"),
        POTION_DISPLAY("potion_display", "Any Potion:"),
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

    static {
        CoreLang.register(TRANSLATION_MAP, Branch.class);
        CoreLang.register(TRANSLATION_MAP, Conditions.class);
        CoreLang.register(TRANSLATION_MAP, Entry.class);
        CoreLang.register(TRANSLATION_MAP, Functions.class);
        CoreLang.register(TRANSLATION_MAP, Ingredient.class);
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
