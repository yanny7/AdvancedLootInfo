package com.yanny.ali.plugin.server;

import com.yanny.ali.api.IKeyTooltipNode;
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
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.minecraft.world.level.storage.loot.providers.nbt.LootNbtProviderType;
import org.jetbrains.annotations.NotNull;

import static com.yanny.ali.plugin.server.ValueTooltipUtils.getBuiltInRegistryTooltip;

public class RegistriesTooltipUtils {
    @NotNull
    public static IKeyTooltipNode getFunctionTypeTooltip(IServerUtils utils, LootItemFunctionType type) {
        return getBuiltInRegistryTooltip(utils, BuiltInRegistries.LOOT_FUNCTION_TYPE, type);
    }

    @NotNull
    public static IKeyTooltipNode getConditionTypeTooltip(IServerUtils utils, LootItemConditionType type) {
        return getBuiltInRegistryTooltip(utils, BuiltInRegistries.LOOT_CONDITION_TYPE, type);
    }

    @NotNull
    public static IKeyTooltipNode getBlockTooltip(IServerUtils utils, Block block) {
        return getBuiltInRegistryTooltip(utils, BuiltInRegistries.BLOCK, block);
    }

    @NotNull
    public static IKeyTooltipNode getItemTooltip(IServerUtils utils, Item item) {
        return getBuiltInRegistryTooltip(utils, BuiltInRegistries.ITEM, item);
    }

    @NotNull
    public static IKeyTooltipNode getEntityTypeTooltip(IServerUtils utils, EntityType<?> entityType) {
        return getBuiltInRegistryTooltip(utils, BuiltInRegistries.ENTITY_TYPE, entityType);
    }

    @NotNull
    public static IKeyTooltipNode getBannerPatternTooltip(IServerUtils utils, BannerPattern bannerPattern) {
        return getBuiltInRegistryTooltip(utils, BuiltInRegistries.BANNER_PATTERN, bannerPattern);
    }

    @NotNull
    public static IKeyTooltipNode getBlockEntityTypeTooltip(IServerUtils utils, BlockEntityType<?> blockEntityType) {
        return getBuiltInRegistryTooltip(utils, BuiltInRegistries.BLOCK_ENTITY_TYPE, blockEntityType);
    }

    @NotNull
    public static IKeyTooltipNode getPotionTooltip(IServerUtils utils, Potion potion) {
        return getBuiltInRegistryTooltip(utils, BuiltInRegistries.POTION, potion);
    }

    @NotNull
    public static IKeyTooltipNode getMobEffectTooltip(IServerUtils utils, MobEffect mobEffect) {
        return getBuiltInRegistryTooltip(utils, BuiltInRegistries.MOB_EFFECT, mobEffect);
    }

    @NotNull
    public static IKeyTooltipNode getLootNbtProviderTypeTooltip(IServerUtils utils, LootNbtProviderType providerType) {
        return getBuiltInRegistryTooltip(utils, BuiltInRegistries.LOOT_NBT_PROVIDER_TYPE, providerType);
    }

    @NotNull
    public static IKeyTooltipNode getFluidTooltip(IServerUtils utils, Fluid fluid) {
        return getBuiltInRegistryTooltip(utils, BuiltInRegistries.FLUID, fluid);
    }

    @NotNull
    public static IKeyTooltipNode getEnchantmentTooltip(IServerUtils utils, Enchantment enchantment) {
        return getBuiltInRegistryTooltip(utils, BuiltInRegistries.ENCHANTMENT, enchantment);
    }

    @NotNull
    public static IKeyTooltipNode getAttributeTooltip(IServerUtils utils, Attribute attribute) {
        return getBuiltInRegistryTooltip(utils, BuiltInRegistries.ATTRIBUTE, attribute);
    }
}
