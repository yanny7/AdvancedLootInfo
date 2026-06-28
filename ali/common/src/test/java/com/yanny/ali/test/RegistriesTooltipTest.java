package com.yanny.ali.test;

import com.yanny.ali.language.Lang;
import com.yanny.ali.plugin.server.RegistriesTooltipUtils;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.feline.CatVariants;
import net.minecraft.world.entity.animal.frog.FrogVariants;
import net.minecraft.world.entity.animal.wolf.WolfVariants;
import net.minecraft.world.entity.decoration.painting.PaintingVariants;
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
import net.minecraft.world.level.block.entity.BlockEntityTypes;
import net.minecraft.world.level.levelgen.structure.BuiltinStructures;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.saveddata.maps.MapDecorationTypes;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.providers.nbt.ContextNbtProvider;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.yanny.ali.test.TooltipTestSuite.LOOKUP;
import static com.yanny.ali.test.TooltipTestSuite.UTILS;
import static com.yanny.ali.test.utils.TestUtils.assertTooltip;

public class RegistriesTooltipTest {
    @Test
    public void testBlockTooltip() {
        assertTooltip(RegistriesTooltipUtils.getBlockTooltip(UTILS, Blocks.DIAMOND_BLOCK).build(Lang.Value.BLOCK), List.of("Block: minecraft:diamond_block"));
    }

    @Test
    public void testItemTooltip() {
        assertTooltip(RegistriesTooltipUtils.getItemTooltip(UTILS, Items.ACACIA_DOOR).build(Lang.Value.ITEM), List.of("Item: minecraft:acacia_door"));
    }

    @Test
    public void testEntityTypeTooltip() {
        assertTooltip(RegistriesTooltipUtils.getEntityTypeTooltip(UTILS, EntityTypes.ALLAY).build(Lang.Value.ENTITY_TYPE), List.of("Entity Type: minecraft:allay"));
    }

    @Test
    public void testBannerPatternTooltip() {
        assertTooltip(RegistriesTooltipUtils.getBannerPatternTooltip(UTILS, LOOKUP.lookupOrThrow(Registries.BANNER_PATTERN).getOrThrow(BannerPatterns.CREEPER).value()).build(Lang.Value.BANNER_PATTERN), List.of("Banner Pattern: minecraft:creeper"));
    }

    @Test
    public void testBlockEntityTypeTooltip() {
        assertTooltip(RegistriesTooltipUtils.getBlockEntityTypeTooltip(UTILS, BlockEntityTypes.BEACON).build(Lang.Value.BLOCK_ENTITY_TYPE), List.of("Block Entity Type: minecraft:beacon"));
    }

    @Test
    public void testPotionTooltip() {
        assertTooltip(RegistriesTooltipUtils.getPotionTooltip(UTILS, Potions.HEALING.value()).build(Lang.Value.POTION), List.of("Potion: minecraft:healing"));
    }

    @Test
    public void testMobEffectTooltip() {
        assertTooltip(RegistriesTooltipUtils.getMobEffectTooltip(UTILS, MobEffects.BLINDNESS.value()).build(Lang.Value.EFFECT), List.of("Effect: minecraft:blindness"));
    }

    @Test
    public void testLootNbtProviderTypeTooltip() {
        assertTooltip(RegistriesTooltipUtils.getNbtProviderTooltip(UTILS, ContextNbtProvider.forContextEntity(LootContext.EntityTarget.ATTACKING_PLAYER)).build(Lang.Value.NBT), List.of("Nbt: minecraft:context"));
    }

    @Test
    public void testFluidTooltip() {
        assertTooltip(RegistriesTooltipUtils.getFluidTooltip(UTILS, Fluids.WATER).build(Lang.Value.FLUID), List.of("Fluid: minecraft:water"));
    }

    @Test
    public void testEnchantmentTooltip() {
        assertTooltip(RegistriesTooltipUtils.getEnchantmentTooltip(UTILS, LOOKUP.lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.AQUA_AFFINITY).value()).build(Lang.Value.ENCHANTMENT), List.of("Enchantment: minecraft:aqua_affinity"));
    }

    @Test
    public void testAttributeTooltip() {
        assertTooltip(RegistriesTooltipUtils.getAttributeTooltip(UTILS, Attributes.JUMP_STRENGTH.value()).build(Lang.Value.ATTRIBUTE), List.of("Attribute: minecraft:jump_strength"));
    }

    @Test
    public void testDataComponentTypeTooltip() {
        assertTooltip(RegistriesTooltipUtils.getDataComponentTypeTooltip(UTILS, DataComponents.DAMAGE).build(Lang.Value.TYPE), List.of("Type: minecraft:damage"));
    }

    @Test
    public void testInstrumentTooltip() {
        assertTooltip(RegistriesTooltipUtils.getInstrumentTooltip(UTILS, LOOKUP.lookupOrThrow(Registries.INSTRUMENT).getOrThrow(Instruments.ADMIRE_GOAT_HORN).value()).build(Lang.Value.TYPE), List.of("Type: minecraft:admire_goat_horn"));
    }

    @Test
    public void testCatVariantTooltip() {
        assertTooltip(RegistriesTooltipUtils.getCatVariantTooltip(UTILS, LOOKUP.lookupOrThrow(Registries.CAT_VARIANT).getOrThrow(CatVariants.CALICO).value()).build(Lang.Value.VARIANT), List.of("Variant: minecraft:calico"));
    }

    @Test
    public void testPaintingVariantTooltip() {
        assertTooltip(RegistriesTooltipUtils.getPaintingVariantTooltip(UTILS, LOOKUP.lookupOrThrow(Registries.PAINTING_VARIANT).getOrThrow(PaintingVariants.BOMB).value()).build(Lang.Value.VARIANT), List.of("Variant: minecraft:bomb"));
    }

    @Test
    public void testFrogVariantTooltip() {
    assertTooltip(RegistriesTooltipUtils.getFrogVariantTooltip(UTILS, LOOKUP.lookupOrThrow(Registries.FROG_VARIANT).getOrThrow(FrogVariants.TEMPERATE).value()).build(Lang.Value.VARIANT), List.of("Variant: minecraft:temperate"));
    }

    @Test
    public void testWolfVariantTooltip() {
        assertTooltip(RegistriesTooltipUtils.getWolfVariantTooltip(UTILS, LOOKUP.lookupOrThrow(Registries.WOLF_VARIANT).getOrThrow(WolfVariants.ASHEN).value()).build(Lang.Value.VARIANT), List.of("Variant: minecraft:ashen"));
    }

    @Test
    public void testMapDecorationTypeTooltip() {
        assertTooltip(RegistriesTooltipUtils.getMapDecorationTypeTooltip(UTILS, MapDecorationTypes.DESERT_VILLAGE.value()).build(Lang.Value.TYPE), List.of("Type: minecraft:village_desert"));
    }

    @Test
    public void testBiomeTooltip() {
        assertTooltip(RegistriesTooltipUtils.getBiomeTooltip(UTILS, LOOKUP.lookupOrThrow(Registries.BIOME).getOrThrow(Biomes.BADLANDS).value()).build(Lang.Value.BIOME), List.of("Biome: minecraft:badlands"));
    }

    @Test
    public void testStructureTooltip() {
        assertTooltip(RegistriesTooltipUtils.getStructureTooltip(UTILS, LOOKUP.lookupOrThrow(Registries.STRUCTURE).getOrThrow(BuiltinStructures.MINESHAFT).value()).build(Lang.Value.STRUCTURE), List.of("Structure: minecraft:mineshaft"));
    }

    @Test
    public void testTrimMaterialTooltip() {
        assertTooltip(RegistriesTooltipUtils.getTrimMaterialTooltip(UTILS, LOOKUP.lookupOrThrow(Registries.TRIM_MATERIAL).getOrThrow(TrimMaterials.AMETHYST).value()).build(Lang.Value.MATERIAL), List.of("Material: minecraft:amethyst"));
    }

    @Test
    public void testTrimPatternTooltip() {
        assertTooltip(RegistriesTooltipUtils.getTrimPatternTooltip(UTILS, LOOKUP.lookupOrThrow(Registries.TRIM_PATTERN).getOrThrow(TrimPatterns.DUNE).value()).build(Lang.Value.PATTERN), List.of("Pattern: minecraft:dune"));
    }

    @Test
    public void testJukeboxSongTooltip() {
        assertTooltip(RegistriesTooltipUtils.getJukeboxSongTooltip(UTILS, LOOKUP.lookupOrThrow(Registries.JUKEBOX_SONG).getOrThrow(JukeboxSongs.CAT).value()).build(Lang.Value.SONG), List.of("Song: minecraft:cat"));
    }
}
