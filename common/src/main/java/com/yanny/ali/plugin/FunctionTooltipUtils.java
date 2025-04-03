package com.yanny.ali.plugin;

import com.yanny.ali.api.IUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.storage.loot.functions.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.yanny.ali.plugin.GenericTooltipUtils.*;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class FunctionTooltipUtils {
    @NotNull
    public static List<Component> getApplyBonusTooltip(IUtils utils, int pad, LootItemFunction function) {
        List<Component> components = new LinkedList<>();
        ApplyBonusCount fun = (ApplyBonusCount) function;

        components.add(pad(pad, translatable("ali.type.function.apply_bonus")));
        components.addAll(getHolderTooltip(utils, pad + 1, fun.enchantment, GenericTooltipUtils::getEnchantmentTooltip));
        components.addAll(getFormulaTooltip(utils, pad + 1, fun.formula));

        return components;
    }

    @NotNull
    public static List<Component> getCopyNameTooltip(IUtils utils, int pad, LootItemFunction function) {
        List<Component> components = new LinkedList<>();
        CopyNameFunction fun = (CopyNameFunction) function;

        components.add(pad(pad, translatable("ali.type.function.copy_name")));
        components.addAll(getNameSourceTooltip(utils, pad + 1, fun.source));

        return components;
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getCopyNbtTooltip(IUtils utils, int pad, LootItemFunction function) {
        List<Component> components = new LinkedList<>();
        CopyNbtFunction fun = (CopyNbtFunction) function;

        components.add(pad(pad, translatable("ali.type.function.copy_nbt")));
        components.addAll(getResourceLocationTooltip(utils, pad + 1, "ali.property.value.nbt_provider",
                Objects.requireNonNull(BuiltInRegistries.LOOT_NBT_PROVIDER_TYPE.getKey(fun.source.getType()))));
        components.addAll(getCollectionTooltip(utils, pad + 1, "ali.property.branch.operations", fun.operations, GenericTooltipUtils::getCopyOperationTooltip));

        return components;
    }

    @NotNull
    public static List<Component> getCopyStateTooltip(IUtils utils, int pad, LootItemFunction function) {
        List<Component> components = new LinkedList<>();
        CopyBlockState fun = (CopyBlockState) function;

        components.add(pad(pad, translatable("ali.type.function.copy_state")));
        components.addAll(getHolderTooltip(utils, pad + 1, fun.block, GenericTooltipUtils::getBlockTooltip));
        components.addAll(getCollectionTooltip(utils, pad + 1, "ali.property.branch.properties", fun.properties, GenericTooltipUtils::getPropertyTooltip));

        return components;
    }

    @NotNull
    public static List<Component> getEnchantRandomlyTooltip(IUtils utils, int pad, LootItemFunction function) {
        List<Component> components = new LinkedList<>();
        EnchantRandomlyFunction fun = (EnchantRandomlyFunction) function;

        components.add(pad(pad, translatable("ali.type.function.enchant_randomly")));
        components.addAll(getOptionalHolderSetTooltip(utils, pad + 1, "ali.property.branch.enchantments", fun.enchantments, GenericTooltipUtils::getEnchantmentTooltip));

        return components;
    }

    @NotNull
    public static List<Component> getEnchantWithLevelsTooltip(IUtils utils, int pad, LootItemFunction function) {
        List<Component> components = new LinkedList<>();
        EnchantWithLevelsFunction fun = (EnchantWithLevelsFunction) function;

        components.add(pad(pad, translatable("ali.type.function.enchant_with_levels")));
        components.addAll(getRangeValueTooltip(utils, pad + 1, "ali.property.value.levels", utils.convertNumber(utils, fun.levels)));
        components.addAll(getBooleanTooltip(utils, pad + 1, "ali.property.value.treasure", fun.treasure));

        return components;
    }

    @NotNull
    public static List<Component> getExplorationMapTooltip(IUtils utils, int pad, LootItemFunction function) {
        List<Component> components = new LinkedList<>();
        ExplorationMapFunction fun = (ExplorationMapFunction) function;

        components.add(pad(pad, translatable("ali.type.function.exploration_map")));
        components.addAll(getTagKeyTooltip(utils, pad + 1, "ali.property.value.destination", fun.destination));
        components.addAll(getEnumTooltip(utils, pad + 1, "ali.property.value.map_decoration", fun.mapDecoration));
        components.addAll(getIntegerTooltip(utils, pad + 1, "ali.property.value.zoom", fun.zoom));
        components.addAll(getIntegerTooltip(utils, pad + 1, "ali.property.value.search_radius", fun.searchRadius));
        components.addAll(getBooleanTooltip(utils, pad + 1, "ali.property.value.skip_known_structures", fun.skipKnownStructures));

        return components;
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getExplosionDecayTooltip(IUtils utils, int pad, LootItemFunction function) {
        return List.of(pad(pad, translatable("ali.type.function.explosion_decay")));
    }

    @NotNull
    public static List<Component> getFillPlayerHeadTooltip(IUtils utils, int pad, LootItemFunction function) {
        List<Component> components = new LinkedList<>();
        FillPlayerHead fun = (FillPlayerHead) function;

        components.add(pad(pad, translatable("ali.type.function.fill_player_head")));
        components.addAll(getEnumTooltip(utils, pad + 1, "ali.property.value.target", "target", Optional.of(fun.entityTarget)));

        return components;
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getFurnaceSmeltTooltip(IUtils utils, int pad, LootItemFunction function) {
        return List.of(pad(pad, translatable("ali.type.function.furnace_smelt")));
    }

    @NotNull
    public static List<Component> getLimitCountTooltip(IUtils utils, int pad, LootItemFunction function) {
        List<Component> components = new LinkedList<>();
        LimitCount fun = (LimitCount) function;

        components.add(pad(pad, translatable("ali.type.function.limit_count")));
        components.addAll(getRangeValueTooltip(utils, pad + 1, "ali.property.value.min", utils.convertNumber(utils, fun.limiter.min)));
        components.addAll(getRangeValueTooltip(utils, pad + 1, "ali.property.value.max", utils.convertNumber(utils, fun.limiter.max)));

        return components;
    }

    @NotNull
    public static List<Component> getLootingEnchantTooltip(IUtils utils, int pad, LootItemFunction function) {
        List<Component> components = new LinkedList<>();
        LootingEnchantFunction fun = (LootingEnchantFunction) function;

        components.add(pad(pad, translatable("ali.type.function.looting_enchant")));
        components.addAll(getRangeValueTooltip(utils, pad + 1, "ali.property.value.value", utils.convertNumber(utils, fun.value)));
        components.addAll(getIntegerTooltip(utils, pad + 1, "ali.property.value.limit", fun.limit));

        return components;
    }

    @NotNull
    public static List<Component> getReferenceTooltip(IUtils utils, int pad, LootItemFunction function) {
        List<Component> components = new LinkedList<>();
        FunctionReference fun = (FunctionReference) function;

        components.add(pad(pad, translatable("ali.type.function.reference")));
        components.addAll(getResourceLocationTooltip(utils, pad + 1, "ali.property.value.name", fun.name));

        return components;
    }

    @NotNull
    public static List<Component> getSequenceTooltip(IUtils utils, int pad, LootItemFunction function) {
        List<Component> components = new LinkedList<>();
        SequenceFunction fun = (SequenceFunction) function;

        components.add(pad(pad, translatable("ali.type.function.sequence")));
        components.addAll(GenericTooltipUtils.getFunctionsTooltip(utils, pad + 1, fun.functions));

        return components;
    }

    @NotNull
    public static List<Component> getSetAttributesTooltip(IUtils utils, int pad, LootItemFunction function) {
        List<Component> components = new LinkedList<>();
        SetAttributesFunction fun = (SetAttributesFunction) function;

        components.add(pad(pad, translatable("ali.type.function.set_attributes")));
        components.addAll(getCollectionTooltip(utils, pad + 1, "ali.property.branch.modifiers", fun.modifiers, GenericTooltipUtils::getModifierTooltip));

        return components;
    }

    @NotNull
    public static List<Component> getSetBannerPatternTooltip(IUtils utils, int pad, LootItemFunction function) {
        List<Component> components = new LinkedList<>();
        SetBannerPatternFunction fun = (SetBannerPatternFunction) function;

        components.add(pad(pad, translatable("ali.type.function.set_banner_pattern")));
        components.addAll(getBooleanTooltip(utils, pad + 1, "ali.property.value.append", fun.append));
        components.addAll(getCollectionTooltip(utils, pad + 1, "ali.property.branch.banner_patterns", fun.patterns, GenericTooltipUtils::getBannerPatternTooltip));

        return components;
    }

    @NotNull
    public static List<Component> getSetContentsTooltip(IUtils utils, int pad, LootItemFunction function) {
        List<Component> components = new LinkedList<>();
        SetContainerContents fun = (SetContainerContents) function;

        components.add(pad(pad, translatable("ali.type.function.set_contents")));
        components.addAll(getHolderTooltip(utils, pad + 1, fun.type, GenericTooltipUtils::getBlockEntityTypeTooltip));

        return components;
    }

    @NotNull
    public static List<Component> getSetCountTooltip(IUtils utils, int pad, LootItemFunction function) {
        List<Component> components = new LinkedList<>();
        SetItemCountFunction fun = (SetItemCountFunction) function;

        components.add(pad(pad, translatable("ali.type.function.set_count")));
        components.addAll(getRangeValueTooltip(utils, pad + 1, "ali.property.value.count", utils.convertNumber(utils, fun.value)));
        components.addAll(getBooleanTooltip(utils, pad + 1, "ali.property.value.add", fun.add));

        return components;
    }

    @NotNull
    public static List<Component> getSetDamageTooltip(IUtils utils, int pad, LootItemFunction function) {
        List<Component> components = new LinkedList<>();
        SetItemDamageFunction fun = (SetItemDamageFunction) function;

        components.add(pad(pad, translatable("ali.type.function.set_damage")));
        components.addAll(getRangeValueTooltip(utils, pad + 1, "ali.property.value.damage", utils.convertNumber(utils, fun.damage)));
        components.addAll(getBooleanTooltip(utils, pad + 1, "ali.property.value.add", fun.add));

        return components;
    }

    @NotNull
    public static List<Component> getSetEnchantmentsTooltip(IUtils utils, int pad, LootItemFunction function) {
        List<Component> components = new LinkedList<>();
        SetEnchantmentsFunction fun = (SetEnchantmentsFunction) function;

        components.add(pad(pad, translatable("ali.type.function.set_enchantments")));

        if (!fun.enchantments.isEmpty()) {
            components.add(pad(pad + 1, translatable("ali.property.branch.enchantments")));
            fun.enchantments.forEach((enchantment, value) -> {
                components.addAll(getHolderTooltip(utils, pad + 2, enchantment, GenericTooltipUtils::getEnchantmentTooltip));
                components.addAll(getRangeValueTooltip(utils, pad + 3, "ali.property.value.levels", utils.convertNumber(utils, value)));
            });
        }

        components.addAll(getBooleanTooltip(utils, pad + 1, "ali.property.value.add", fun.add));

        return components;
    }

    @NotNull
    public static List<Component> getSetInstrumentTooltip(IUtils utils, int pad, LootItemFunction function) {
        List<Component> components = new LinkedList<>();
        SetInstrumentFunction fun = (SetInstrumentFunction) function;

        components.add(pad(pad, translatable("ali.type.function.set_instrument")));
        components.addAll(getTagKeyTooltip(utils, pad + 1, "ali.property.value.options", fun.options));

        return components;
    }

    @NotNull
    public static List<Component> getSetLootTableTooltip(IUtils utils, int pad, LootItemFunction function) {
        List<Component> components = new LinkedList<>();
        SetContainerLootTable fun = (SetContainerLootTable) function;

        components.add(pad(pad, translatable("ali.type.function.set_loot_table")));
        components.addAll(getResourceLocationTooltip(utils, pad + 1, "ali.property.value.name", fun.name));
        components.addAll(getLongTooltip(utils, pad + 1, "ali.property.value.seed", fun.seed));
        components.addAll(getHolderTooltip(utils, pad + 1, fun.type, GenericTooltipUtils::getBlockEntityTypeTooltip));

        return components;
    }

    @NotNull
    public static List<Component> getSetLoreTooltip(IUtils utils, int pad, LootItemFunction function) {
        List<Component> components = new LinkedList<>();
        SetLoreFunction fun = (SetLoreFunction) function;

        components.add(pad(pad, translatable("ali.type.function.set_lore")));
        components.addAll(getBooleanTooltip(utils, pad + 1, "ali.property.value.replace", fun.replace));
        components.addAll(getCollectionTooltip(utils, pad + 1, "ali.property.branch.lore", fun.lore, (u, p, c) -> List.of(pad(pad + 2, c))));
        components.addAll(getEnumTooltip(utils, pad + 1, "ali.property.value.resolution_context", "target", fun.resolutionContext));

        return components;
    }

    @NotNull
    public static List<Component> getSetNameTooltip(IUtils utils, int pad, LootItemFunction function) {
        List<Component> components = new LinkedList<>();
        SetNameFunction fun = (SetNameFunction) function;

        components.add(pad(pad, translatable("ali.type.function.set_name")));
        components.addAll(getOptionalTooltip(utils, pad + 1, "ali.property.value.name", fun.name, GenericTooltipUtils::getComponentTooltip));
        components.addAll(getEnumTooltip(utils, pad + 1, "ali.property.value.resolution_context", "target", fun.resolutionContext));

        return components;
    }

    @NotNull
    public static List<Component> getSetNbtTooltip(IUtils utils, int pad, LootItemFunction function) {
        List<Component> components = new LinkedList<>();
        SetNbtFunction fun = (SetNbtFunction) function;

        components.add(pad(pad, translatable("ali.type.function.set_nbt")));
        components.addAll(getStringTooltip(utils, pad + 1, "ali.property.value.tag", fun.tag.getAsString()));

        return components;
    }

    @NotNull
    public static List<Component> getSetPotionTooltip(IUtils utils, int pad, LootItemFunction function) {
        List<Component> components = new LinkedList<>();
        SetPotionFunction fun = (SetPotionFunction) function;

        components.add(pad(pad, translatable("ali.type.function.set_potion")));
        components.addAll(getHolderTooltip(utils, pad + 1, fun.potion, GenericTooltipUtils::getPotionTooltip));

        return components;
    }

    @NotNull
    public static List<Component> getSetStewEffectTooltip(IUtils utils, int pad, LootItemFunction function) {
        List<Component> components = new LinkedList<>();
        SetStewEffectFunction fun = (SetStewEffectFunction) function;

        components.add(pad(pad, translatable("ali.type.function.set_stew_effect")));

        if (!fun.effects.isEmpty()) {
            components.add(pad(pad + 1, translatable("ali.property.branch.mob_effects")));
            fun.effects.forEach((effect) -> {
                components.addAll(getHolderTooltip(utils, pad + 2, effect.effect(), GenericTooltipUtils::getMobEffectTooltip));
                components.addAll(getRangeValueTooltip(utils, pad + 3, "ali.property.value.duration", utils.convertNumber(utils, effect.duration())));
            });
        }

        return components;
    }
}
