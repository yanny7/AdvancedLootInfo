package com.yanny.alicompat.compat.spellengine;

import com.yanny.aci.language.CoreLang;
import com.yanny.aci.language.ITooltipKey;
import com.yanny.aci.language.Translation;
import com.yanny.aci.tooltip.CoreTooltipUtils;
import com.yanny.alicompat.ICompatTranslations;
import com.yanny.alicompat.Utils;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class SpellEngineLang implements ICompatTranslations {
    static final String MOD_ID = "spell_engine";
    static final String WEIGHT_OPERATION_OWNER = MOD_ID + ".weight_operation";

    public static final Map<String, String> TRANSLATION_MAP = new HashMap<>();

    public enum Entry implements ITooltipKey {
        AFFILIATION_GROUP("affiliation_group", "Affiliation Group:"),
        INLINE_POOL("inline_pool", "Inline Pool:"),
        ;

        private final Translation translation;

        Entry(String k, String e) {
            translation = new Translation(Utils.langKey(MOD_ID, "type.entry", k), e);
        }

        @NotNull
        @Override
        public Translation getTranslation() {
            return translation;
        }
    }

    public enum Functions implements ITooltipKey {
        SPELL_BIND_RANDOMLY("spell_bind_randomly", "Spell Bind Randomly:"),
        ;

        private final Translation translation;

        Functions(String k, String e) {
            translation = new Translation(Utils.langKey(MOD_ID, "type.function", k), e);
        }

        @NotNull
        @Override
        public Translation getTranslation() {
            return translation;
        }
    }

    public enum Value implements ITooltipKey {
        EXTRA_WEIGHT("extra_weight", "Class Affiliation Extra Weight: %s"),
        INCLUDE_TEAM("include_team", "Include Team: %s"),
        TIER("tier", "Tier: %s"),
        SPELL_POOL("spell_pool", "Spell Pool: %s"),
        ;

        private final Translation translation;

        Value(String k, String e) {
            translation = new Translation(Utils.langKey(MOD_ID, "property.value", k), e);
        }

        @NotNull
        @Override
        public Translation getTranslation() {
            return translation;
        }
    }

    static {
        CoreLang.register(TRANSLATION_MAP, Entry.class);
        CoreLang.register(TRANSLATION_MAP, Functions.class);
        CoreLang.register(TRANSLATION_MAP, Value.class);
        TRANSLATION_MAP.put(CoreTooltipUtils.enumKey(Utils.MOD_ID, WEIGHT_OPERATION_OWNER, "MULTIPLY"), "Multiply");
        TRANSLATION_MAP.put(CoreTooltipUtils.enumKey(Utils.MOD_ID, WEIGHT_OPERATION_OWNER, "ADD"), "Add");
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
