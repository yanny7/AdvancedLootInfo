package com.yanny.ali.plugin.server;

import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.ali.api.IServerUtils;
import net.minecraft.core.Registry;
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
        return getBuiltInRegistryTooltip(utils, Registry.LOOT_POOL_ENTRY_TYPE, type, utils.getConfiguration().showInGameNames);
    }

    @NotNull
    public static TooltipBuilder getFunctionTypeTooltip(IServerUtils utils, LootItemFunctionType type) {
        return getBuiltInRegistryTooltip(utils, Registry.LOOT_FUNCTION_TYPE, type, utils.getConfiguration().showInGameNames);
    }

    @NotNull
    public static TooltipBuilder getConditionTypeTooltip(IServerUtils utils, LootItemConditionType type) {
        return getBuiltInRegistryTooltip(utils, Registry.LOOT_CONDITION_TYPE, type, utils.getConfiguration().showInGameNames);
    }

    @NotNull
    public static TooltipBuilder getBlockTooltip(IServerUtils utils, Block block) {
        return getBuiltInRegistryTooltip(utils, Registry.BLOCK, block, utils.getConfiguration().showInGameNames);
    }

    @NotNull
    public static TooltipBuilder getItemTooltip(IServerUtils utils, Item item) {
        return getBuiltInRegistryTooltip(utils, Registry.ITEM, item, utils.getConfiguration().showInGameNames);
    }

    @NotNull
    public static TooltipBuilder getEntityTypeTooltip(IServerUtils utils, EntityType<?> entityType) {
        return getBuiltInRegistryTooltip(utils, Registry.ENTITY_TYPE, entityType, utils.getConfiguration().showInGameNames);
    }

    @NotNull
    public static TooltipBuilder getBannerPatternTooltip(IServerUtils utils, BannerPattern bannerPattern) {
        return getBuiltInRegistryTooltip(utils, Registry.BANNER_PATTERN, bannerPattern, utils.getConfiguration().showInGameNames);
    }

    @NotNull
    public static TooltipBuilder getBlockEntityTypeTooltip(IServerUtils utils, BlockEntityType<?> blockEntityType) {
        return getBuiltInRegistryTooltip(utils, Registry.BLOCK_ENTITY_TYPE, blockEntityType, utils.getConfiguration().showInGameNames);
    }

    @NotNull
    public static TooltipBuilder getPotionTooltip(IServerUtils utils, Potion potion) {
        return getBuiltInRegistryTooltip(utils, Registry.POTION, potion, utils.getConfiguration().showInGameNames);
    }

    @NotNull
    public static TooltipBuilder getMobEffectTooltip(IServerUtils utils, MobEffect mobEffect) {
        return getBuiltInRegistryTooltip(utils, Registry.MOB_EFFECT, mobEffect, utils.getConfiguration().showInGameNames);
    }

    @NotNull
    public static TooltipBuilder getLootNbtProviderTypeTooltip(IServerUtils utils, LootNbtProviderType providerType) {
        return getBuiltInRegistryTooltip(utils, Registry.LOOT_NBT_PROVIDER_TYPE, providerType, utils.getConfiguration().showInGameNames);
    }

    @NotNull
    public static TooltipBuilder getFluidTooltip(IServerUtils utils, Fluid fluid) {
        return getBuiltInRegistryTooltip(utils, Registry.FLUID, fluid, utils.getConfiguration().showInGameNames);
    }

    @NotNull
    public static TooltipBuilder getEnchantmentTooltip(IServerUtils utils, Enchantment enchantment) {
        return getBuiltInRegistryTooltip(utils, Registry.ENCHANTMENT, enchantment, utils.getConfiguration().showInGameNames);
    }

    @NotNull
    public static TooltipBuilder getAttributeTooltip(IServerUtils utils, Attribute attribute) {
        return getBuiltInRegistryTooltip(utils, Registry.ATTRIBUTE, attribute, utils.getConfiguration().showInGameNames);
    }
}
