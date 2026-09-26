package com.yanny.alicompat.compat.grimoireofgaia;

import com.yanny.aci.language.CoreLang;
import com.yanny.aci.language.ITooltipKey;
import com.yanny.aci.language.Translation;
import com.yanny.alicompat.ICompatTranslations;
import com.yanny.alicompat.Utils;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class GrimoireOfGaiaLang implements ICompatTranslations {
    static final String MOD_ID = "grimoireofgaia";

    public static final Map<String, String> TRANSLATION_MAP = new HashMap<>();

    public enum ConsumeEffects implements ITooltipKey {
        CLEAR_NEGATIVE_STATUS_EFFECTS("clear_negative_status_effects", "Clear Negative Effects"),
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

    static {
        CoreLang.register(TRANSLATION_MAP, ConsumeEffects.class);
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
