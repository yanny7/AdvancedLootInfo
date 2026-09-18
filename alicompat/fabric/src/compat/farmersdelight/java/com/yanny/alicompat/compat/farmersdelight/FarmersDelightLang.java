package com.yanny.alicompat.compat.farmersdelight;

import com.yanny.aci.language.CoreLang;
import com.yanny.aci.language.ITooltipKey;
import com.yanny.aci.language.Translation;
import com.yanny.aci.tooltip.CoreTooltipUtils;
import com.yanny.alicompat.ICompatTranslations;
import com.yanny.alicompat.Utils;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class FarmersDelightLang implements ICompatTranslations {
    static final String MOD_ID = "farmersdelight";
    static final String ITEM_ABILITY = "farmersdelight.item_ability";

    public static final Map<String, String> TRANSLATION_MAP = new HashMap<>();

    public enum ConsumeEffects implements ITooltipKey {
        EXTINGUISH("extinguish", "Extinguish"),
        HEAL("heal", "Heal:"),
        REMOVE_RANDOM_EFFECTS("remove_random_effects", "Remove Random Effects:"),
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

    public enum Functions implements ITooltipKey {
        COPY_SKILLET("copy_skillet", "Copy Skillet:"),
        SMOKER_COOK("smoker_cook", "Smoker Cook:"),
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
        HARMFUL_ONLY("harmful_only", "Harmful Only: %s"),
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
        CoreLang.register(TRANSLATION_MAP, ConsumeEffects.class);
        CoreLang.register(TRANSLATION_MAP, Functions.class);
        CoreLang.register(TRANSLATION_MAP, Value.class);

        putItemAbility("SWORD_DIG", "Sword Dig");
        putItemAbility("SHOVEL_DIG", "Shovel Dig");
        putItemAbility("PICKAXE_DIG", "Pickaxe Dig");
        putItemAbility("SHEARS_CARVE", "Shears Carve");
        putItemAbility("SHEARS_DIG", "Shears Dig");
        putItemAbility("SHEARS_HARVEST", "Shears Harvest");
        putItemAbility("AXE_DIG", "Axe Dig");
        putItemAbility("AXE_STRIP", "Axe Strip");
        putItemAbility("HOE_DIG", "Hoe Dig");
        putItemAbility("KNIFE_DIG", "Knife Dig");
        putItemAbility("KNIFE_HARVEST", "Knife Harvest");
    }

    private static void putItemAbility(String constant, String english) {
        TRANSLATION_MAP.put(CoreTooltipUtils.enumKey(Utils.MOD_ID, ITEM_ABILITY, constant), english);
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
