package com.yanny.ali.plugin.client;

import com.yanny.ali.api.IClientUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.storage.loot.functions.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import static com.yanny.ali.plugin.client.GenericTooltipUtils.*;

public class FunctionTooltipUtils {
    @NotNull
    public static List<Component> getApplyBonusTooltip(IClientUtils utils, int pad, ApplyBonusCount fun) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.function.apply_bonus")));
        components.addAll(getEnchantmentTooltip(utils, pad + 1, fun.enchantment));
        components.addAll(getFormulaTooltip(utils, pad + 1, fun.formula));

        return components;
    }

    @NotNull
    public static List<Component> getCopyNameTooltip(IClientUtils utils, int pad, CopyNameFunction fun) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.function.copy_name")));
        components.addAll(getEnumTooltip(utils, pad + 1, "ali.property.value.source", fun.source));

        return components;
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getCopyNbtTooltip(IClientUtils utils, int pad, CopyNbtFunction fun) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.function.copy_nbt")));
        components.addAll(getResourceLocationTooltip(utils, pad + 1, "ali.property.value.nbt_provider",
                Objects.requireNonNull(BuiltInRegistries.LOOT_NBT_PROVIDER_TYPE.getKey(fun.source.getType()))));
        components.addAll(getCollectionTooltip(utils, pad + 1, "ali.property.branch.operations", fun.operations, GenericTooltipUtils::getCopyOperationTooltip));

        return components;
    }

    @NotNull
    public static List<Component> getCopyStateTooltip(IClientUtils utils, int pad, CopyBlockState fun) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.function.copy_state")));
        components.addAll(getBlockTooltip(utils, pad + 1, fun.block));
        components.addAll(getCollectionTooltip(utils, pad + 1, "ali.property.branch.properties", fun.properties, GenericTooltipUtils::getPropertyTooltip));

        return components;
    }

    @NotNull
    public static List<Component> getEnchantRandomlyTooltip(IClientUtils utils, int pad, EnchantRandomlyFunction fun) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.function.enchant_randomly")));
        components.addAll(getCollectionTooltip(utils, pad + 1, "ali.property.branch.enchantments", fun.enchantments, GenericTooltipUtils::getEnchantmentTooltip));

        return components;
    }

    @NotNull
    public static List<Component> getEnchantWithLevelsTooltip(IClientUtils utils, int pad, EnchantWithLevelsFunction fun) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.function.enchant_with_levels")));
        components.addAll(getNumberProviderTooltip(utils, pad + 1, "ali.property.value.levels", fun.levels));
        components.addAll(getBooleanTooltip(utils, pad + 1, "ali.property.value.treasure", fun.treasure));

        return components;
    }

    @NotNull
    public static List<Component> getExplorationMapTooltip(IClientUtils utils, int pad, ExplorationMapFunction fun) {
        List<Component> components = new LinkedList<>();

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
    public static List<Component> getExplosionDecayTooltip(IClientUtils ignoredUtils, int pad, ApplyExplosionDecay ignoredFun) {
        return List.of(pad(pad, translatable("ali.type.function.explosion_decay")));
    }

    @NotNull
    public static List<Component> getFillPlayerHeadTooltip(IClientUtils utils, int pad, FillPlayerHead fun) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.function.fill_player_head")));
        components.addAll(getEnumTooltip(utils, pad + 1, "ali.property.value.target", fun.entityTarget));

        return components;
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getFurnaceSmeltTooltip(IClientUtils ignoredUtils, int pad, SmeltItemFunction ignoredFun) {
        return List.of(pad(pad, translatable("ali.type.function.furnace_smelt")));
    }

    @NotNull
    public static List<Component> getLimitCountTooltip(IClientUtils utils, int pad, LimitCount fun) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.function.limit_count")));
        components.addAll(getIntRangeTooltip(utils, pad + 1, "ali.property.value.limit", fun.limiter));

        return components;
    }

    @NotNull
    public static List<Component> getLootingEnchantTooltip(IClientUtils utils, int pad, LootingEnchantFunction fun) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.function.looting_enchant")));
        components.addAll(getNumberProviderTooltip(utils, pad + 1, "ali.property.value.value", fun.value));

        if (fun.limit > 0) {
            components.addAll(getIntegerTooltip(utils, pad + 1, "ali.property.value.limit", fun.limit));
        }

        return components;
    }

    @NotNull
    public static List<Component> getReferenceTooltip(IClientUtils utils, int pad, FunctionReference fun) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.function.reference")));
        components.addAll(getResourceLocationTooltip(utils, pad + 1, "ali.property.value.name", fun.name));

        return components;
    }

    @NotNull
    public static List<Component> getSetAttributesTooltip(IClientUtils utils, int pad, SetAttributesFunction fun) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.function.set_attributes")));
        components.addAll(getCollectionTooltip(utils, pad + 1, "ali.property.branch.modifiers", fun.modifiers, GenericTooltipUtils::getModifierTooltip));

        return components;
    }

    @NotNull
    public static List<Component> getSetBannerPatternTooltip(IClientUtils utils, int pad, SetBannerPatternFunction fun) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.function.set_banner_pattern")));
        components.addAll(getBooleanTooltip(utils, pad + 1, "ali.property.value.append", fun.append));
        components.addAll(getBannerPatternsTooltip(utils, pad + 1, fun.patterns));

        return components;
    }

    @NotNull
    public static List<Component> getSetContentsTooltip(IClientUtils utils, int pad, SetContainerContents fun) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.function.set_contents")));
        components.addAll(getBlockEntityTypeTooltip(utils, pad + 1, fun.type));
        //TODO entries

        return components;
    }

    @NotNull
    public static List<Component> getSetCountTooltip(IClientUtils utils, int pad, SetItemCountFunction fun) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.function.set_count")));
        components.addAll(getNumberProviderTooltip(utils, pad + 1, "ali.property.value.count", fun.value));
        components.addAll(getBooleanTooltip(utils, pad + 1, "ali.property.value.add", fun.add));

        return components;
    }

    @NotNull
    public static List<Component> getSetDamageTooltip(IClientUtils utils, int pad, SetItemDamageFunction fun) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.function.set_damage")));
        components.addAll(getNumberProviderTooltip(utils, pad + 1, "ali.property.value.damage", fun.damage));
        components.addAll(getBooleanTooltip(utils, pad + 1, "ali.property.value.add", fun.add));

        return components;
    }

    @NotNull
    public static List<Component> getSetEnchantmentsTooltip(IClientUtils utils, int pad, SetEnchantmentsFunction fun) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.function.set_enchantments")));

        if (!fun.enchantments.isEmpty()) {
            components.add(pad(pad + 1, translatable("ali.property.branch.enchantments")));
            fun.enchantments.forEach((enchantment, value) -> {
                components.addAll(getEnchantmentTooltip(utils, pad + 2, enchantment));
                components.addAll(getNumberProviderTooltip(utils, pad + 3, "ali.property.value.levels", value));
            });
        }

        components.addAll(getBooleanTooltip(utils, pad + 1, "ali.property.value.add", fun.add));

        return components;
    }

    @NotNull
    public static List<Component> getSetInstrumentTooltip(IClientUtils utils, int pad, SetInstrumentFunction fun) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.function.set_instrument")));
        components.addAll(getTagKeyTooltip(utils, pad + 1, "ali.property.value.options", fun.options));

        return components;
    }

    @NotNull
    public static List<Component> getSetLootTableTooltip(IClientUtils utils, int pad, SetContainerLootTable fun) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.function.set_loot_table")));
        components.addAll(getResourceLocationTooltip(utils, pad + 1, "ali.property.value.name", fun.name));
        components.addAll(getLongTooltip(utils, pad + 1, "ali.property.value.seed", fun.seed));
        components.addAll(getBlockEntityTypeTooltip(utils, pad + 1, fun.type));

        return components;
    }

    @NotNull
    public static List<Component> getSetLoreTooltip(IClientUtils utils, int pad, SetLoreFunction fun) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.function.set_lore")));
        components.addAll(getBooleanTooltip(utils, pad + 1, "ali.property.value.replace", fun.replace));
        components.addAll(getComponentsTooltip(utils, pad + 1, "ali.property.branch.lore", fun.lore.stream().map((c) -> pad(pad + 2, c)).toList()));

        if (fun.resolutionContext != null) {
            components.addAll(getEnumTooltip(utils, pad + 1, "ali.property.value.resolution_context", fun.resolutionContext));
        }

        return components;
    }

    @NotNull
    public static List<Component> getSetNameTooltip(IClientUtils utils, int pad, SetNameFunction fun) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.function.set_name")));
        components.addAll(getComponentTooltip(utils, pad + 1, "ali.property.value.name", fun.name));

        if (fun.resolutionContext != null) {
            components.addAll(getEnumTooltip(utils, pad + 1, "ali.property.value.resolution_context", fun.resolutionContext));
        }

        return components;
    }

    @NotNull
    public static List<Component> getSetNbtTooltip(IClientUtils utils, int pad, SetNbtFunction fun) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.function.set_nbt")));
        components.addAll(getStringTooltip(utils, pad + 1, "ali.property.value.tag", fun.tag.getAsString()));

        return components;
    }

    @NotNull
    public static List<Component> getSetPotionTooltip(IClientUtils utils, int pad, SetPotionFunction fun) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.function.set_potion")));
        components.addAll(getPotionTooltip(utils, pad + 1, fun.potion));

        return components;
    }

    @NotNull
    public static List<Component> getSetStewEffectTooltip(IClientUtils utils, int pad, SetStewEffectFunction fun) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.function.set_stew_effect")));

        if (!fun.effectDurationMap.isEmpty()) {
            components.add(pad(pad + 1, translatable("ali.property.branch.mob_effects")));
            fun.effectDurationMap.forEach((effect, duration) -> {
                components.addAll(getMobEffectTooltip(utils, pad + 2, effect));
                components.addAll(getNumberProviderTooltip(utils, pad + 3, "ali.property.value.duration", duration));
            });
        }

        return components;
    }
}
