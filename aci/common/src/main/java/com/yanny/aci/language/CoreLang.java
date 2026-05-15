package com.yanny.aci.language;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public final class CoreLang {
    public static final Map<String, String> TRANSLATION_MAP = new HashMap<>();

    public enum Utils implements ITooltipKey {
        AUTO_DETECTED("auto_detected", "Auto-detected: %s"),
        NOT_IMPLEMENTED("missing", "Not implemented: %s"),
        REMOVED("removed", "REMOVED"),
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
        register(Utils.class);
    }

    public static void register(Class<? extends ITooltipKey> enumClass) {
        for (ITooltipKey entry : enumClass.getEnumConstants()) {
            TRANSLATION_MAP.put(entry.singular(), entry.englishSingular());
            TRANSLATION_MAP.put(entry.plural(), entry.englishPlural());
        }
    }
}
