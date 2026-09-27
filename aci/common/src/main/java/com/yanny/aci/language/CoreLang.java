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

    static {
        register(TRANSLATION_MAP, Utils.class);
        register(TRANSLATION_MAP, Spawn.class);
    }

    public static void register(Map<String, String> translationMap, Class<? extends ITooltipKey> enumClass) {
        for (ITooltipKey entry : enumClass.getEnumConstants()) {
            translationMap.put(entry.singular(), entry.englishSingular());
            translationMap.put(entry.plural(), entry.englishPlural());
        }
    }
}
