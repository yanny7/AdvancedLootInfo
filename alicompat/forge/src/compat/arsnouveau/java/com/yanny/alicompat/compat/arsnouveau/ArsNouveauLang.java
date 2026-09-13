package com.yanny.alicompat.compat.arsnouveau;

import com.yanny.aci.language.CoreLang;
import com.yanny.aci.language.ITooltipKey;
import com.yanny.aci.language.Translation;
import com.yanny.alicompat.ICompatTranslations;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class ArsNouveauLang implements ICompatTranslations {
    static final String MOD_ID = "ars_nouveau";

    public static final Map<String, String> TRANSLATION_MAP = new HashMap<>();

    public enum Ingredient implements ITooltipKey {
        POTION("potion", "Potion Ingredient:"),
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
