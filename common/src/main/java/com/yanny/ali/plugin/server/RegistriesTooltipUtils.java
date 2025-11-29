package com.yanny.ali.plugin.server;

import com.yanny.ali.api.IKeyTooltipNode;
import com.yanny.ali.api.IServerUtils;
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
import net.minecraft.world.item.JukeboxSong;
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
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryType;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.minecraft.world.level.storage.loot.providers.nbt.LootNbtProviderType;
import org.jetbrains.annotations.NotNull;

import static com.yanny.ali.plugin.server.GenericTooltipUtils.getRegistryTooltip;
import static com.yanny.ali.plugin.server.ValueTooltipUtils.getBuiltInRegistryTooltip;

public class RegistriesTooltipUtils {
    @NotNull
    public static IKeyTooltipNode getEntryTypeTooltip(IServerUtils utils, LootPoolEntryType type) {
        return getBuiltInRegistryTooltip(utils, BuiltInRegistries.LOOT_POOL_ENTRY_TYPE, type);
    }

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
        return getRegistryTooltip(utils, Registries.BANNER_PATTERN, bannerPattern);
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
        return getRegistryTooltip(utils, Registries.ENCHANTMENT, enchantment);
    }

    @NotNull
    public static IKeyTooltipNode getAttributeTooltip(IServerUtils utils, Attribute attribute) {
        return getBuiltInRegistryTooltip(utils, BuiltInRegistries.ATTRIBUTE, attribute);
    }

    @NotNull
    public static IKeyTooltipNode getDataComponentTypeTooltip(IServerUtils utils, DataComponentType<?> type) {
        return getBuiltInRegistryTooltip(utils, BuiltInRegistries.DATA_COMPONENT_TYPE, type);
    }

    @NotNull
    public static IKeyTooltipNode getInstrumentTooltip(IServerUtils utils, Instrument value) {
        return getBuiltInRegistryTooltip(utils, BuiltInRegistries.INSTRUMENT, value);
    }

    @NotNull
    public static IKeyTooltipNode getEntitySubPredicateTooltip(IServerUtils utils, EntitySubPredicate predicate) {
        return getBuiltInRegistryTooltip(utils, BuiltInRegistries.ENTITY_SUB_PREDICATE_TYPE, predicate.codec());
    }

    @NotNull
    public static IKeyTooltipNode getCatVariantTooltip(IServerUtils utils, CatVariant catVariant) {
        return getBuiltInRegistryTooltip(utils, BuiltInRegistries.CAT_VARIANT, catVariant);
    }

    @NotNull
    public static IKeyTooltipNode getPaintingVariantTooltip(IServerUtils utils, PaintingVariant paintingVariant) {
        return getRegistryTooltip(utils, Registries.PAINTING_VARIANT, paintingVariant);
    }

    @NotNull
    public static IKeyTooltipNode getFrogVariantTooltip(IServerUtils utils, FrogVariant frogVariant) {
        return getBuiltInRegistryTooltip(utils, BuiltInRegistries.FROG_VARIANT, frogVariant);
    }

    @NotNull
    public static IKeyTooltipNode getWolfVariantTooltip(IServerUtils utils, WolfVariant wolfVariant) {
        return getRegistryTooltip(utils, Registries.WOLF_VARIANT, wolfVariant);
    }

    @NotNull
    public static IKeyTooltipNode getMapDecorationTypeTooltip(IServerUtils utils, MapDecorationType mapDecorationType) {
        return getRegistryTooltip(utils, Registries.MAP_DECORATION_TYPE, mapDecorationType);
    }

    @NotNull
    public static IKeyTooltipNode getBiomeTooltip(IServerUtils utils, Biome biome) {
        return getRegistryTooltip(utils, Registries.BIOME, biome);
    }

    @NotNull
    public static IKeyTooltipNode getStructureTooltip(IServerUtils utils, Structure structure) {
        return getRegistryTooltip(utils, Registries.STRUCTURE, structure);
    }

    @NotNull
    public static IKeyTooltipNode getTrimMaterialTooltip(IServerUtils utils, TrimMaterial material) {
        return getRegistryTooltip(utils, Registries.TRIM_MATERIAL, material);
    }

    @NotNull
    public static IKeyTooltipNode getTrimPatternTooltip(IServerUtils utils, TrimPattern pattern) {
        return getRegistryTooltip(utils, Registries.TRIM_PATTERN, pattern);
    }

    @NotNull
    public static IKeyTooltipNode getJukeboxSongTooltip(IServerUtils utils, JukeboxSong pattern) {
        return getRegistryTooltip(utils, Registries.JUKEBOX_SONG, pattern);
    }
}
