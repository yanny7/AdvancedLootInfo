package com.yanny.alicompat.compat.villagerconfig;

import com.yanny.aci.language.CoreLang;
import com.yanny.aci.language.ITooltipKey;
import com.yanny.aci.language.Translation;
import com.yanny.aci.number.NumberFormatter;
import com.yanny.alicompat.ICompatTranslations;
import com.yanny.alicompat.Utils;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class VillagerConfigLang implements ICompatTranslations {
    static final String MOD_ID = "villagerconfig";
    static final Identifier ENCHANTMENT_LEVEL = Identifier.fromNamespaceAndPath(Utils.MOD_ID, MOD_ID + ".enchantment_level");
    static final Identifier TREASURE_MULTIPLIER = Identifier.fromNamespaceAndPath(Utils.MOD_ID, MOD_ID + ".treasure_multiplier");

    public static final Map<String, String> TRANSLATION_MAP = new HashMap<>();

    public enum Functions implements ITooltipKey {
        ENCHANT_RANDOMLY("enchant_randomly", "Enchant Randomly:"),
        SET_DYE("set_dye", "Set Dye:"),
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

    public enum Branch implements ITooltipKey {
        DYE_COLORS("dye_colors", "Dye Colors:"),
        EXCLUDE("exclude", "Exclude:"),
        INCLUDE("include", "Include:"),
        ;

        private final Translation translation;

        Branch(String k, String e) {
            translation = new Translation(Utils.langKey(MOD_ID, "property.branch", k), e);
        }

        @NotNull
        @Override
        public Translation getTranslation() {
            return translation;
        }
    }

    public enum Value implements ITooltipKey {
        MAX_LEVEL("max_level", "Max Level: %s"),
        MIN_LEVEL("min_level", "Min Level: %s"),
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

    public enum Numbers implements ITooltipKey {
        ENCHANTMENT_LEVEL(VillagerConfigLang.ENCHANTMENT_LEVEL, "", "enchantment level"),
        ENCHANTMENT_LEVEL_DESC(VillagerConfigLang.ENCHANTMENT_LEVEL, ".desc", "enchantment level"),
        TREASURE_MULTIPLIER(VillagerConfigLang.TREASURE_MULTIPLIER, "", "treasure multiplier"),
        TREASURE_MULTIPLIER_DESC(VillagerConfigLang.TREASURE_MULTIPLIER, ".desc", "treasure multiplier"),
        ;

        private final Translation translation;

        Numbers(Identifier type, String suffix, String e) {
            translation = new Translation(NumberFormatter.varKey(type) + suffix, e);
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
        CoreLang.register(TRANSLATION_MAP, Numbers.class);
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
