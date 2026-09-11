package com.yanny.alicompat.compat.supplementaries;

import com.yanny.aci.language.CoreLang;
import com.yanny.aci.language.ITooltipKey;
import com.yanny.aci.language.Translation;
import com.yanny.alicompat.ICompatTranslations;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class SupplementariesLang implements ICompatTranslations {
    static final String MOD_ID = "supplementaries";

    public static final Map<String, String> TRANSLATION_MAP = new HashMap<>();

    public enum Functions implements ITooltipKey {
        CURSE_LOOT("curse_loot", "Curse Loot:"),
        RANDOM_ARROWS("random_arrows", "Random Arrows:"),
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

    public enum Value implements ITooltipKey {
        FIREWORK_STARS("firework_stars", "Firework Stars: %s"),
        FLIGHT_DURATION("flight_duration", "Flight Duration: %s"),
        RANDOM_FIREWORK("random_firework", "Random Firework:"),
        RANDOM_FIREWORK_STAR("random_firework_star", "Random Firework Star"),
        ;

        private final Translation translation;

        Value(String k, String e) {
            translation = new Translation("alicompat.property.value." + k, e);
        }

        @NotNull
        @Override
        public Translation getTranslation() {
            return translation;
        }
    }

    static {
        CoreLang.register(TRANSLATION_MAP, Functions.class);
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
