package com.yanny.ali.configuration;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.yanny.ali.Utils;
import net.minecraft.resources.Identifier;
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
    public static final Codec<AliConfig> CODEC = RecordCodecBuilder.create((instance) ->
        instance.group(
                BlockLootCategory.CODEC.codec().listOf().optionalFieldOf("blockCategories", Collections.emptyList()).forGetter(c -> c.blockCategories),
                EntityLootCategory.CODEC.codec().listOf().optionalFieldOf("entityCategories", Collections.emptyList()).forGetter(c -> c.entityCategories),
                GameplayLootCategory.CODEC.codec().listOf().optionalFieldOf("gameplayCategories", Collections.emptyList()).forGetter(c -> c.gameplayCategories),
                TradeLootCategory.CODEC.codec().listOf().optionalFieldOf("tradeCategories", Collections.emptyList()).forGetter(c -> c.tradeCategories),
                Identifier.CODEC.listOf().optionalFieldOf("disabledEntities", Collections.emptyList()).forGetter((c) -> c.disabledEntities),
                Codec.BOOL.optionalFieldOf("logMoreStatistics", false).forGetter((c) -> c.logMoreStatistics),
                Codec.BOOL.optionalFieldOf("showInGameNames", true).forGetter((c) -> c.showInGameNames)
        ).apply(instance, (blocks, entities, gameplay, trades, disabled, log, show) -> {
            AliConfig config = new AliConfig();

            config.disabledEntities = new ArrayList<>(disabled);
            config.logMoreStatistics = log;
            config.showInGameNames = show;
            config.blockCategories = blocks;
            config.entityCategories = entities;
            config.gameplayCategories = gameplay;
            config.tradeCategories = trades;
            return config;
        })
    );

    public List<BlockLootCategory> blockCategories;
    public List<EntityLootCategory> entityCategories;
    public List<GameplayLootCategory> gameplayCategories;
    public List<TradeLootCategory> tradeCategories;

    public List<Identifier> disabledEntities;

    public boolean logMoreStatistics = false;
    public boolean showInGameNames = true;

    public AliConfig() {
        blockCategories = new ArrayList<>();
        blockCategories.add(new BlockLootCategory(Utils.modLoc("plant_loot"), Items.DIAMOND_HOE, false, null, List.of(VegetationBlock.class)));
        blockCategories.add(new BlockLootCategory(Utils.modLoc("block_loot"), Items.DIAMOND_PICKAXE, false, null, Collections.singletonList(Block.class)));

        entityCategories = new ArrayList<>();
        entityCategories.add(new EntityLootCategory(Utils.modLoc("entity_loot"), Items.SKELETON_SKULL, false, null, Collections.singletonList(Entity.class)));

        gameplayCategories = new ArrayList<>();
        gameplayCategories.add(new GameplayLootCategory(Utils.modLoc("chest_loot"), Items.CHEST, false, null, List.of(
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
        gameplayCategories.add(new GameplayLootCategory(Utils.modLoc("archaeology_loot"), Items.DECORATED_POT, false, Ingredient.of(Items.BRUSH, Items.SUSPICIOUS_SAND, Items.SUSPICIOUS_GRAVEL), List.of(
                Pattern.compile("^.*:archaeology/.*$"),
                Pattern.compile("^.*:brush/.*$")
        )));
        gameplayCategories.add(new GameplayLootCategory(Utils.modLoc("hero_loot"), Items.EMERALD, false, null, List.of(Pattern.compile("^.*:gameplay/hero_of_the_village/.*$"))));
        gameplayCategories.add(new GameplayLootCategory(Utils.modLoc("cat_morning_gift"), Items.PHANTOM_MEMBRANE, false, Ingredient.of(Items.CAT_SPAWN_EGG), List.of(Pattern.compile("^.*:gameplay/cat_morning_gift.*$"))));
        gameplayCategories.add(new GameplayLootCategory(Utils.modLoc("piglin_bartering"), Items.GOLD_INGOT, false, Ingredient.of(Items.PIGLIN_SPAWN_EGG, Items.GOLD_INGOT), List.of(Pattern.compile("^.*:gameplay/piglin_bartering.*$"))));
        gameplayCategories.add(new GameplayLootCategory(Utils.modLoc("sniffer_digging"), Items.SNIFFER_EGG, false, Ingredient.of(Items.SNIFFER_SPAWN_EGG), List.of(Pattern.compile("^.*:gameplay/sniffer_digging.*$"))));
        gameplayCategories.add(new GameplayLootCategory(Utils.modLoc("panda_sneeze"), Items.BAMBOO, false, Ingredient.of(Items.PANDA_SPAWN_EGG), Collections.singletonList(Pattern.compile("^.*:gameplay/panda_sneeze.*$"))));
        gameplayCategories.add(new GameplayLootCategory(Utils.modLoc("shearing"), Items.SHEARS, false, Ingredient.of(Items.SHEARS), Collections.singletonList(Pattern.compile("^.*:shearing/.*$"))));
        gameplayCategories.add(new GameplayLootCategory(Utils.modLoc("armadillo_shed"), Items.ARMADILLO_SCUTE, false, Ingredient.of(Items.ARMADILLO_SPAWN_EGG), Collections.singletonList(Pattern.compile("^.*:gameplay/armadillo_shed.*$"))));
        gameplayCategories.add(new GameplayLootCategory(Utils.modLoc("chicken_lay"), Items.EGG, false, Ingredient.of(Items.CHICKEN_SPAWN_EGG), Collections.singletonList(Pattern.compile("^.*:gameplay/chicken_lay.*$"))));
        gameplayCategories.add(new GameplayLootCategory(Utils.modLoc("carving"), Items.CARVED_PUMPKIN, false, Ingredient.of(Items.SHEARS), Collections.singletonList(Pattern.compile("^.*:carve/.*$"))));
        gameplayCategories.add(new GameplayLootCategory(Utils.modLoc("charged_creeper"), Items.CREEPER_HEAD, false, Ingredient.of(Items.CREEPER_SPAWN_EGG), Collections.singletonList(Pattern.compile("^.*:charged_creeper/.*$"))));
        gameplayCategories.add(new GameplayLootCategory(Utils.modLoc("harvesting"), Items.FLOWER_POT, false, null, Collections.singletonList(Pattern.compile("^.*:harvest/.*$"))));
        gameplayCategories.add(new GameplayLootCategory(Utils.modLoc("turtle_grow"), Items.TURTLE_SCUTE, false, Ingredient.of(Items.TURTLE_SPAWN_EGG), Collections.singletonList(Pattern.compile("^.*:gameplay/turtle_grow.*$"))));
        gameplayCategories.add(new GameplayLootCategory(Utils.modLoc("gameplay_loot"), Items.COMPASS, false, null, Collections.singletonList(Pattern.compile(".*"))));

        tradeCategories = new ArrayList<>();
        tradeCategories.add(new TradeLootCategory(Utils.modLoc("trade_loot"), Items.EMERALD_BLOCK, false, null, Collections.singletonList(Pattern.compile(".*"))));

        disabledEntities = new ArrayList<>();
    }
}
