package com.yanny.ali.test;

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
        assertTooltip(RegistriesTooltipUtils.getBannerPatternTooltip(UTILS, Objects.requireNonNull(BuiltInRegistries.BANNER_PATTERN.get(BannerPatterns.CREEPER))).key("ali.property.value.banner_pattern"), List.of("Banner Pattern: minecraft:creeper"));
    }

    @Test
    public void testBlockEntityTypeTooltip() {
        assertTooltip(RegistriesTooltipUtils.getBlockEntityTypeTooltip(UTILS, BlockEntityType.BEACON).key("ali.property.value.block_entity_type"), List.of("Block Entity Type: minecraft:beacon"));
    }

    @Test
    public void testPotionTooltip() {
        assertTooltip(RegistriesTooltipUtils.getPotionTooltip(UTILS, Potions.HEALING).key("ali.property.value.potion"), List.of("Potion: minecraft:healing"));
    }

    @Test
    public void testMobEffectTooltip() {
        assertTooltip(RegistriesTooltipUtils.getMobEffectTooltip(UTILS, MobEffects.BLINDNESS).key("ali.property.value.mob_effect"), List.of("Mob Effect: minecraft:blindness"));
    }

    @Test
    public void testLootNbtProviderTypeTooltip() {
        assertTooltip(RegistriesTooltipUtils.getLootNbtProviderTypeTooltip(UTILS, NbtProviders.CONTEXT).key("ali.property.value.nbt_provider"), List.of("Nbt Provider: minecraft:context"));
    }

    @Test
    public void testFluidTooltip() {
        assertTooltip(RegistriesTooltipUtils.getFluidTooltip(UTILS, Fluids.WATER).key("ali.property.value.fluid"), List.of("Fluid: minecraft:water"));
    }

    @Test
    public void testEnchantmentTooltip() {
        assertTooltip(RegistriesTooltipUtils.getEnchantmentTooltip(UTILS, Enchantments.AQUA_AFFINITY).key("ali.property.value.enchantment"), List.of("Enchantment: minecraft:aqua_affinity"));
    }

    @Test
    public void testAttributeTooltip() {
        assertTooltip(RegistriesTooltipUtils.getAttributeTooltip(UTILS, Attributes.JUMP_STRENGTH).key("ali.property.value.attribute"), List.of("Attribute: minecraft:horse.jump_strength"));
    }
}
