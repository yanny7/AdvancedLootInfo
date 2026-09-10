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

    public enum Conditions implements ITooltipKey {
        BLOCK_TAG("block_tag", "Block Tag:"),
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

    static {
        CoreLang.register(TRANSLATION_MAP, Conditions.class);
        CoreLang.register(TRANSLATION_MAP, Entry.class);
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
