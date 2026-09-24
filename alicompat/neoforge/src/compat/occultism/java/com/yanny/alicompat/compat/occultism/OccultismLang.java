package com.yanny.alicompat.compat.occultism;

import com.yanny.aci.language.CoreLang;
import com.yanny.aci.language.ITooltipKey;
import com.yanny.aci.language.Translation;
import com.yanny.alicompat.ICompatTranslations;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class OccultismLang implements ICompatTranslations {
    static final String MOD_ID = "occultism";

    public static final Map<String, String> TRANSLATION_MAP = new HashMap<>();

    public enum ConsumeEffects implements ITooltipKey {
        DAMAGE_ITEM("damage_item", "Damage Item:"),
        ;

        private final Translation translation;

        ConsumeEffects(String k, String e) {
            translation = new Translation("alicompat.type.consume_effect." + k, e);
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
