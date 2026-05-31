package com.yanny.awi.language;

import com.yanny.aci.language.ITooltipKey;
import com.yanny.aci.language.Translation;
import org.jetbrains.annotations.NotNull;

public final class Lang {
    public enum FeatureConfiguration implements ITooltipKey {
        COUNT("count", "Count:"),
        ORE("ore", "Ore:"),
        ;

        private final Translation translation;

        FeatureConfiguration(String k, String e) {
            this.translation = new Translation("awi.type.feature_configuration." + k, e);
        }

        @NotNull
        @Override
        public Translation getTranslation() {
            return translation;
        }
    }
    public enum RuleTest implements ITooltipKey {
        ALWAYS_TRUE("always_true", "Always True"),
        ;

        private final Translation translation;

        RuleTest(String k, String e) {
            this.translation = new Translation("awi.type.rule_test." + k, e);
        }

        @NotNull
        @Override
        public Translation getTranslation() {
            return translation;
        }
    }

    public enum Value implements ITooltipKey {
        BLOCK("block", "Block: %s"),
        COUNT("count", "Count: %s"),
        DISCARD_CHANCE_ON_AIR_EXPOSURE("discard_chance_on_air_exposure", "Discard Chance On Air Exposure: %s"),
        SIZE("size", "Size: %s"),
        ;

        private final Translation translation;

        Value(String k, String e) {
            this.translation = new Translation("awi.property.value." + k, e);
        }

        @NotNull
        @Override
        public Translation getTranslation() {
            return translation;
        }
    }

    public enum Branch implements ITooltipKey {
        PROPERTIES("properties", "Properties:"),
        STATE("state", "State:"),
        TARGET("target", "Target:"),
        TARGET_STATES("target_states", "Target States:"),
        ;

        private final Translation translation;

        Branch(ITooltipKey s, String k, String e) {
            this.translation = new Translation(s.singular(), "awi.property.branch." + k, s.englishSingular(), e);
        }

        Branch(String k, String e) {
            this.translation = new Translation("awi.property.branch." + k, e);
        }

        @NotNull
        @Override
        public Translation getTranslation() {
            return translation;
        }
    }
}
