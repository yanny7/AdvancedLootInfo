package com.yanny.ali.plugin.server;

import com.mojang.logging.LogUtils;
import com.yanny.ali.api.IKeyTooltipNode;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.plugin.common.tooltip.ComponentTooltipNode;
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
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryType;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.minecraft.world.level.storage.loot.providers.nbt.LootNbtProviderType;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import static com.yanny.ali.plugin.server.ValueTooltipUtils.getBuiltInRegistryTooltip;

public class RegistriesTooltipUtils {
    private static final Logger LOGGER = LogUtils.getLogger();

    @NotNull
    public static IKeyTooltipNode getEntryTypeTooltip(IServerUtils utils, LootPoolEntryType type) {
        return getBuiltInRegistryTooltip(utils, BuiltInRegistries.LOOT_POOL_ENTRY_TYPE, type);
    }

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
        if (utils.getConfiguration().showInGameNames) {
            try {
                return ComponentTooltipNode.values(block.getName());
            } catch (Throwable ignored) {
                LOGGER.warn("Failed to get localized Block name: {}", BuiltInRegistries.BLOCK.getKey(block));
            }
        }

        return getBuiltInRegistryTooltip(utils, BuiltInRegistries.BLOCK, block);
    }

    @NotNull
    public static IKeyTooltipNode getItemTooltip(IServerUtils utils, Item item) {
        if (utils.getConfiguration().showInGameNames) {
            try {
                return ComponentTooltipNode.values(item.getName(item.getDefaultInstance()));
            } catch (Throwable ignored) {
                LOGGER.warn("Failed to get localized Item name: {}", BuiltInRegistries.ITEM.getKey(item));
            }
        }

        return getBuiltInRegistryTooltip(utils, BuiltInRegistries.ITEM, item);
    }

    @NotNull
    public static IKeyTooltipNode getEntityTypeTooltip(IServerUtils utils, EntityType<?> entityType) {
        if (utils.getConfiguration().showInGameNames) {
            try {
                return ComponentTooltipNode.values(entityType.getDescription());
            } catch (Throwable ignored) {
                LOGGER.warn("Failed to get localized EntityType name: {}", BuiltInRegistries.ENTITY_TYPE.getKey(entityType));
            }
        }

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
        if (utils.getConfiguration().showInGameNames) {
            try {
                return ComponentTooltipNode.values(mobEffect.getDisplayName());
            } catch (Throwable ignored) {
                LOGGER.warn("Failed to get localized MobEffect name: {}", BuiltInRegistries.MOB_EFFECT.getKey(mobEffect));
            }
        }

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
        if (utils.getConfiguration().showInGameNames) {
            try {
                return ComponentTooltipNode.values(enchantment.getFullname(1));
            } catch (Throwable ignored) {
                LOGGER.warn("Failed to get localized Enchantment name: {}", BuiltInRegistries.ENCHANTMENT.getKey(enchantment));
            }
        }

        return getBuiltInRegistryTooltip(utils, BuiltInRegistries.ENCHANTMENT, enchantment);
    }

    @NotNull
    public static IKeyTooltipNode getAttributeTooltip(IServerUtils utils, Attribute attribute) {
        if (utils.getConfiguration().showInGameNames) {
            try {
                return ComponentTooltipNode.values(Component.translatable(attribute.getDescriptionId()));
            } catch (Throwable ignored) {
                LOGGER.warn("Failed to get localized Attribute name: {}", BuiltInRegistries.ATTRIBUTE.getKey(attribute));
            }
        }

        return getBuiltInRegistryTooltip(utils, BuiltInRegistries.ATTRIBUTE, attribute);
    }
}
