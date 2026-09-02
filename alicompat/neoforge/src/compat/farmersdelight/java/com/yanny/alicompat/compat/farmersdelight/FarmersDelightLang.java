package com.yanny.alicompat.compat.farmersdelight;

import com.yanny.aci.language.CoreLang;
import com.yanny.aci.language.ITooltipKey;
import com.yanny.aci.language.Translation;
import com.yanny.alicompat.ICompatTranslations;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class FarmersDelightLang implements ICompatTranslations {
    static final String MOD_ID = "farmersdelight";

    public static final Map<String, String> TRANSLATION_MAP = new HashMap<>();

    public enum Functions implements ITooltipKey {
        COPY_SKILLET("copy_skillet", "Copy Skillet"),
        SMOKER_COOK("smoker_cook", "Smoker Cook"),
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
