package com.yanny.ali.configuration;

import com.mojang.datafixers.util.Either;
import com.yanny.aci.configuration.ICoreConfig;
import com.yanny.aci.configuration.TooltipColors;
import com.yanny.ali.Utils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class AliConfig implements ICoreConfig {
    public static final int CURRENT_VERSION = 2;

    public int configVersion = 0;

    public List<BlockLootCategory> blockCategories;
    public List<EntityLootCategory> entityCategories;
    public List<GameplayLootCategory> gameplayCategories;
    public List<TradeLootCategory> tradeCategories;

    public List<ResourceLocation> disabledEntities;
    public List<ResourceLocation> defaultBlockLootConditions;
    public List<ResourceLocation> defaultBlockLootFunctions;
    public List<ResourceLocation> ignoredPredicateConditions;

    /**
     * Loot tables an entity type drops, keyed by entity type id. Only needed for entities whose loot table is neither
     * their type's default one nor a path below it - those are found by their id alone, see {@code
     * EntityLootTableResolver}.
     */
    public Map<ResourceLocation, List<ResourceLocation>> entityLootTables;

    public TooltipColors tooltipColors = new TooltipColors();

    public boolean logMoreStatistics = false;
    public boolean showInGameNames = true;
    public boolean hideDefaultBlockLoot = true;

    public AliConfig() {
        blockCategories = new ArrayList<>();
        blockCategories.add(new BlockLootCategory(Utils.modLoc("plant_loot"), Items.DIAMOND_HOE, false, of(), List.of(Either.left(BlockTags.CROPS))));
        blockCategories.add(new BlockLootCategory(Utils.modLoc("block_loot"), Items.DIAMOND_PICKAXE, false, of(), Collections.emptyList()));

        entityCategories = new ArrayList<>();
        entityCategories.add(new EntityLootCategory(Utils.modLoc("entity_loot"), Items.SKELETON_SKULL, false, of(), Collections.emptyList()));

        gameplayCategories = new ArrayList<>();
        gameplayCategories.add(new GameplayLootCategory(Utils.modLoc("chest_loot"), Items.CHEST, false, of(), List.of(Pattern.compile("^.*:chests/.*$"))));
        gameplayCategories.add(new GameplayLootCategory(Utils.modLoc("fishing_loot"), Items.FISHING_ROD, false, of(Items.FISHING_ROD), List.of(Pattern.compile("^.*:gameplay/fishing.*$"))));
        gameplayCategories.add(new GameplayLootCategory(Utils.modLoc("archaeology_loot"), Items.DECORATED_POT, false, of(Items.BRUSH, Items.SUSPICIOUS_SAND, Items.SUSPICIOUS_GRAVEL), List.of(Pattern.compile("^.*:archaeology/.*$"))));
        gameplayCategories.add(new GameplayLootCategory(Utils.modLoc("hero_loot"), Items.EMERALD, false, of(), List.of(Pattern.compile("^.*:gameplay/hero_of_the_village/.*$"))));
        gameplayCategories.add(new GameplayLootCategory(Utils.modLoc("cat_morning_gift"), Items.PHANTOM_MEMBRANE, false, of(Items.CAT_SPAWN_EGG), List.of(Pattern.compile("^.*:gameplay/cat_morning_gift.*$"))));
        gameplayCategories.add(new GameplayLootCategory(Utils.modLoc("piglin_bartering"), Items.GOLD_INGOT, false, of(Items.PIGLIN_SPAWN_EGG, Items.GOLD_INGOT), List.of(Pattern.compile("^.*:gameplay/piglin_bartering.*$"))));
        gameplayCategories.add(new GameplayLootCategory(Utils.modLoc("sniffer_digging"), Items.SNIFFER_EGG, false, of(Items.SNIFFER_SPAWN_EGG), List.of(Pattern.compile("^.*:gameplay/sniffer_digging.*$"))));
        gameplayCategories.add(new GameplayLootCategory(Utils.modLoc("gameplay_loot"), Items.COMPASS, false, of(), Collections.singletonList(Pattern.compile(".*"))));

        tradeCategories = new ArrayList<>();
        tradeCategories.add(new TradeLootCategory(Utils.modLoc("trade_loot"), Items.EMERALD_BLOCK, false, of(), Collections.singletonList(Pattern.compile(".*"))));

        disabledEntities = new ArrayList<>();

        defaultBlockLootConditions = new ArrayList<>(List.of(new ResourceLocation("survives_explosion")));
        defaultBlockLootFunctions = new ArrayList<>(List.of(new ResourceLocation("explosion_decay")));
        ignoredPredicateConditions = new ArrayList<>(List.of(
                new ResourceLocation("random_chance"),
                new ResourceLocation("random_chance_with_looting"),
                new ResourceLocation("table_bonus"),
                new ResourceLocation("survives_explosion")
        ));

        entityLootTables = new LinkedHashMap<>();
    }

    @Override
    public int getConfigVersion() {
        return configVersion;
    }

    @Override
    public void setConfigVersion(int configVersion) {
        this.configVersion = configVersion;
    }

    @Override
    public int getCurrentVersion() {
        return CURRENT_VERSION;
    }

    @Override
    public void normalize() {
        AliConfig defaults = new AliConfig();

        if (blockCategories == null) {
            blockCategories = defaults.blockCategories;
        }
        if (entityCategories == null) {
            entityCategories = defaults.entityCategories;
        }
        if (gameplayCategories == null) {
            gameplayCategories = defaults.gameplayCategories;
        }
        if (tradeCategories == null) {
            tradeCategories = defaults.tradeCategories;
        }
        if (disabledEntities == null) {
            disabledEntities = defaults.disabledEntities;
        }
        if (defaultBlockLootConditions == null) {
            defaultBlockLootConditions = defaults.defaultBlockLootConditions;
        }
        if (defaultBlockLootFunctions == null) {
            defaultBlockLootFunctions = defaults.defaultBlockLootFunctions;
        }
        if (ignoredPredicateConditions == null) {
            ignoredPredicateConditions = defaults.ignoredPredicateConditions;
        }
        if (entityLootTables == null) {
            entityLootTables = defaults.entityLootTables;
        }
        if (tooltipColors == null) {
            tooltipColors = defaults.tooltipColors;
        }
    }

    @NotNull
    @Unmodifiable
    private static List<Ingredient> of(Item... items) {
        return Arrays.stream(items).map(Ingredient::of).toList();
    }
}
