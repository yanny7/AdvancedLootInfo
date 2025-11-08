package com.yanny.ali.plugin.server;

import com.yanny.ali.api.IKeyTooltipNode;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.api.ITooltipNode;
import net.minecraft.advancements.critereon.EntitySubPredicate;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.animal.CatVariant;
import net.minecraft.world.entity.animal.FrogVariant;
import net.minecraft.world.entity.animal.WolfVariant;
import net.minecraft.world.entity.decoration.PaintingVariant;
import net.minecraft.world.item.Instrument;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.armortrim.TrimMaterial;
import net.minecraft.world.item.armortrim.TrimPattern;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.saveddata.maps.MapDecorationType;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.minecraft.world.level.storage.loot.providers.nbt.LootNbtProviderType;
import org.jetbrains.annotations.NotNull;

import static com.yanny.ali.plugin.server.ValueTooltipUtils.getBuiltInRegistryTooltip;

public class RegistriesTooltipUtils {
    @NotNull
    public static IKeyTooltipNode getFunctionTypeTooltip(IServerUtils utils, LootItemFunctionType<?> type) {
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
        return getBuiltInRegistryTooltip(utils, Registries.BANNER_PATTERN, bannerPattern);
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

    @Unmodifiable
    @NotNull
    public static ITooltipNode getDataComponentTypeTooltip(IServerUtils utils, String key, DataComponentType<?> type) {
        return getBuiltInRegistryTooltip(utils, key, BuiltInRegistries.DATA_COMPONENT_TYPE, type);
    }

    @Unmodifiable
    @NotNull
    public static ITooltipNode getInstrumentTooltip(IServerUtils utils, String key, Instrument value) {
        return getBuiltInRegistryTooltip(utils, key, BuiltInRegistries.INSTRUMENT, value);
    }

    @Unmodifiable
    @NotNull
    public static ITooltipNode getEntitySubPredicateTooltip(IServerUtils utils, String key, EntitySubPredicate predicate) {
        return getBuiltInRegistryTooltip(utils, key, BuiltInRegistries.ENTITY_SUB_PREDICATE_TYPE, predicate.codec());
    }

    @Unmodifiable
    @NotNull
    public static ITooltipNode getCatVariantTooltip(IServerUtils utils, String key, CatVariant catVariant) {
        return getBuiltInRegistryTooltip(utils, key, BuiltInRegistries.CAT_VARIANT, catVariant);
    }

    @Unmodifiable
    @NotNull
    public static ITooltipNode getPaintingVariantTooltip(IServerUtils utils, String key, PaintingVariant paintingVariant) {
        return getBuiltInRegistryTooltip(utils, key, BuiltInRegistries.PAINTING_VARIANT, paintingVariant);
    }

    @Unmodifiable
    @NotNull
    public static ITooltipNode getFrogVariantTooltip(IServerUtils utils, String key, FrogVariant frogVariant) {
        return getBuiltInRegistryTooltip(utils, key, BuiltInRegistries.FROG_VARIANT, frogVariant);
    }

    @Unmodifiable
    @NotNull
    public static ITooltipNode getWolfVariantTooltip(IServerUtils utils, String key, WolfVariant wolfVariant) {
        return getRegistryTooltip(utils, key, Registries.WOLF_VARIANT, wolfVariant);
    }

    @Unmodifiable
    @NotNull
    public static ITooltipNode getMapDecorationTypeTooltip(IServerUtils utils, String key, MapDecorationType mapDecorationType) {
        return getRegistryTooltip(utils, key, Registries.MAP_DECORATION_TYPE, mapDecorationType);
    }

    @Unmodifiable
    @NotNull
    public static ITooltipNode getBiomeTooltip(IServerUtils utils, String key, Biome biome) {
        return getRegistryTooltip(utils, key, Registries.BIOME, biome);
    }

    @Unmodifiable
    @NotNull
    public static ITooltipNode getStructureTooltip(IServerUtils utils, String key, Structure structure) {
        return getRegistryTooltip(utils, key, Registries.STRUCTURE, structure);
    }

    @Unmodifiable
    @NotNull
    public static ITooltipNode getTrimMaterialTooltip(IServerUtils utils, String key, TrimMaterial material) {
        return getRegistryTooltip(utils, key, Registries.TRIM_MATERIAL, material);
    }

    @Unmodifiable
    @NotNull
    public static ITooltipNode getTrimPatternTooltip(IServerUtils utils, String key, TrimPattern pattern) {
        return getRegistryTooltip(utils, key, Registries.TRIM_PATTERN, pattern);
    }
}
