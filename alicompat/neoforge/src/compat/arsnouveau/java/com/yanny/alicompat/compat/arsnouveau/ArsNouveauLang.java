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

    public enum EntitySubPredicates implements ITooltipKey {
        PERCENT_HEALTH_EQUAL_OR_LOWER("percent_health_equal_or_lower", "Percent Health Equal Or Lower:"),
        ;

        private final Translation translation;

        EntitySubPredicates(String k, String e) {
            translation = new Translation("alicompat.type.entity_sub_predicate." + k, e);
        }

        @NotNull
        @Override
        public Translation getTranslation() {
            return translation;
        }
    }

    public enum Value implements ITooltipKey {
        THRESHOLD("threshold", "Threshold: %s"),
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
        CoreLang.register(TRANSLATION_MAP, EntitySubPredicates.class);
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
