package com.yanny.ali.configuration;

import com.yanny.ali.Utils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Items;
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

    public AliConfig() {
        blockCategories = new ArrayList<>();
        blockCategories.add(new BlockLootCategory(Utils.modLoc("plant_loot"), Items.DIAMOND_HOE, false, List.of(VegetationBlock.class)));
        blockCategories.add(new BlockLootCategory(Utils.modLoc("block_loot"), Items.DIAMOND_PICKAXE, false, Collections.singletonList(Block.class)));

        entityCategories = new ArrayList<>();
        entityCategories.add(new EntityLootCategory(Utils.modLoc("entity_loot"), Items.SKELETON_SKULL, false, Collections.singletonList(Entity.class)));

        gameplayCategories = new ArrayList<>();
        gameplayCategories.add(new GameplayLootCategory(Utils.modLoc("chest_loot"), Items.CHEST, false, List.of(
                Pattern.compile("^.*:chests/[a-z_]*$"),
                Pattern.compile("^.*:chests/village/[a-z_]*$")
        )));
        gameplayCategories.add(new GameplayLootCategory(Utils.modLoc("trial_chambers"), Items.SPAWNER, false, List.of(
                Pattern.compile("^.*:chests/trial_chambers/.*$"),
                Pattern.compile("^.*:pots/trial_chambers/.*$"),
                Pattern.compile("^.*:dispensers/trial_chambers/.*$"),
                Pattern.compile("^.*:spawners/ominous/trial_chamber/.*$"),
                Pattern.compile("^.*:spawners/trial_chamber/.*$"),
                Pattern.compile("^.*:equipment/.*$")
        )));
        gameplayCategories.add(new GameplayLootCategory(Utils.modLoc("fishing_loot"), Items.FISHING_ROD, false, List.of(Pattern.compile("^.*:gameplay/fishing.*$"))));
        gameplayCategories.add(new GameplayLootCategory(Utils.modLoc("archaeology_loot"), Items.DECORATED_POT, false, List.of(Pattern.compile("^.*:archaeology/.*$"))));
        gameplayCategories.add(new GameplayLootCategory(Utils.modLoc("hero_loot"), Items.EMERALD, false, List.of(Pattern.compile("^.*:gameplay/hero_of_the_village/.*$"))));
        gameplayCategories.add(new GameplayLootCategory(Utils.modLoc("shearing_loot"), Items.SHEARS, false, Collections.singletonList(Pattern.compile("^.*:shearing/.*$"))));
        gameplayCategories.add(new GameplayLootCategory(Utils.modLoc("gameplay_loot"), Items.COMPASS, false, Collections.singletonList(Pattern.compile(".*"))));

        tradeCategories = new ArrayList<>();
        tradeCategories.add(new TradeLootCategory(Utils.modLoc("trade_loot"), Items.EMERALD_BLOCK, false, Collections.singletonList(Pattern.compile(".*"))));

        disabledEntities = new ArrayList<>();
    }
}
