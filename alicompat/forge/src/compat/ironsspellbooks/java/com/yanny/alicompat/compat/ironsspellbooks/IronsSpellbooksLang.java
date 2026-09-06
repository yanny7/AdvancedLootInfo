package com.yanny.alicompat.compat.ironsspellbooks;

import com.yanny.aci.language.CoreLang;
import com.yanny.aci.language.ITooltipKey;
import com.yanny.aci.language.Translation;
import com.yanny.alicompat.ICompatTranslations;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class IronsSpellbooksLang implements ICompatTranslations {
    static final String MOD_ID = "irons_spellbooks";

    public static final Map<String, String> TRANSLATION_MAP = new HashMap<>();

    public enum Functions implements ITooltipKey {
        RANDOMIZE_RING_ENHANCEMENT("randomize_ring_enhancement", "Randomize Ring Enhancement:"),
        RANDOMIZE_SPELL("randomize_spell", "Randomize Spell:"),
        RANDOM_POTION("random_potion", "Random Potion"),
        RANDOM_SPELL_SCROLL("random_spell_scroll", "Random Spell Scroll:"),
        SET_FURLED_MAP("set_furled_map", "Set Furled Map:"),
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

    public enum Branch implements ITooltipKey {
        ALTERNATIVE("alternative", "Alternative:"),
        APPLICABLE_SPELLS("applicable_spells", "Applicable Spells:"),
        SPELLS("spells", "Spells:"),
        SPELL_FILTER("spell_filter", "Spell Filter:"),
        ;

        private final Translation translation;

        Branch(String k, String e) {
            translation = new Translation("alicompat.property.branch." + k, e);
        }

        @NotNull
        @Override
        public Translation getTranslation() {
            return translation;
        }
    }

    public enum Value implements ITooltipKey {
        FORCE("force", "Force: %s"),
        SCHOOL("school", "School: %s"),
        SPELL("spell", "Spell: %s"),
        SPELL_LEVEL("spell_level", "Spell Level: %s"),
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
        CoreLang.register(TRANSLATION_MAP, Branch.class);
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
