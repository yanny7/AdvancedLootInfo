package com.yanny.alicompat.compat.rats;

import com.yanny.aci.language.CoreLang;
import com.yanny.aci.language.ITooltipKey;
import com.yanny.aci.language.Translation;
import com.yanny.alicompat.ICompatTranslations;
import com.yanny.alicompat.Utils;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class RatsLang implements ICompatTranslations {
    static final String MOD_ID = "rats";

    public static final Map<String, String> TRANSLATION_MAP = new HashMap<>();

    public enum Conditions implements ITooltipKey {
        HAS_TOGA_AND_IN_RATLANTIS("has_toga_and_in_ratlantis", "Rat has toga in Ratlantis"),
        KILLER_HAS_UPGRADE("killer_has_upgrade", "Killer Rat Has Upgrade:"),
        RAT_HAS_PLAGUE("rat_has_plague", "Rat has plague"),
        RATLANTIS_LOADED("ratlantis_loaded", "Ratlantis loaded"),
        ;

        private final Translation translation;

        Conditions(String k, String e) {
            translation = new Translation(Utils.langKey(MOD_ID, "type.condition", k), e);
        }

        @NotNull
        @Override
        public Translation getTranslation() {
            return translation;
        }
    }

    static {
        CoreLang.register(TRANSLATION_MAP, Conditions.class);
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
