package com.yanny.alicompat.compat.immersiveengineering;

import com.yanny.aci.language.CoreLang;
import com.yanny.aci.language.ITooltipKey;
import com.yanny.aci.language.Translation;
import com.yanny.alicompat.ICompatTranslations;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class ImmersiveEngineeringLang implements ICompatTranslations {
    static final String MOD_ID = "immersiveengineering";

    public static final Map<String, String> TRANSLATION_MAP = new HashMap<>();

    public enum Entry implements ITooltipKey {
        DROP_INVENTORY("drop_inventory", "Drop Inventory:"),
        MULTIBLOCK_DROPS("multiblock_drops", "Multiblock Drops:"),
        TILE_DROP("tile_drop", "Block Entity Drop:"),
        ;

        private final Translation translation;

        Entry(String k, String e) {
            translation = new Translation("alicompat.type.entry." + k, e);
        }

        @NotNull
        @Override
        public Translation getTranslation() {
            return translation;
        }
    }

    public enum Functions implements ITooltipKey {
        CONVEYOR_COVER("conveyor_cover", "Conveyor Cover:"),
        PROPERTY_COUNT("property_count", "Property Count:"),
        REVOLVERPERK("revolverperk", "Revolver Perks:"),
        SECRET_BLUPRINTZ("secret_bluprintz", "Secret Bluprintz:"),
        WINDMILL("windmill", "Windmill Sails:"),
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
        ALTERNATIVE("alternative", "Alternative: %s"),
        ;

        private final Translation translation;

        Value(String k, String e) {
            this.translation = new Translation("alicompat.property.value." + k, e);
        }

        @NotNull
        @Override
        public Translation getTranslation() {
            return translation;
        }
    }

    public enum Branch implements ITooltipKey {
        ALTERNATIVE(Value.ALTERNATIVE, "alternative", "Alternative:"),
        ;

        private final Translation translation;

        Branch(ITooltipKey s, String k, String e) {
            this.translation = new Translation(s.singular(), "alicompat.property.branch." + k, s.englishSingular(), e);
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
        CoreLang.register(TRANSLATION_MAP, Branch.class);
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
