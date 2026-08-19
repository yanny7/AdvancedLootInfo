package com.yanny.aci.language;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public final class CoreLang {
    public static final Map<String, String> TRANSLATION_MAP = new HashMap<>();

    public enum Utils implements ITooltipKey {
        AUTO_DETECTED("auto_detected", "Auto-detected: %s"),
        ENTRY("entry", "Entry:"),
        TAG("tag", "Tag: %s"),
        NOT_IMPLEMENTED("missing", "Not implemented: %s"),
        ;

        private final Translation translation;

        Utils(String k, String e) {
            translation = new Translation("aci.util." + k, e);
        }

        @NotNull
        @Override
        public Translation getTranslation() {
            return translation;
        }
    }

    static {
        register(TRANSLATION_MAP, Utils.class);
    }

    public static void register(Map<String, String> translationMap, Class<? extends ITooltipKey> enumClass) {
        for (ITooltipKey entry : enumClass.getEnumConstants()) {
            translationMap.put(entry.singular(), entry.englishSingular());
            translationMap.put(entry.plural(), entry.englishPlural());
        }
    }
}
