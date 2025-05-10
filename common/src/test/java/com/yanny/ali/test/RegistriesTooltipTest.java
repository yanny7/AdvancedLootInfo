package com.yanny.ali.test;

import com.yanny.ali.plugin.client.RegistriesTooltipUtils;
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
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.equipment.trim.TrimMaterials;
import net.minecraft.world.item.equipment.trim.TrimPatterns;
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
        assertTooltip(RegistriesTooltipUtils.getBlockTooltip(UTILS, 1, "ali.property.value.block", Blocks.DIAMOND_BLOCK), List.of("  -> Block: minecraft:diamond_block"));
    }

    @Test
    public void testItemTooltip() {
        assertTooltip(RegistriesTooltipUtils.getItemTooltip(UTILS, 0, "ali.property.value.item", Items.ACACIA_DOOR), List.of("Item: minecraft:acacia_door"));
    }

    @Test
    public void testEntityTypeTooltip() {
        assertTooltip(RegistriesTooltipUtils.getEntityTypeTooltip(UTILS, 0, "ali.property.value.entity_type", EntityType.ALLAY), List.of("Entity Type: minecraft:allay"));
    }

    @Test
    public void testBannerPatternTooltip() {
        assertTooltip(RegistriesTooltipUtils.getBannerPatternTooltip(UTILS, 0, "ali.property.value.banner_pattern", LOOKUP.lookupOrThrow(Registries.BANNER_PATTERN).getOrThrow(BannerPatterns.CREEPER).value()), List.of("Banner Pattern: minecraft:creeper"));
    }

    @Test
    public void testBlockEntityTypeTooltip() {
        assertTooltip(RegistriesTooltipUtils.getBlockEntityTypeTooltip(UTILS, 0, "ali.property.value.block_entity_type", BlockEntityType.BEACON), List.of("Block Entity Type: minecraft:beacon"));
    }

    @Test
    public void testPotionTooltip() {
        assertTooltip(RegistriesTooltipUtils.getPotionTooltip(UTILS, 0, "ali.property.value.potion", Potions.HEALING.value()), List.of("Potion: minecraft:healing"));
    }

    @Test
    public void testMobEffectTooltip() {
        assertTooltip(RegistriesTooltipUtils.getMobEffectTooltip(UTILS, 0, "ali.property.value.effect", MobEffects.BLINDNESS.value()), List.of("Effect: minecraft:blindness"));
    }

    @Test
    public void testLootNbtProviderTypeTooltip() {
        assertTooltip(RegistriesTooltipUtils.getLootNbtProviderTypeTooltip(UTILS, 0, "ali.property.value.nbt", NbtProviders.CONTEXT), List.of("Nbt: minecraft:context"));
    }

    @Test
    public void testFluidTooltip() {
        assertTooltip(RegistriesTooltipUtils.getFluidTooltip(UTILS, 0, "ali.property.value.fluid", Fluids.WATER), List.of("Fluid: minecraft:water"));
    }

    @Test
    public void testEnchantmentTooltip() {
        assertTooltip(RegistriesTooltipUtils.getEnchantmentTooltip(UTILS, 0, "ali.property.value.enchantment", LOOKUP.lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.AQUA_AFFINITY).value()), List.of("Enchantment: minecraft:aqua_affinity"));
    }

    @Test
    public void testAttributeTooltip() {
        assertTooltip(RegistriesTooltipUtils.getAttributeTooltip(UTILS, 0, "ali.property.value.attribute", Attributes.JUMP_STRENGTH.value()), List.of("Attribute: minecraft:jump_strength"));
    }

    @Test
    public void testDataComponentTypeTooltip() {
        assertTooltip(RegistriesTooltipUtils.getDataComponentTypeTooltip(UTILS, 0, "ali.property.value.type", DataComponents.DAMAGE), List.of("Type: minecraft:damage"));
    }

    @Test
    public void testInstrumentTooltip() {
        assertTooltip(RegistriesTooltipUtils.getInstrumentTooltip(UTILS, 0, "ali.property.value.type", LOOKUP.lookupOrThrow(Registries.INSTRUMENT).getOrThrow(Instruments.ADMIRE_GOAT_HORN).value()), List.of("Type: minecraft:admire_goat_horn"));
    }

    @Test
    public void testEntitySubPredicateTooltip() {
        assertTooltip(RegistriesTooltipUtils.getEntitySubPredicateTooltip(UTILS, 0, "ali.property.value.variant", EntitySubPredicates.catVariant(LOOKUP.lookupOrThrow(Registries.CAT_VARIANT).getOrThrow(CatVariant.CALICO))), List.of("Variant: minecraft:cat"));
    }

    @Test
    public void testCatVariantTooltip() {
        assertTooltip(RegistriesTooltipUtils.getCatVariantTooltip(UTILS, 0, "ali.property.value.variant", LOOKUP.lookupOrThrow(Registries.CAT_VARIANT).getOrThrow(CatVariant.CALICO).value()), List.of("Variant: minecraft:calico"));
    }

    @Test
    public void testPaintingVariantTooltip() {
        assertTooltip(RegistriesTooltipUtils.getPaintingVariantTooltip(UTILS, 0, "ali.property.value.variant", LOOKUP.lookupOrThrow(Registries.PAINTING_VARIANT).getOrThrow(PaintingVariants.BOMB).value()), List.of("Variant: minecraft:bomb"));
    }

    @Test
    public void testFrogVariantTooltip() {
    assertTooltip(RegistriesTooltipUtils.getFrogVariantTooltip(UTILS, 0, "ali.property.value.variant", LOOKUP.lookupOrThrow(Registries.FROG_VARIANT).getOrThrow(FrogVariant.TEMPERATE).value()), List.of("Variant: minecraft:temperate"));
    }

    @Test
    public void testWolfVariantTooltip() {
        assertTooltip(RegistriesTooltipUtils.getWolfVariantTooltip(UTILS, 0, "ali.property.value.variant", LOOKUP.lookupOrThrow(Registries.WOLF_VARIANT).getOrThrow(WolfVariants.ASHEN).value()), List.of("Variant: minecraft:ashen"));
    }

    @Test
    public void testMapDecorationTypeTooltip() {
        assertTooltip(RegistriesTooltipUtils.getMapDecorationTypeTooltip(UTILS, 0, "ali.property.value.type", MapDecorationTypes.DESERT_VILLAGE.value()), List.of("Type: minecraft:village_desert"));
    }

    @Test
    public void testBiomeTooltip() {
        assertTooltip(RegistriesTooltipUtils.getBiomeTooltip(UTILS, 0, "ali.property.value.biome", LOOKUP.lookupOrThrow(Registries.BIOME).getOrThrow(Biomes.BADLANDS).value()), List.of("Biome: minecraft:badlands"));
    }

    @Test
    public void testStructureTooltip() {
        assertTooltip(RegistriesTooltipUtils.getStructureTooltip(UTILS, 0, "ali.property.value.structure", LOOKUP.lookupOrThrow(Registries.STRUCTURE).getOrThrow(BuiltinStructures.MINESHAFT).value()), List.of("Structure: minecraft:mineshaft"));
    }

    @Test
    public void testTrimMaterialTooltip() {
        assertTooltip(RegistriesTooltipUtils.getTrimMaterialTooltip(UTILS, 0, "ali.property.value.material", LOOKUP.lookupOrThrow(Registries.TRIM_MATERIAL).getOrThrow(TrimMaterials.AMETHYST).value()), List.of("Material: minecraft:amethyst"));
    }

    @Test
    public void testTrimPatternTooltip() {
        assertTooltip(RegistriesTooltipUtils.getTrimPatternTooltip(UTILS, 0, "ali.property.value.pattern", LOOKUP.lookupOrThrow(Registries.TRIM_PATTERN).getOrThrow(TrimPatterns.DUNE).value()), List.of("Pattern: minecraft:dune"));
    }

    @Test
    public void testJukeboxSongTooltip() {
        assertTooltip(RegistriesTooltipUtils.getJukeboxSongTooltip(UTILS, 0, "ali.property.value.song", LOOKUP.lookupOrThrow(Registries.JUKEBOX_SONG).getOrThrow(JukeboxSongs.CAT).value()), List.of("Song: minecraft:cat"));
    }
}
