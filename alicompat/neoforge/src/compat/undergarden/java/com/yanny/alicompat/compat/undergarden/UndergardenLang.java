package com.yanny.alicompat.compat.undergarden;

import com.yanny.aci.language.CoreLang;
import com.yanny.aci.language.ITooltipKey;
import com.yanny.aci.language.Translation;
import com.yanny.alicompat.ICompatTranslations;
import com.yanny.alicompat.Utils;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class UndergardenLang implements ICompatTranslations {
    static final String MOD_ID = "undergarden";

    public static final Map<String, String> TRANSLATION_MAP = new HashMap<>();

    public enum ConsumeEffects implements ITooltipKey {
        MODIFY_UTHERIC_INFECTION("modify_utheric_infection", "Modify Utheric Infection:"),
        ;

        private final Translation translation;

        ConsumeEffects(String k, String e) {
            translation = new Translation(Utils.langKey(MOD_ID, "type.consume_effect", k), e);
        }

        @NotNull
        @Override
        public Translation getTranslation() {
            return translation;
        }
    }

    public enum ItemSubPredicates implements ITooltipKey {
        INFECTION_CONSUME_EFFECT("infection_consume_effect", "Infection Consume Effect:"),
        ;

        private final Translation translation;

        ItemSubPredicates(String k, String e) {
            translation = new Translation(Utils.langKey(MOD_ID, "type.item_sub_predicate", k), e);
        }

        @NotNull
        @Override
        public Translation getTranslation() {
            return translation;
        }
    }

    static {
        CoreLang.register(TRANSLATION_MAP, ConsumeEffects.class);
        CoreLang.register(TRANSLATION_MAP, ItemSubPredicates.class);
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
