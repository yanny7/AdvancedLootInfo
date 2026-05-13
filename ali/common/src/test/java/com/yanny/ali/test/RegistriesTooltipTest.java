package com.yanny.ali.test;

import com.yanny.ali.language.Lang;
import com.yanny.ali.plugin.server.RegistriesTooltipUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BannerPatterns;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.loot.providers.nbt.NbtProviders;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Objects;

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
        assertTooltip(RegistriesTooltipUtils.getEntityTypeTooltip(UTILS, EntityType.ALLAY).build(Lang.Value.ENTITY_TYPE), List.of("Entity Type: minecraft:allay"));
    }

    @Test
    public void testBannerPatternTooltip() {
        assertTooltip(RegistriesTooltipUtils.getBannerPatternTooltip(UTILS, Objects.requireNonNull(BuiltInRegistries.BANNER_PATTERN.get(BannerPatterns.CREEPER))).build(Lang.Value.BANNER_PATTERN), List.of("Banner Pattern: minecraft:creeper"));
    }

    @Test
    public void testBlockEntityTypeTooltip() {
        assertTooltip(RegistriesTooltipUtils.getBlockEntityTypeTooltip(UTILS, BlockEntityType.BEACON).build(Lang.Value.BLOCK_ENTITY_TYPE), List.of("Block Entity Type: minecraft:beacon"));
    }

    @Test
    public void testPotionTooltip() {
        assertTooltip(RegistriesTooltipUtils.getPotionTooltip(UTILS, Potions.HEALING).build(Lang.Value.POTION), List.of("Potion: minecraft:healing"));
    }

    @Test
    public void testMobEffectTooltip() {
        assertTooltip(RegistriesTooltipUtils.getMobEffectTooltip(UTILS, MobEffects.BLINDNESS).build(Lang.Value.MOB_EFFECT), List.of("Mob Effect: minecraft:blindness"));
    }

    @Test
    public void testLootNbtProviderTypeTooltip() {
        assertTooltip(RegistriesTooltipUtils.getLootNbtProviderTypeTooltip(UTILS, NbtProviders.CONTEXT).build(Lang.Value.NBT_PROVIDER), List.of("Nbt Provider: minecraft:context"));
    }

    @Test
    public void testFluidTooltip() {
        assertTooltip(RegistriesTooltipUtils.getFluidTooltip(UTILS, Fluids.WATER).build(Lang.Value.FLUID), List.of("Fluid: minecraft:water"));
    }

    @Test
    public void testEnchantmentTooltip() {
        assertTooltip(RegistriesTooltipUtils.getEnchantmentTooltip(UTILS, Enchantments.AQUA_AFFINITY).build(Lang.Value.ENCHANTMENT), List.of("Enchantment: minecraft:aqua_affinity"));
    }

    @Test
    public void testAttributeTooltip() {
        assertTooltip(RegistriesTooltipUtils.getAttributeTooltip(UTILS, Attributes.JUMP_STRENGTH).build(Lang.Value.ATTRIBUTE), List.of("Attribute: minecraft:horse.jump_strength"));
    }
}
