package com.yanny.alicompat.compat.snowrealmagic;

import com.yanny.aci.language.CoreLang;
import com.yanny.aci.language.ITooltipKey;
import com.yanny.aci.language.Translation;
import com.yanny.alicompat.ICompatTranslations;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class SnowRealMagicLang implements ICompatTranslations {
    static final String MOD_ID = "snowrealmagic";

    public static final Map<String, String> TRANSLATION_MAP = new HashMap<>();

    public enum Entry implements ITooltipKey {
        NORMALIZE("normalize", "Covered Block Drops:"),
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
