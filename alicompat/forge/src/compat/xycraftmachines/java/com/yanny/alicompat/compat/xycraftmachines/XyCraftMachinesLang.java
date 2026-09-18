package com.yanny.alicompat.compat.xycraftmachines;

import com.yanny.aci.language.CoreLang;
import com.yanny.aci.language.ITooltipKey;
import com.yanny.aci.language.Translation;
import com.yanny.alicompat.ICompatTranslations;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class XyCraftMachinesLang implements ICompatTranslations {
    static final String MOD_ID = "xycraft_machines";

    public static final Map<String, String> TRANSLATION_MAP = new HashMap<>();

    public enum Ingredient implements ITooltipKey {
        BLOCK_STATE("block_state", "Block State:"),
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
