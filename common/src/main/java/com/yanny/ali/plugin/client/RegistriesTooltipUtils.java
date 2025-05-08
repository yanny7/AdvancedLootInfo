package com.yanny.ali.plugin.client;

import com.yanny.ali.api.IClientUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
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
import net.minecraft.world.level.storage.loot.providers.nbt.LootNbtProviderType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

import static com.yanny.ali.plugin.client.GenericTooltipUtils.getBuiltInRegistryTooltip;

public class RegistriesTooltipUtils {
    @Unmodifiable
    @NotNull
    public static List<Component> getBlockTooltip(IClientUtils utils, int pad, String key, Block block) {
        return getBuiltInRegistryTooltip(utils, pad, key, BuiltInRegistries.BLOCK, block);
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getItemTooltip(IClientUtils utils, int pad, String key, Item item) {
        return getBuiltInRegistryTooltip(utils, pad, key, BuiltInRegistries.ITEM, item);
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getEntityTypeTooltip(IClientUtils utils, int pad, String key, EntityType<?> entityType) {
        return getBuiltInRegistryTooltip(utils, pad, key, BuiltInRegistries.ENTITY_TYPE, entityType);
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getBannerPatternTooltip(IClientUtils utils, int pad, String key, BannerPattern bannerPattern) {
        return getBuiltInRegistryTooltip(utils, pad, key, BuiltInRegistries.BANNER_PATTERN, bannerPattern);
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getBlockEntityTypeTooltip(IClientUtils utils, int pad, String key, BlockEntityType<?> blockEntityType) {
        return getBuiltInRegistryTooltip(utils, pad, key, BuiltInRegistries.BLOCK_ENTITY_TYPE, blockEntityType);
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getPotionTooltip(IClientUtils utils, int pad, String key, Potion potion) {
        return getBuiltInRegistryTooltip(utils, pad, key, BuiltInRegistries.POTION, potion);
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getMobEffectTooltip(IClientUtils utils, int pad, String key, MobEffect mobEffect) {
        return getBuiltInRegistryTooltip(utils, pad, key, BuiltInRegistries.MOB_EFFECT, mobEffect);
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getLootNbtProviderTypeTooltip(IClientUtils utils, int pad, String key, LootNbtProviderType providerType) {
        return getBuiltInRegistryTooltip(utils, pad, key, BuiltInRegistries.LOOT_NBT_PROVIDER_TYPE, providerType);
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getFluidTooltip(IClientUtils utils, int pad, String key, Fluid fluid) {
        return getBuiltInRegistryTooltip(utils, pad, key, BuiltInRegistries.FLUID, fluid);
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getEnchantmentTooltip(IClientUtils utils, int pad, String key, Enchantment enchantment) {
        return getBuiltInRegistryTooltip(utils, pad, key, BuiltInRegistries.ENCHANTMENT, enchantment);
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getAttributeTooltip(IClientUtils utils, int pad, String key, Attribute attribute) {
        return getBuiltInRegistryTooltip(utils, pad, key, BuiltInRegistries.ATTRIBUTE, attribute);
    }
}
