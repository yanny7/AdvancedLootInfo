package com.yanny.ali.plugin.client;

import com.yanny.ali.api.IClientUtils;
import net.minecraft.advancements.critereon.EntitySubPredicate;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
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
import net.minecraft.world.level.storage.loot.providers.nbt.LootNbtProviderType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

import static com.yanny.ali.plugin.client.GenericTooltipUtils.getBuiltInRegistryTooltip;
import static com.yanny.ali.plugin.client.GenericTooltipUtils.getRegistryTooltip;

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
        return getRegistryTooltip(utils, pad, key, Registries.BANNER_PATTERN, bannerPattern);
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
        return getRegistryTooltip(utils, pad, key, Registries.ENCHANTMENT, enchantment);
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getAttributeTooltip(IClientUtils utils, int pad, String key, Attribute attribute) {
        return getBuiltInRegistryTooltip(utils, pad, key, BuiltInRegistries.ATTRIBUTE, attribute);
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getDataComponentTypeTooltip(IClientUtils utils, int pad, String key, DataComponentType<?> type) {
        return getBuiltInRegistryTooltip(utils, pad, key, BuiltInRegistries.DATA_COMPONENT_TYPE, type);
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getInstrumentTooltip(IClientUtils utils, int pad, String key, Instrument value) {
        return getBuiltInRegistryTooltip(utils, pad, key, BuiltInRegistries.INSTRUMENT, value);
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getEntitySubPredicateTooltip(IClientUtils utils, int pad, String key, EntitySubPredicate predicate) {
        return getBuiltInRegistryTooltip(utils, pad, key, BuiltInRegistries.ENTITY_SUB_PREDICATE_TYPE, predicate.codec());
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getCatVariantTooltip(IClientUtils utils, int pad, String key, CatVariant catVariant) {
        return getBuiltInRegistryTooltip(utils, pad, key, BuiltInRegistries.CAT_VARIANT, catVariant);
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getPaintingVariantTooltip(IClientUtils utils, int pad, String key, PaintingVariant paintingVariant) {
        return getRegistryTooltip(utils, pad, key, Registries.PAINTING_VARIANT, paintingVariant);
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getFrogVariantTooltip(IClientUtils utils, int pad, String key, FrogVariant frogVariant) {
        return getBuiltInRegistryTooltip(utils, pad, key, BuiltInRegistries.FROG_VARIANT, frogVariant);
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getWolfVariantTooltip(IClientUtils utils, int pad, String key, WolfVariant wolfVariant) {
        return getRegistryTooltip(utils, pad, key, Registries.WOLF_VARIANT, wolfVariant);
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getMapDecorationTypeTooltip(IClientUtils utils, int pad, String key, MapDecorationType mapDecorationType) {
        return getRegistryTooltip(utils, pad, key, Registries.MAP_DECORATION_TYPE, mapDecorationType);
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getBiomeTooltip(IClientUtils utils, int pad, String key, Biome biome) {
        return getRegistryTooltip(utils, pad, key, Registries.BIOME, biome);
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getStructureTooltip(IClientUtils utils, int pad, String key, Structure structure) {
        return getRegistryTooltip(utils, pad, key, Registries.STRUCTURE, structure);
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getTrimMaterialTooltip(IClientUtils utils, int pad, String key, TrimMaterial material) {
        return getRegistryTooltip(utils, pad, key, Registries.TRIM_MATERIAL, material);
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getTrimPatternTooltip(IClientUtils utils, int pad, String key, TrimPattern pattern) {
        return getRegistryTooltip(utils, pad, key, Registries.TRIM_PATTERN, pattern);
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getJukeboxSongTooltip(IClientUtils utils, int pad, String key, JukeboxSong song) {
        return getRegistryTooltip(utils, pad, key, Registries.JUKEBOX_SONG, song);
    }
}
