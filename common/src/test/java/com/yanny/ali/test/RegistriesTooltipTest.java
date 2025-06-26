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
        assertTooltip(RegistriesTooltipUtils.getBlockTooltip(UTILS, "ali.property.value.block", Blocks.DIAMOND_BLOCK), List.of("Block: minecraft:diamond_block"));
    }

    @Test
    public void testItemTooltip() {
        assertTooltip(RegistriesTooltipUtils.getItemTooltip(UTILS, "ali.property.value.item", Items.ACACIA_DOOR), List.of("Item: minecraft:acacia_door"));
    }

    @Test
    public void testEntityTypeTooltip() {
        assertTooltip(RegistriesTooltipUtils.getEntityTypeTooltip(UTILS, "ali.property.value.entity_type", EntityType.ALLAY), List.of("Entity Type: minecraft:allay"));
    }

    @Test
    public void testBannerPatternTooltip() {
        assertTooltip(RegistriesTooltipUtils.getBannerPatternTooltip(UTILS, "ali.property.value.banner_pattern", Objects.requireNonNull(BuiltInRegistries.BANNER_PATTERN.get(BannerPatterns.CREEPER))), List.of("Banner Pattern: minecraft:creeper"));
    }

    @Test
    public void testBlockEntityTypeTooltip() {
        assertTooltip(RegistriesTooltipUtils.getBlockEntityTypeTooltip(UTILS, "ali.property.value.block_entity_type", BlockEntityType.BEACON), List.of("Block Entity Type: minecraft:beacon"));
    }

    @Test
    public void testPotionTooltip() {
        assertTooltip(RegistriesTooltipUtils.getPotionTooltip(UTILS, "ali.property.value.potion", Potions.HEALING), List.of("Potion: minecraft:healing"));
    }

    @Test
    public void testMobEffectTooltip() {
        assertTooltip(RegistriesTooltipUtils.getMobEffectTooltip(UTILS, "ali.property.value.mob_effect", MobEffects.BLINDNESS), List.of("Mob Effect: minecraft:blindness"));
    }

    @Test
    public void testLootNbtProviderTypeTooltip() {
        assertTooltip(RegistriesTooltipUtils.getLootNbtProviderTypeTooltip(UTILS, "ali.property.value.nbt_provider", NbtProviders.CONTEXT), List.of("Nbt Provider: minecraft:context"));
    }

    @Test
    public void testFluidTooltip() {
        assertTooltip(RegistriesTooltipUtils.getFluidTooltip(UTILS, "ali.property.value.fluid", Fluids.WATER), List.of("Fluid: minecraft:water"));
    }

    @Test
    public void testEnchantmentTooltip() {
        assertTooltip(RegistriesTooltipUtils.getEnchantmentTooltip(UTILS, "ali.property.value.enchantment", Enchantments.AQUA_AFFINITY), List.of("Enchantment: minecraft:aqua_affinity"));
    }

    @Test
    public void testAttributeTooltip() {
        assertTooltip(RegistriesTooltipUtils.getAttributeTooltip(UTILS, "ali.property.value.attribute", Attributes.JUMP_STRENGTH), List.of("Attribute: minecraft:horse.jump_strength"));
    }
}
