package com.yanny.ali.plugin.server;

import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.api.ITooltipNode;
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
import org.jetbrains.annotations.Unmodifiable;

import static com.yanny.ali.plugin.server.GenericTooltipUtils.getBuiltInRegistryTooltip;

public class RegistriesTooltipUtils {
    @Unmodifiable
    @NotNull
    public static ITooltipNode getFunctionTypeTooltip(IServerUtils utils, String key, LootItemFunctionType type) {
        return getBuiltInRegistryTooltip(utils, key, BuiltInRegistries.LOOT_FUNCTION_TYPE, type);
    }
    @Unmodifiable
    @NotNull
    public static ITooltipNode getConditionTypeTooltip(IServerUtils utils, String key, LootItemConditionType type) {
        return getBuiltInRegistryTooltip(utils, key, BuiltInRegistries.LOOT_CONDITION_TYPE, type);
    }

    @Unmodifiable
    @NotNull
    public static ITooltipNode getBlockTooltip(IServerUtils utils, String key, Block block) {
        return getBuiltInRegistryTooltip(utils, key, BuiltInRegistries.BLOCK, block);
    }

    @Unmodifiable
    @NotNull
    public static ITooltipNode getItemTooltip(IServerUtils utils, String key, Item item) {
        return getBuiltInRegistryTooltip(utils, key, BuiltInRegistries.ITEM, item);
    }

    @Unmodifiable
    @NotNull
    public static ITooltipNode getEntityTypeTooltip(IServerUtils utils, String key, EntityType<?> entityType) {
        return getBuiltInRegistryTooltip(utils, key, BuiltInRegistries.ENTITY_TYPE, entityType);
    }

    @Unmodifiable
    @NotNull
    public static ITooltipNode getBannerPatternTooltip(IServerUtils utils, String key, BannerPattern bannerPattern) {
        return getBuiltInRegistryTooltip(utils, key, BuiltInRegistries.BANNER_PATTERN, bannerPattern);
    }

    @Unmodifiable
    @NotNull
    public static ITooltipNode getBlockEntityTypeTooltip(IServerUtils utils, String key, BlockEntityType<?> blockEntityType) {
        return getBuiltInRegistryTooltip(utils, key, BuiltInRegistries.BLOCK_ENTITY_TYPE, blockEntityType);
    }

    @Unmodifiable
    @NotNull
    public static ITooltipNode getPotionTooltip(IServerUtils utils, String key, Potion potion) {
        return getBuiltInRegistryTooltip(utils, key, BuiltInRegistries.POTION, potion);
    }

    @Unmodifiable
    @NotNull
    public static ITooltipNode getMobEffectTooltip(IServerUtils utils, String key, MobEffect mobEffect) {
        return getBuiltInRegistryTooltip(utils, key, BuiltInRegistries.MOB_EFFECT, mobEffect);
    }

    @Unmodifiable
    @NotNull
    public static ITooltipNode getLootNbtProviderTypeTooltip(IServerUtils utils, String key, LootNbtProviderType providerType) {
        return getBuiltInRegistryTooltip(utils, key, BuiltInRegistries.LOOT_NBT_PROVIDER_TYPE, providerType);
    }

    @Unmodifiable
    @NotNull
    public static ITooltipNode getFluidTooltip(IServerUtils utils, String key, Fluid fluid) {
        return getBuiltInRegistryTooltip(utils, key, BuiltInRegistries.FLUID, fluid);
    }
    
    @Unmodifiable
    @NotNull
    public static ITooltipNode getEnchantmentTooltip(IServerUtils utils, String key, Enchantment enchantment) {
        return getBuiltInRegistryTooltip(utils, key, BuiltInRegistries.ENCHANTMENT, enchantment);
    }

    @Unmodifiable
    @NotNull
    public static ITooltipNode getAttributeTooltip(IServerUtils utils, String key, Attribute attribute) {
        return getBuiltInRegistryTooltip(utils, key, BuiltInRegistries.ATTRIBUTE, attribute);
    }
}
