package com.yanny.ali.configuration;

import com.yanny.ali.Utils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.VegetationBlock;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

public class AliConfig {
    public List<BlockLootCategory> blockCategories;
    public List<EntityLootCategory> entityCategories;
    public List<GameplayLootCategory> gameplayCategories;
    public List<TradeLootCategory> tradeCategories;

    public List<ResourceLocation> disabledEntities;

    public boolean logMoreStatistics = false;
    public boolean showInGameNames = true;

    public AliConfig() {
        blockCategories = new ArrayList<>();
        blockCategories.add(new BlockLootCategory(Utils.modLoc("plant_loot"), Items.DIAMOND_HOE, false, Ingredient.EMPTY, List.of(VegetationBlock.class)));
        blockCategories.add(new BlockLootCategory(Utils.modLoc("block_loot"), Items.DIAMOND_PICKAXE, false, Ingredient.EMPTY, Collections.singletonList(Block.class)));

        entityCategories = new ArrayList<>();
        entityCategories.add(new EntityLootCategory(Utils.modLoc("entity_loot"), Items.SKELETON_SKULL, false, Ingredient.EMPTY, Collections.singletonList(Entity.class)));

        gameplayCategories = new ArrayList<>();
        gameplayCategories.add(new GameplayLootCategory(Utils.modLoc("chest_loot"), Items.CHEST, false, Ingredient.EMPTY, List.of(
                Pattern.compile("^.*:chests/[a-z_]*$"),
                Pattern.compile("^.*:chests/village/[a-z_]*$")
        )));
        gameplayCategories.add(new GameplayLootCategory(Utils.modLoc("trial_chambers"), Items.TRIAL_SPAWNER, false, Ingredient.of(Items.TRIAL_SPAWNER, Items.TRIAL_KEY, Items.OMINOUS_TRIAL_KEY), List.of(
                Pattern.compile("^.*:chests/trial_chambers/.*$"),
                Pattern.compile("^.*:pots/trial_chambers/.*$"),
                Pattern.compile("^.*:dispensers/trial_chambers/.*$"),
                Pattern.compile("^.*:spawners/ominous/trial_chamber/.*$"),
                Pattern.compile("^.*:spawners/trial_chamber/.*$"),
                Pattern.compile("^.*:equipment/.*$")
        )));
        gameplayCategories.add(new GameplayLootCategory(Utils.modLoc("fishing_loot"), Items.FISHING_ROD, false, Ingredient.of(Items.FISHING_ROD), List.of(Pattern.compile("^.*:gameplay/fishing.*$"))));
        gameplayCategories.add(new GameplayLootCategory(Utils.modLoc("archaeology_loot"), Items.DECORATED_POT, false, Ingredient.of(Items.BRUSH, Items.SUSPICIOUS_SAND, Items.SUSPICIOUS_GRAVEL), List.of(Pattern.compile("^.*:archaeology/.*$"))));
        gameplayCategories.add(new GameplayLootCategory(Utils.modLoc("hero_loot"), Items.EMERALD, false, Ingredient.EMPTY, List.of(Pattern.compile("^.*:gameplay/hero_of_the_village/.*$"))));
        gameplayCategories.add(new GameplayLootCategory(Utils.modLoc("cat_morning_gift"), Items.PHANTOM_MEMBRANE, false, Ingredient.of(Items.CAT_SPAWN_EGG), List.of(Pattern.compile("^.*:gameplay/cat_morning_gift.*$"))));
        gameplayCategories.add(new GameplayLootCategory(Utils.modLoc("piglin_bartering"), Items.GOLD_INGOT, false, Ingredient.of(Items.PIGLIN_SPAWN_EGG, Items.GOLD_INGOT), List.of(Pattern.compile("^.*:gameplay/piglin_bartering.*$"))));
        gameplayCategories.add(new GameplayLootCategory(Utils.modLoc("sniffer_digging"), Items.SNIFFER_EGG, false, Ingredient.of(Items.SNIFFER_SPAWN_EGG), List.of(Pattern.compile("^.*:gameplay/sniffer_digging.*$"))));
        gameplayCategories.add(new GameplayLootCategory(Utils.modLoc("panda_sneeze"), Items.BAMBOO, false, Ingredient.of(Items.PANDA_SPAWN_EGG), Collections.singletonList(Pattern.compile("^.*:gameplay/panda_sneeze.*$"))));
        gameplayCategories.add(new GameplayLootCategory(Utils.modLoc("shearing"), Items.SHEARS, false, Ingredient.of(Items.SHEARS), Collections.singletonList(Pattern.compile("^.*:shearing/.*$"))));
        gameplayCategories.add(new GameplayLootCategory(Utils.modLoc("gameplay_loot"), Items.COMPASS, false, Ingredient.EMPTY, Collections.singletonList(Pattern.compile(".*"))));

        tradeCategories = new ArrayList<>();
        tradeCategories.add(new TradeLootCategory(Utils.modLoc("trade_loot"), Items.EMERALD_BLOCK, false, Ingredient.EMPTY, Collections.singletonList(Pattern.compile(".*"))));

        disabledEntities = new ArrayList<>();
    }
}
