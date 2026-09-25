package com.yanny.alicompat.compat.enderio;

import com.yanny.aci.language.CoreLang;
import com.yanny.aci.language.ITooltipKey;
import com.yanny.aci.language.Translation;
import com.yanny.alicompat.ICompatTranslations;
import com.yanny.alicompat.Utils;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class EnderIoLang implements ICompatTranslations {
    static final String MOD_ID = "enderio";

    public static final Map<String, String> TRANSLATION_MAP = new HashMap<>();

    public enum Functions implements ITooltipKey {
        COPY_PAINT("copy_paint", "Copy Paint:"),
        SET_LOOT_CAPACITOR("set_loot_capacitor", "Set Loot Capacitor:"),
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
        ANY_SOUL_STORAGE("any_soul_storage", "Soul Storage (Empty Or Filled):"),
        CONDUIT("conduit", "Conduit:"),
        EMPTY_SOUL_STORAGE("empty_soul_storage", "Empty Soul Storage:"),
        FILLED_SOUL_STORAGE("filled_soul_storage", "Filled Soul Storage:"),
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

    public enum Value implements ITooltipKey {
        COPY_PRIMARY("copy_primary", "Copy Primary: %s"),
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
        CoreLang.register(TRANSLATION_MAP, Ingredient.class);
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
