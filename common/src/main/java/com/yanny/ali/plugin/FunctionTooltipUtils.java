package com.yanny.ali.plugin;

import com.yanny.ali.api.ILootFunction;
import com.yanny.ali.api.IUtils;
import com.yanny.ali.plugin.function.*;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.LinkedList;
import java.util.List;

import static com.yanny.ali.plugin.GenericTooltipUtils.pad;
import static com.yanny.ali.plugin.GenericTooltipUtils.translatable;

public class FunctionTooltipUtils {
    @NotNull
    public static List<Component> getApplyBonusTooltip(IUtils utils, int pad, ILootFunction function) {
        List<Component> components = new LinkedList<>();
        ApplyBonusAliFunction fun = (ApplyBonusAliFunction) function;

        components.add(pad(pad, translatable("ali.type.function.apply_bonus")));
        components.addAll(GenericTooltipUtils.getEnchantmentTooltip(pad + 1, fun.enchantment));
        components.addAll(GenericTooltipUtils.getFormulaTooltip(pad + 1, fun.formula));

        return components;
    }

    @NotNull
    public static List<Component> getCopyNameTooltip(IUtils utils, int pad, ILootFunction function) {
        List<Component> components = new LinkedList<>();
        CopyNameAliFunction fun = (CopyNameAliFunction) function;

        components.add(pad(pad, translatable("ali.type.function.copy_name")));
        components.addAll(GenericTooltipUtils.getNameSourceTooltip(pad + 1, fun.source));

        return components;
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getCopyNbtTooltip(IUtils utils, int pad, ILootFunction function) {
        return List.of(pad(pad, translatable("ali.type.function.copy_nbt")));
    }

    @NotNull
    public static List<Component> getCopyStateTooltip(IUtils utils, int pad, ILootFunction function) {
        List<Component> components = new LinkedList<>();
        CopyStateAliFunction fun = (CopyStateAliFunction) function;

        components.add(pad(pad, translatable("ali.type.function.copy_state")));
        components.addAll(GenericTooltipUtils.getBlockTooltip(pad + 1, fun.block));
        components.addAll(GenericTooltipUtils.getPropertiesTooltip(pad + 1, fun.properties));

        return components;
    }

    @NotNull
    public static List<Component> getEnchantRandomlyTooltip(IUtils utils, int pad, ILootFunction function) {
        List<Component> components = new LinkedList<>();
        EnchantRandomlyAliFunction fun = (EnchantRandomlyAliFunction) function;

        components.add(pad(pad, translatable("ali.type.function.enchant_randomly")));
        components.addAll(GenericTooltipUtils.getEnchantmentsTooltip(pad + 1, fun.enchantments));

        return components;
    }

    @NotNull
    public static List<Component> getEnchantWithLevelsTooltip(IUtils utils, int pad, ILootFunction function) {
        List<Component> components = new LinkedList<>();
        EnchantWithLevelsAliFunction fun = (EnchantWithLevelsAliFunction) function;

        components.add(pad(pad, translatable("ali.type.function.enchant_with_levels")));
        components.addAll(GenericTooltipUtils.getRangeValueTooltip(pad + 1, "ali.property.value.levels", fun.levels));
        components.addAll(GenericTooltipUtils.getBooleanTooltip(pad + 1, "ali.property.value.treasure", fun.treasure));

        return components;
    }

    @NotNull
    public static List<Component> getExplorationMapTooltip(IUtils utils, int pad, ILootFunction function) {
        List<Component> components = new LinkedList<>();
        ExplorationMapAliFunction fun = (ExplorationMapAliFunction) function;

        components.add(pad(pad, translatable("ali.type.function.exploration_map")));
        components.addAll(GenericTooltipUtils.getTagKeyTooltip(pad + 1, "ali.property.value.destination", fun.structure));
        components.addAll(GenericTooltipUtils.getEnumTooltip(pad + 1, "ali.property.value.map_decoration", fun.mapDecoration));
        components.addAll(GenericTooltipUtils.getIntegerTooltip(pad + 1, "ali.property.value.zoom", fun.zoom));
        components.addAll(GenericTooltipUtils.getIntegerTooltip(pad + 1, "ali.property.value.search_radius", fun.searchRadius));
        components.addAll(GenericTooltipUtils.getBooleanTooltip(pad + 1, "ali.property.value.skip_known_structures", fun.skipKnownStructures));

        return components;
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getExplosionDecayTooltip(IUtils utils, int pad, ILootFunction function) {
        return List.of(pad(pad, translatable("ali.type.function.explosion_decay")));
    }

    @NotNull
    public static List<Component> getFillPlayerHeadTooltip(IUtils utils, int pad, ILootFunction function) {
        List<Component> components = new LinkedList<>();
        FillPlayerHeadAliFunction fun = (FillPlayerHeadAliFunction) function;

        components.add(pad(pad, translatable("ali.type.function.fill_player_head")));
        components.addAll(GenericTooltipUtils.getEnumTooltip(pad + 1, "ali.property.value.target", "target", fun.entityTarget));

        return components;
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getFurnaceSmeltTooltip(IUtils utils, int pad, ILootFunction function) {
        return List.of(pad(pad, translatable("ali.type.function.furnace_smelt")));
    }

    @NotNull
    public static List<Component> getLimitCountTooltip(IUtils utils, int pad, ILootFunction function) {
        List<Component> components = new LinkedList<>();
        LimitCountAliFunction fun = (LimitCountAliFunction) function;

        components.add(pad(pad, translatable("ali.type.function.limit_count")));
        components.addAll(GenericTooltipUtils.getRangeValueTooltip(pad + 1, "ali.property.value.min", fun.min));
        components.addAll(GenericTooltipUtils.getRangeValueTooltip(pad + 1, "ali.property.value.max", fun.max));

        return components;
    }

    @NotNull
    public static List<Component> getLootingEnchantTooltip(IUtils utils, int pad, ILootFunction function) {
        List<Component> components = new LinkedList<>();
        LootingEnchantAliFunction fun = (LootingEnchantAliFunction) function;

        components.add(pad(pad, translatable("ali.type.function.looting_enchant")));
        components.addAll(GenericTooltipUtils.getRangeValueTooltip(pad + 1, "ali.property.value.value", fun.value));
        components.addAll(GenericTooltipUtils.getIntegerTooltip(pad + 1, "ali.property.value.limit", fun.limit));

        return components;
    }

    @NotNull
    public static List<Component> getReferenceTooltip(IUtils utils, int pad, ILootFunction function) {
        List<Component> components = new LinkedList<>();
        ReferenceAliFunction fun = (ReferenceAliFunction) function;

        components.add(pad(pad, translatable("ali.type.function.reference")));
        components.addAll(GenericTooltipUtils.getResourceLocationTooltip(pad + 1, "ali.property.value.name", fun.name));

        return components;
    }

    @NotNull
    public static List<Component> getSetAttributesTooltip(IUtils utils, int pad, ILootFunction function) {
        List<Component> components = new LinkedList<>();
        SetAttributesAliFunction fun = (SetAttributesAliFunction) function;

        components.add(pad(pad, translatable("ali.type.function.set_attributes")));
        components.addAll(GenericTooltipUtils.getModifiersTooltip(pad + 1, fun.modifiers));

        return components;
    }

    @NotNull
    public static List<Component> getSetBannerPatternTooltip(IUtils utils, int pad, ILootFunction function) {
        List<Component> components = new LinkedList<>();
        SetBannerPatternAliFunction fun = (SetBannerPatternAliFunction) function;

        components.add(pad(pad, translatable("ali.type.function.set_banner_pattern")));
        components.addAll(GenericTooltipUtils.getBooleanTooltip(pad + 1, "ali.property.value.append", fun.append));
        components.addAll(GenericTooltipUtils.getBannerPatternsTooltip(pad + 1, fun.patterns));

        return components;
    }

    @NotNull
    public static List<Component> getSetContentsTooltip(IUtils utils, int pad, ILootFunction function) {
        List<Component> components = new LinkedList<>();
        SetContentsAliFunction fun = (SetContentsAliFunction) function;

        components.add(pad(pad, translatable("ali.type.function.set_contents")));
        components.addAll(GenericTooltipUtils.getBlockEntityTypeTooltip(pad + 1, fun.blockEntityType));
        //FIXME entries

        return components;
    }

    @NotNull
    public static List<Component> getSetCountTooltip(IUtils utils, int pad, ILootFunction function) {
        List<Component> components = new LinkedList<>();
        SetCountAliFunction fun = (SetCountAliFunction) function;

        components.add(pad(pad, translatable("ali.type.function.set_count")));
        components.addAll(GenericTooltipUtils.getRangeValueTooltip(pad + 1, "ali.property.value.count", fun.count));
        components.addAll(GenericTooltipUtils.getBooleanTooltip(pad + 1, "ali.property.value.add", fun.add));

        return components;
    }

    @NotNull
    public static List<Component> getSetDamageTooltip(IUtils utils, int pad, ILootFunction function) {
        List<Component> components = new LinkedList<>();
        SetDamageAliFunction fun = (SetDamageAliFunction) function;

        components.add(pad(pad, translatable("ali.type.function.set_damage")));
        components.addAll(GenericTooltipUtils.getRangeValueTooltip(pad + 1, "ali.property.value.damage", fun.damage));
        components.addAll(GenericTooltipUtils.getBooleanTooltip(pad + 1, "ali.property.value.add", fun.add));

        return components;
    }

    @NotNull
    public static List<Component> getSetEnchantmentsTooltip(IUtils utils, int pad, ILootFunction function) {
        List<Component> components = new LinkedList<>();
        SetEnchantmentsAliFunction fun = (SetEnchantmentsAliFunction) function;

        components.add(pad(pad, translatable("ali.type.function.set_enchantments")));

        if (!fun.enchantments.isEmpty()) {
            components.add(pad(pad + 1, translatable("ali.property.branch.enchantments")));
            fun.enchantments.forEach((enchantment, value) -> {
                components.addAll(GenericTooltipUtils.getEnchantmentTooltip(pad + 2, enchantment));
                components.addAll(GenericTooltipUtils.getRangeValueTooltip(pad + 3, "ali.property.value.levels", value));
            });
        }

        components.addAll(GenericTooltipUtils.getBooleanTooltip(pad + 1, "ali.property.value.add", fun.add));

        return components;
    }

    @NotNull
    public static List<Component> getSetInstrumentTooltip(IUtils utils, int pad, ILootFunction function) {
        List<Component> components = new LinkedList<>();
        SetInstrumentAliFunction fun = (SetInstrumentAliFunction) function;

        components.add(pad(pad, translatable("ali.type.function.set_instrument")));
        components.addAll(GenericTooltipUtils.getTagKeyTooltip(pad + 1, "ali.property.value.options", fun.options));

        return components;
    }

    @NotNull
    public static List<Component> getSetLootTableTooltip(IUtils utils, int pad, ILootFunction function) {
        List<Component> components = new LinkedList<>();
        SetLootTableAliFunction fun = (SetLootTableAliFunction) function;

        components.add(pad(pad, translatable("ali.type.function.set_loot_table")));
        components.addAll(GenericTooltipUtils.getResourceLocationTooltip(pad + 1, "ali.property.value.name", fun.name));
        components.addAll(GenericTooltipUtils.getLongTooltip(pad + 1, "ali.property.value.seed", fun.seed));
        components.addAll(GenericTooltipUtils.getBlockEntityTypeTooltip(pad + 1, fun.blockEntityType));

        return components;
    }

    @NotNull
    public static List<Component> getSetLoreTooltip(IUtils utils, int pad, ILootFunction function) {
        List<Component> components = new LinkedList<>();
        SetLoreAliFunction fun = (SetLoreAliFunction) function;

        components.add(pad(pad, translatable("ali.type.function.set_lore")));
        components.addAll(GenericTooltipUtils.getBooleanTooltip(pad + 1, "ali.property.value.replace", fun.replace));
        components.addAll(GenericTooltipUtils.getComponentsTooltip(pad + 1, "ali.property.branch.lore", fun.lore.stream().map((c) -> (Component)pad(pad + 2, c)).toList()));

        if (fun.resolutionContext != null) {
            components.addAll(GenericTooltipUtils.getEnumTooltip(pad + 1, "ali.property.value.resolution_context", "target", fun.resolutionContext));
        }

        return components;
    }

    @NotNull
    public static List<Component> getSetNameTooltip(IUtils utils, int pad, ILootFunction function) {
        List<Component> components = new LinkedList<>();
        SetNameAliFunction fun = (SetNameAliFunction) function;

        components.add(pad(pad, translatable("ali.type.function.set_name")));
        components.addAll(GenericTooltipUtils.getComponentTooltip(pad + 1, "ali.property.value.name", fun.name));

        if (fun.resolutionContext != null) {
            components.addAll(GenericTooltipUtils.getEnumTooltip(pad + 1, "ali.property.value.resolution_context", "target", fun.resolutionContext));
        }

        return components;
    }

    @NotNull
    public static List<Component> getSetNbtTooltip(IUtils utils, int pad, ILootFunction function) {
        List<Component> components = new LinkedList<>();
        SetNbtAliFunction fun = (SetNbtAliFunction) function;

        components.add(pad(pad, translatable("ali.type.function.set_nbt")));
        components.addAll(GenericTooltipUtils.getStringTooltip(pad + 1, "ali.property.value.tag", fun.tag));

        return components;
    }

    @NotNull
    public static List<Component> getSetPotionTooltip(IUtils utils, int pad, ILootFunction function) {
        List<Component> components = new LinkedList<>();
        SetPotionAliFunction fun = (SetPotionAliFunction) function;

        components.add(pad(pad, translatable("ali.type.function.set_potion")));
        components.addAll(GenericTooltipUtils.getPotionTooltip(pad + 1, fun.potion));

        return components;
    }

    @NotNull
    public static List<Component> getSetStewEffectTooltip(IUtils utils, int pad, ILootFunction function) {
        List<Component> components = new LinkedList<>();
        SetStewEffectAliFunction fun = (SetStewEffectAliFunction) function;

        components.add(pad(pad, translatable("ali.type.function.set_stew_effect")));

        if (!fun.effectMap.isEmpty()) {
            components.add(pad(pad + 1, translatable("ali.property.branch.mob_effects")));
            fun.effectMap.forEach((effect, duration) -> {
                components.addAll(GenericTooltipUtils.getMobEffectTooltip(pad + 2, effect));
                components.addAll(GenericTooltipUtils.getRangeValueTooltip(pad + 3, "ali.property.value.duration", duration));
            });
        }

        return components;
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getUnknownTooltip(IUtils utils, int pad, ILootFunction function) {
        UnknownAliFunction fun = (UnknownAliFunction) function;
        return List.of(pad(pad, translatable("ali.type.function.unknown", fun.functionType)));
    }
}
