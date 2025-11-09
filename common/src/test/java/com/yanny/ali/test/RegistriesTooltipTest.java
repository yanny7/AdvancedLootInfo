package com.yanny.ali.test;

import com.yanny.ali.plugin.server.RegistriesTooltipUtils;
import net.minecraft.advancements.critereon.EntitySubPredicates;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.CatVariant;
import net.minecraft.world.entity.animal.FrogVariant;
import net.minecraft.world.entity.animal.WolfVariants;
import net.minecraft.world.entity.decoration.PaintingVariants;
import net.minecraft.world.item.Instruments;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.JukeboxSongs;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.armortrim.TrimMaterials;
import net.minecraft.world.item.armortrim.TrimPatterns;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BannerPatterns;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.levelgen.structure.BuiltinStructures;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.saveddata.maps.MapDecorationTypes;
import net.minecraft.world.level.storage.loot.providers.nbt.NbtProviders;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.yanny.ali.test.TooltipTestSuite.LOOKUP;
import static com.yanny.ali.test.TooltipTestSuite.UTILS;
import static com.yanny.ali.test.utils.TestUtils.assertTooltip;

public class RegistriesTooltipTest {
    @Test
    public void testBlockTooltip() {
        assertTooltip(RegistriesTooltipUtils.getBlockTooltip(UTILS, Blocks.DIAMOND_BLOCK).key("ali.property.value.block"), List.of("Block: minecraft:diamond_block"));
    }

    @Test
    public void testItemTooltip() {
        assertTooltip(RegistriesTooltipUtils.getItemTooltip(UTILS, Items.ACACIA_DOOR).key("ali.property.value.item"), List.of("Item: minecraft:acacia_door"));
    }

    @Test
    public void testEntityTypeTooltip() {
        assertTooltip(RegistriesTooltipUtils.getEntityTypeTooltip(UTILS, EntityType.ALLAY).key("ali.property.value.entity_type"), List.of("Entity Type: minecraft:allay"));
    }

    @Test
    public void testBannerPatternTooltip() {
        assertTooltip(RegistriesTooltipUtils.getBannerPatternTooltip(UTILS, LOOKUP.lookupOrThrow(Registries.BANNER_PATTERN).getOrThrow(BannerPatterns.CREEPER).value()).key("ali.property.value.banner_pattern"), List.of("Banner Pattern: minecraft:creeper"));
    }

    @Test
    public void testBlockEntityTypeTooltip() {
        assertTooltip(RegistriesTooltipUtils.getBlockEntityTypeTooltip(UTILS, BlockEntityType.BEACON).key("ali.property.value.block_entity_type"), List.of("Block Entity Type: minecraft:beacon"));
    }

    @Test
    public void testPotionTooltip() {
        assertTooltip(RegistriesTooltipUtils.getPotionTooltip(UTILS, Potions.HEALING.value()).key("ali.property.value.potion"), List.of("Potion: minecraft:healing"));
    }

    @Test
    public void testMobEffectTooltip() {
        assertTooltip(RegistriesTooltipUtils.getMobEffectTooltip(UTILS, MobEffects.BLINDNESS.value()).key("ali.property.value.effect"), List.of("Effect: minecraft:blindness"));
    }

    @Test
    public void testLootNbtProviderTypeTooltip() {
        assertTooltip(RegistriesTooltipUtils.getLootNbtProviderTypeTooltip(UTILS, NbtProviders.CONTEXT).key("ali.property.value.nbt"), List.of("Nbt: minecraft:context"));
    }

    @Test
    public void testFluidTooltip() {
        assertTooltip(RegistriesTooltipUtils.getFluidTooltip(UTILS, Fluids.WATER).key("ali.property.value.fluid"), List.of("Fluid: minecraft:water"));
    }

    @Test
    public void testEnchantmentTooltip() {
        assertTooltip(RegistriesTooltipUtils.getEnchantmentTooltip(UTILS, LOOKUP.lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.AQUA_AFFINITY).value()).key("ali.property.value.enchantment"), List.of("Enchantment: minecraft:aqua_affinity"));
    }

    @Test
    public void testAttributeTooltip() {
        assertTooltip(RegistriesTooltipUtils.getAttributeTooltip(UTILS, Attributes.JUMP_STRENGTH.value()).key("ali.property.value.attribute"), List.of("Attribute: minecraft:generic.jump_strength"));
    }

    @Test
    public void testDataComponentTypeTooltip() {
        assertTooltip(RegistriesTooltipUtils.getDataComponentTypeTooltip(UTILS, DataComponents.DAMAGE).key("ali.property.value.type"), List.of("Type: minecraft:damage"));
    }

    @Test
    public void testInstrumentTooltip() {
        assertTooltip(RegistriesTooltipUtils.getInstrumentTooltip(UTILS, LOOKUP.lookupOrThrow(Registries.INSTRUMENT).getOrThrow(Instruments.ADMIRE_GOAT_HORN).value()).key("ali.property.value.type"), List.of("Type: minecraft:admire_goat_horn"));
    }

    @Test
    public void testEntitySubPredicateTooltip() {
        assertTooltip(RegistriesTooltipUtils.getEntitySubPredicateTooltip(UTILS, EntitySubPredicates.catVariant(LOOKUP.lookupOrThrow(Registries.CAT_VARIANT).getOrThrow(CatVariant.CALICO))).key("ali.property.value.variant"), List.of("Variant: minecraft:cat"));
    }

    @Test
    public void testCatVariantTooltip() {
        assertTooltip(RegistriesTooltipUtils.getCatVariantTooltip(UTILS, LOOKUP.lookupOrThrow(Registries.CAT_VARIANT).getOrThrow(CatVariant.CALICO).value()).key("ali.property.value.variant"), List.of("Variant: minecraft:calico"));
    }

    @Test
    public void testPaintingVariantTooltip() {
        assertTooltip(RegistriesTooltipUtils.getPaintingVariantTooltip(UTILS, LOOKUP.lookupOrThrow(Registries.PAINTING_VARIANT).getOrThrow(PaintingVariants.BOMB).value()).key("ali.property.value.variant"), List.of("Variant: minecraft:bomb"));
    }

    @Test
    public void testFrogVariantTooltip() {
    assertTooltip(RegistriesTooltipUtils.getFrogVariantTooltip(UTILS, LOOKUP.lookupOrThrow(Registries.FROG_VARIANT).getOrThrow(FrogVariant.TEMPERATE).value()).key("ali.property.value.variant"), List.of("Variant: minecraft:temperate"));
    }

    @Test
    public void testWolfVariantTooltip() {
        assertTooltip(RegistriesTooltipUtils.getWolfVariantTooltip(UTILS, LOOKUP.lookupOrThrow(Registries.WOLF_VARIANT).getOrThrow(WolfVariants.ASHEN).value()).key("ali.property.value.variant"), List.of("Variant: minecraft:ashen"));
    }

    @Test
    public void testMapDecorationTypeTooltip() {
        assertTooltip(RegistriesTooltipUtils.getMapDecorationTypeTooltip(UTILS, MapDecorationTypes.DESERT_VILLAGE.value()).key("ali.property.value.type"), List.of("Type: minecraft:village_desert"));
    }

    @Test
    public void testBiomeTooltip() {
        assertTooltip(RegistriesTooltipUtils.getBiomeTooltip(UTILS, LOOKUP.lookupOrThrow(Registries.BIOME).getOrThrow(Biomes.BADLANDS).value()).key("ali.property.value.biome"), List.of("Biome: minecraft:badlands"));
    }

    @Test
    public void testStructureTooltip() {
        assertTooltip(RegistriesTooltipUtils.getStructureTooltip(UTILS, LOOKUP.lookupOrThrow(Registries.STRUCTURE).getOrThrow(BuiltinStructures.MINESHAFT).value()).key("ali.property.value.structure"), List.of("Structure: minecraft:mineshaft"));
    }

    @Test
    public void testTrimMaterialTooltip() {
        assertTooltip(RegistriesTooltipUtils.getTrimMaterialTooltip(UTILS, LOOKUP.lookupOrThrow(Registries.TRIM_MATERIAL).getOrThrow(TrimMaterials.AMETHYST).value()).key("ali.property.value.material"), List.of("Material: minecraft:amethyst"));
    }

    @Test
    public void testTrimPatternTooltip() {
        assertTooltip(RegistriesTooltipUtils.getTrimPatternTooltip(UTILS, LOOKUP.lookupOrThrow(Registries.TRIM_PATTERN).getOrThrow(TrimPatterns.DUNE).value()).key("ali.property.value.pattern"), List.of("Pattern: minecraft:dune"));
    }

    @Test
    public void testJukeboxSongTooltip() {
        assertTooltip(RegistriesTooltipUtils.getJukeboxSongTooltip(UTILS, LOOKUP.lookupOrThrow(Registries.JUKEBOX_SONG).getOrThrow(JukeboxSongs.CAT).value()).key("ali.property.value.song"), List.of("Song: minecraft:cat"));
    }
}
