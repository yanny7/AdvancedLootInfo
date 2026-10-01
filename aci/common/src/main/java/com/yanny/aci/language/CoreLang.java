package com.yanny.aci.language;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public final class CoreLang {
    public static final Map<String, String> TRANSLATION_MAP = new HashMap<>();

    public enum Utils implements ITooltipKey {
        AUTO_DETECTED("auto_detected", "Auto-detected: %s"),
        ENTRY("entry", "Entry:"),
        TAG("tag", "Tag: %s"),
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

    public enum Spawn implements ITooltipKey {
        SPAWNS("spawns", "Spawns:"),
        DIMENSION("dimension", "Dimension: %s"),
        STRUCTURE("structure", "Structure: %s"),
        BIOME_INCLUDED("biome_included", "+ %s"),
        BIOME_EXCLUDED("biome_excluded", "- %s"),
        CATEGORY("category", "Category: %s"),
        WEIGHT("weight", "Weight: %s"),
        GROUP_SIZE("group_size", "Group size: %s"),
        SPAWN_COST("spawn_cost", "Spawn cost: charge %s, budget %s"),
        ;

        private final Translation translation;

        Spawn(String k, String e) {
            translation = new Translation("aci.spawn." + k, e);
        }

        @NotNull
        @Override
        public Translation getTranslation() {
            return translation;
        }
    }

    public enum Numbers implements ITooltipKey {
        RANGE("range", "%s to %s"),
        AT_LEAST("at_least", "≥ %s"),
        AT_MOST("at_most", "≤ %s"),
        ANY("any", "any"),
        UNKNOWN("unknown", "?"),
        PERCENT("percent", "%s%%"),
        MODE("mode", "~%s (%s)"),
        MODE_PEAK("mode_peak", "~%s"),
        DEPENDS("depends", "(%s)"),
        LEVEL_ROW("level_row", "%s %s: %s"),
        WEIGHTED_ENTRY("weighted_entry", "%s (%s)"),
        OTHERWISE("otherwise", "otherwise %s"),
        FN_MOD("fn.mod", "mod"),
        FN_FLOOR_MOD("fn.floor_mod", "floorMod"),
        FN_FLOOR_DIV("fn.floor_div", "floorDiv"),
        FN_POW("fn.pow", "pow"),
        FN_MIN("fn.min", "min"),
        FN_MAX("fn.max", "max"),
        FN_ABS("fn.abs", "abs"),
        FN_FLOOR("fn.floor", "floor"),
        FN_CEIL("fn.ceil", "ceil"),
        FN_ROUND("fn.round", "round"),
        FN_TRUNC("fn.trunc", "trunc"),
        FN_SQRT("fn.sqrt", "sqrt"),
        FN_SIN("fn.sin", "sin"),
        FN_COS("fn.cos", "cos"),
        FN_LENGTH("fn.length", "length"),
        FN_CLAMP("fn.clamp", "clamp"),
        FN_AVG("fn.avg", "avg"),
        FN_UNIFORM_INT("fn.uniform_int", "U"),
        FN_UNIFORM_FLOAT("fn.uniform_float", "U"),
        FN_BINOMIAL("fn.binomial", "binom"),
        FN_NORMAL("fn.normal", "normal"),
        FN_BIASED_TO_BOTTOM("fn.biased_to_bottom", "biased"),
        FN_TRAPEZOID_FLOAT("fn.trapezoid_float", "trapezoid"),
        VAR_LEVEL("var.level", "LVL"),
        VAR_SCORE("var.score", "score(%s; %s)"),
        VAR_SCORE_DESC("var.score.desc", "score \"%2$s\" (%1$s)"),
        TARGET_THIS("target.this", "this"),
        TARGET_KILLER("target.killer", "killer"),
        TARGET_DIRECT_KILLER("target.direct_killer", "direct_killer"),
        TARGET_KILLER_PLAYER("target.killer_player", "killer_player"),
        TARGET_TARGET_ENTITY("target.target_entity", "target_entity"),
        TARGET_INTERACTING_ENTITY("target.interacting_entity", "interacting_entity"),
        ;

        public static final String PREFIX = "aci.number.";

        private final Translation translation;

        Numbers(String k, String e) {
            translation = new Translation(PREFIX + k, e);
        }

        @NotNull
        @Override
        public Translation getTranslation() {
            return translation;
        }
    }

    static {
        register(TRANSLATION_MAP, Utils.class);
        register(TRANSLATION_MAP, Spawn.class);
        register(TRANSLATION_MAP, Numbers.class);
    }

    public static void register(Map<String, String> translationMap, Class<? extends ITooltipKey> enumClass) {
        for (ITooltipKey entry : enumClass.getEnumConstants()) {
            translationMap.put(entry.singular(), entry.englishSingular());
            translationMap.put(entry.plural(), entry.englishPlural());
        }
    }
}
