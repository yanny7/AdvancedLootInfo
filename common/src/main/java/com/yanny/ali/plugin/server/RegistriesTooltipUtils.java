package com.yanny.ali.plugin.server;

import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.api.ITooltipNode;
import net.minecraft.advancements.critereon.EntitySubPredicate;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
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
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.equipment.trim.TrimMaterial;
import net.minecraft.world.item.equipment.trim.TrimPattern;
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
import org.jetbrains.annotations.Unmodifiable;

import static com.yanny.ali.plugin.server.GenericTooltipUtils.getBuiltInRegistryTooltip;
import static com.yanny.ali.plugin.server.GenericTooltipUtils.getRegistryTooltip;

public class RegistriesTooltipUtils {
    @Unmodifiable
    @NotNull
    public static ITooltipNode getFunctionTypeTooltip(IServerUtils utils, String key, LootItemFunctionType<?> type) {
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
        return getRegistryTooltip(utils, key, Registries.BANNER_PATTERN, bannerPattern);
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
        return getRegistryTooltip(utils, key, Registries.ENCHANTMENT, enchantment);
    }

    @Unmodifiable
    @NotNull
    public static ITooltipNode getAttributeTooltip(IServerUtils utils, String key, Attribute attribute) {
        return getBuiltInRegistryTooltip(utils, key, BuiltInRegistries.ATTRIBUTE, attribute);
    }

    @Unmodifiable
    @NotNull
    public static ITooltipNode getDataComponentTypeTooltip(IServerUtils utils, String key, DataComponentType<?> type) {
        return getBuiltInRegistryTooltip(utils, key, BuiltInRegistries.DATA_COMPONENT_TYPE, type);
    }

    @Unmodifiable
    @NotNull
    public static ITooltipNode getInstrumentTooltip(IServerUtils utils, String key, Instrument value) {
        return getRegistryTooltip(utils, key, Registries.INSTRUMENT, value);
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
        return getRegistryTooltip(utils, key, Registries.PAINTING_VARIANT, paintingVariant);
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

    @Unmodifiable
    @NotNull
    public static ITooltipNode getJukeboxSongTooltip(IServerUtils utils, String key, JukeboxSong pattern) {
        return getRegistryTooltip(utils, key, Registries.JUKEBOX_SONG, pattern);
    }

    @Unmodifiable
    @NotNull
    public static ITooltipNode getSoundEventTooltip(IServerUtils utils, String key, SoundEvent soundEvent) {
        return getRegistryTooltip(utils, key, Registries.SOUND_EVENT, soundEvent);
    }
}
