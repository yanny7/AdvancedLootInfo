package com.yanny.ali.plugin.server;

import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.ali.api.IServerUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryType;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.minecraft.world.level.storage.loot.providers.nbt.LootNbtProviderType;
import org.jetbrains.annotations.NotNull;

import static com.yanny.aci.tooltip.CoreTooltipUtils.getBuiltInRegistryTooltip;

public class RegistriesTooltipUtils {
    @NotNull
    public static TooltipBuilder getEntryTypeTooltip(IServerUtils utils, LootPoolEntryType type) {
        return getBuiltInRegistryTooltip(utils, BuiltInRegistries.LOOT_POOL_ENTRY_TYPE, type);
    }

    @NotNull
    public static TooltipBuilder getFunctionTypeTooltip(IServerUtils utils, LootItemFunctionType type) {
        return getBuiltInRegistryTooltip(utils, BuiltInRegistries.LOOT_FUNCTION_TYPE, type);
    }

    @NotNull
    public static TooltipBuilder getConditionTypeTooltip(IServerUtils utils, LootItemConditionType type) {
        return getBuiltInRegistryTooltip(utils, BuiltInRegistries.LOOT_CONDITION_TYPE, type);
    }

    @NotNull
    public static TooltipBuilder getBlockTooltip(IServerUtils utils, Block block) {
        return getBuiltInRegistryTooltip(utils, BuiltInRegistries.BLOCK, block, utils.getConfiguration().showInGameNames);
    }

    @NotNull
    public static TooltipBuilder getItemTooltip(IServerUtils utils, Item item) {
        return getBuiltInRegistryTooltip(utils, BuiltInRegistries.ITEM, item, utils.getConfiguration().showInGameNames);
    }

    @NotNull
    public static TooltipBuilder getEntityTypeTooltip(IServerUtils utils, EntityType<?> entityType) {
        return getBuiltInRegistryTooltip(utils, BuiltInRegistries.ENTITY_TYPE, entityType, utils.getConfiguration().showInGameNames);
    }

    @NotNull
    public static TooltipBuilder getBannerPatternTooltip(IServerUtils utils, BannerPattern bannerPattern) {
        return getBuiltInRegistryTooltip(utils, BuiltInRegistries.BANNER_PATTERN, bannerPattern);
    }

    @NotNull
    public static TooltipBuilder getBlockEntityTypeTooltip(IServerUtils utils, BlockEntityType<?> blockEntityType) {
        return getBuiltInRegistryTooltip(utils, BuiltInRegistries.BLOCK_ENTITY_TYPE, blockEntityType);
    }

    @NotNull
    public static TooltipBuilder getPotionTooltip(IServerUtils utils, Potion potion) {
        return getBuiltInRegistryTooltip(utils, BuiltInRegistries.POTION, potion);
    }

    @NotNull
    public static TooltipBuilder getMobEffectTooltip(IServerUtils utils, MobEffect mobEffect) {
        return getBuiltInRegistryTooltip(utils, BuiltInRegistries.MOB_EFFECT, mobEffect, utils.getConfiguration().showInGameNames);
    }

    @NotNull
    public static TooltipBuilder getLootNbtProviderTypeTooltip(IServerUtils utils, LootNbtProviderType providerType) {
        return getBuiltInRegistryTooltip(utils, BuiltInRegistries.LOOT_NBT_PROVIDER_TYPE, providerType);
    }

    @NotNull
    public static TooltipBuilder getFluidTooltip(IServerUtils utils, Fluid fluid) {
        return getBuiltInRegistryTooltip(utils, BuiltInRegistries.FLUID, fluid);
    }

    @NotNull
    public static TooltipBuilder getEnchantmentTooltip(IServerUtils utils, Enchantment enchantment) {
        return getBuiltInRegistryTooltip(utils, BuiltInRegistries.ENCHANTMENT, enchantment, utils.getConfiguration().showInGameNames);
    }

    @NotNull
    public static TooltipBuilder getAttributeTooltip(IServerUtils utils, Attribute attribute) {
        return getBuiltInRegistryTooltip(utils, BuiltInRegistries.ATTRIBUTE, attribute, utils.getConfiguration().showInGameNames);
    }
}
