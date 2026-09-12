package com.yanny.alicompat.compat.aether;

import com.yanny.aci.language.CoreLang;
import com.yanny.aci.language.ITooltipKey;
import com.yanny.aci.language.Translation;
import com.yanny.alicompat.ICompatTranslations;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class AetherLang implements ICompatTranslations {
    static final String MOD_ID = "aether";

    public static final Map<String, String> TRANSLATION_MAP = new HashMap<>();

    public enum Conditions implements ITooltipKey {
        CONFIG_ENABLED("config_enabled", "Config Enabled:"),
        ;

        private final Translation translation;

        Conditions(String k, String e) {
            translation = new Translation("alicompat.type.condition." + k, e);
        }

        @NotNull
        @Override
        public Translation getTranslation() {
            return translation;
        }
    }

    public enum Functions implements ITooltipKey {
        DOUBLE_DROPS("double_drops", "Double Drops:"),
        SPAWN_TNT("spawn_tnt", "Spawn TNT:"),
        SPAWN_XP("spawn_xp", "Spawn XP:"),
        WHIRLWIND_SPAWN_ENTITY("whirlwind_spawn_entity", "Whirlwind Spawn Entity:"),
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

    public enum Value implements ITooltipKey {
        CONFIG("config", "Config: %s"),
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
        CoreLang.register(TRANSLATION_MAP, Conditions.class);
        CoreLang.register(TRANSLATION_MAP, Functions.class);
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
