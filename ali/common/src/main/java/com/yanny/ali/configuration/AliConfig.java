package com.yanny.ali.configuration;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.yanny.aci.configuration.ICoreConfig;
import com.yanny.aci.configuration.TooltipColors;
import com.yanny.ali.Utils;
import net.minecraft.resources.Identifier;
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

    private static final List<Identifier> DEFAULT_BLOCK_LOOT_CONDITIONS = List.of(Identifier.withDefaultNamespace("survives_explosion"));
    private static final List<Identifier> DEFAULT_BLOCK_LOOT_FUNCTIONS = List.of(Identifier.withDefaultNamespace("explosion_decay"));
    private static final List<Identifier> DEFAULT_IGNORED_PREDICATE_CONDITIONS = List.of(
            Identifier.withDefaultNamespace("random_chance"),
            Identifier.withDefaultNamespace("random_chance_with_enchanted_bonus"),
            Identifier.withDefaultNamespace("table_bonus"),
            Identifier.withDefaultNamespace("survives_explosion")
    );

    public static final Codec<AliConfig> CODEC = RecordCodecBuilder.create((instance) ->
        instance.group(
                Codec.INT.fieldOf("configVersion").orElse(0).forGetter((c) -> c.configVersion),
                BlockLootCategory.CODEC.codec().listOf().fieldOf("blockCategories").orElseGet(AliConfig::defaultBlockCategories).forGetter(c -> c.blockCategories),
                EntityLootCategory.CODEC.codec().listOf().fieldOf("entityCategories").orElseGet(AliConfig::defaultEntityCategories).forGetter(c -> c.entityCategories),
                GameplayLootCategory.CODEC.codec().listOf().fieldOf("gameplayCategories").orElseGet(AliConfig::defaultGameplayCategories).forGetter(c -> c.gameplayCategories),
                TradeLootCategory.CODEC.codec().listOf().fieldOf("tradeCategories").orElseGet(AliConfig::defaultTradeCategories).forGetter(c -> c.tradeCategories),
                Identifier.CODEC.listOf().fieldOf("disabledEntities").orElse(Collections.emptyList()).forGetter((c) -> c.disabledEntities),
                Codec.BOOL.fieldOf("logMoreStatistics").orElse(false).forGetter((c) -> c.logMoreStatistics),
                Codec.BOOL.fieldOf("showInGameNames").orElse(true).forGetter((c) -> c.showInGameNames),
                Codec.BOOL.fieldOf("hideDefaultBlockLoot").orElse(true).forGetter((c) -> c.hideDefaultBlockLoot),
                Identifier.CODEC.listOf().fieldOf("defaultBlockLootConditions").orElse(DEFAULT_BLOCK_LOOT_CONDITIONS).forGetter((c) -> c.defaultBlockLootConditions),
                Identifier.CODEC.listOf().fieldOf("defaultBlockLootFunctions").orElse(DEFAULT_BLOCK_LOOT_FUNCTIONS).forGetter((c) -> c.defaultBlockLootFunctions),
                Identifier.CODEC.listOf().fieldOf("ignoredPredicateConditions").orElse(DEFAULT_IGNORED_PREDICATE_CONDITIONS).forGetter((c) -> c.ignoredPredicateConditions),
                Codec.unboundedMap(Identifier.CODEC, Identifier.CODEC.listOf()).fieldOf("entityLootTables").orElse(Collections.emptyMap()).forGetter((c) -> c.entityLootTables),
                TooltipColors.CODEC.fieldOf("tooltipColors").orElseGet(TooltipColors::new).forGetter((c) -> c.tooltipColors)
        ).apply(instance, (version, blocks, entities, gameplay, trades, disabled, log, show, hideDefaultLoot, defaultConditions, defaultFunctions, ignoredPredicates, entityLoot, colors) -> {
            AliConfig config = new AliConfig();

            config.configVersion = version;
            config.disabledEntities = new ArrayList<>(disabled);
            config.logMoreStatistics = log;
            config.showInGameNames = show;
            config.blockCategories = new ArrayList<>(blocks);
            config.entityCategories = new ArrayList<>(entities);
            config.gameplayCategories = new ArrayList<>(gameplay);
            config.tradeCategories = new ArrayList<>(trades);
            config.hideDefaultBlockLoot = hideDefaultLoot;
            config.defaultBlockLootConditions = new ArrayList<>(defaultConditions);
            config.defaultBlockLootFunctions = new ArrayList<>(defaultFunctions);
            config.ignoredPredicateConditions = new ArrayList<>(ignoredPredicates);
            config.entityLootTables = new LinkedHashMap<>(entityLoot);
            config.tooltipColors = colors;
            return config;
        })
    );

    public int configVersion = 0;

    public List<BlockLootCategory> blockCategories;
    public List<EntityLootCategory> entityCategories;
    public List<GameplayLootCategory> gameplayCategories;
    public List<TradeLootCategory> tradeCategories;

    public List<Identifier> disabledEntities;
    public List<Identifier> defaultBlockLootConditions;
    public List<Identifier> defaultBlockLootFunctions;
    public List<Identifier> ignoredPredicateConditions;

    /**
     * Loot tables an entity type drops, keyed by entity type id. Only needed for entities whose loot table is neither
     * their type's default one nor a path below it - those are found by their id alone, see {@code
     * EntityLootTableResolver}.
     */
    public Map<Identifier, List<Identifier>> entityLootTables;

    public TooltipColors tooltipColors = new TooltipColors();

    public boolean logMoreStatistics = false;
    public boolean showInGameNames = true;
    public boolean hideDefaultBlockLoot = true;

    public AliConfig() {
        blockCategories = new ArrayList<>(defaultBlockCategories());
        entityCategories = new ArrayList<>(defaultEntityCategories());
        gameplayCategories = new ArrayList<>(defaultGameplayCategories());
        tradeCategories = new ArrayList<>(defaultTradeCategories());

        disabledEntities = new ArrayList<>();

        defaultBlockLootConditions = new ArrayList<>(DEFAULT_BLOCK_LOOT_CONDITIONS);
        defaultBlockLootFunctions = new ArrayList<>(DEFAULT_BLOCK_LOOT_FUNCTIONS);
        ignoredPredicateConditions = new ArrayList<>(DEFAULT_IGNORED_PREDICATE_CONDITIONS);

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

    @NotNull
    @Unmodifiable
    private static List<BlockLootCategory> defaultBlockCategories() {
        return List.of(
                new BlockLootCategory(Utils.modLoc("plant_loot"), Items.DIAMOND_HOE, false, of(), List.of(Either.left(BlockTags.CROPS))),
                new BlockLootCategory(Utils.modLoc("block_loot"), Items.DIAMOND_PICKAXE, false, of(), Collections.emptyList())
        );
    }

    @NotNull
    @Unmodifiable
    private static List<EntityLootCategory> defaultEntityCategories() {
        return List.of(new EntityLootCategory(Utils.modLoc("entity_loot"), Items.SKELETON_SKULL, false, of(), Collections.emptyList()));
    }

    @NotNull
    @Unmodifiable
    private static List<GameplayLootCategory> defaultGameplayCategories() {
        return List.of(
                new GameplayLootCategory(Utils.modLoc("chest_loot"), Items.CHEST, false, of(), List.of(
                        Pattern.compile("^.*:chests/[a-z_]*$"),
                        Pattern.compile("^.*:chests/village/[a-z_]*$")
                )),
                new GameplayLootCategory(Utils.modLoc("trial_chambers"), Items.TRIAL_SPAWNER, false, of(Items.TRIAL_SPAWNER, Items.TRIAL_KEY, Items.OMINOUS_TRIAL_KEY), List.of(
                        Pattern.compile("^.*:chests/trial_chambers/.*$"),
                        Pattern.compile("^.*:pots/trial_chambers/.*$"),
                        Pattern.compile("^.*:dispensers/trial_chambers/.*$"),
                        Pattern.compile("^.*:spawners/ominous/trial_chamber/.*$"),
                        Pattern.compile("^.*:spawners/trial_chamber/.*$")
                )),
                new GameplayLootCategory(Utils.modLoc("fishing_loot"), Items.FISHING_ROD, false, of(Items.FISHING_ROD), List.of(Pattern.compile("^.*:gameplay/fishing.*$"))),
                new GameplayLootCategory(Utils.modLoc("archaeology_loot"), Items.DECORATED_POT, false, of(Items.BRUSH, Items.SUSPICIOUS_SAND, Items.SUSPICIOUS_GRAVEL), List.of(
                        Pattern.compile("^.*:archaeology/.*$"),
                        Pattern.compile("^.*:brush/.*$")
                )),
                new GameplayLootCategory(Utils.modLoc("hero_loot"), Items.EMERALD, false, of(), List.of(Pattern.compile("^.*:gameplay/hero_of_the_village/.*$"))),
                new GameplayLootCategory(Utils.modLoc("cat_morning_gift"), Items.PHANTOM_MEMBRANE, false, of(Items.CAT_SPAWN_EGG), List.of(Pattern.compile("^.*:gameplay/cat_morning_gift.*$"))),
                new GameplayLootCategory(Utils.modLoc("piglin_bartering"), Items.GOLD_INGOT, false, of(Items.PIGLIN_SPAWN_EGG, Items.GOLD_INGOT), List.of(Pattern.compile("^.*:gameplay/piglin_bartering.*$"))),
                new GameplayLootCategory(Utils.modLoc("sniffer_digging"), Items.SNIFFER_EGG, false, of(Items.SNIFFER_SPAWN_EGG), List.of(Pattern.compile("^.*:gameplay/sniffer_digging.*$"))),
                new GameplayLootCategory(Utils.modLoc("panda_sneeze"), Items.BAMBOO, false, of(Items.PANDA_SPAWN_EGG), Collections.singletonList(Pattern.compile("^.*:gameplay/panda_sneeze.*$"))),
                new GameplayLootCategory(Utils.modLoc("shearing"), Items.SHEARS, false, of(Items.SHEARS), Collections.singletonList(Pattern.compile("^.*:shearing/.*$"))),
                new GameplayLootCategory(Utils.modLoc("armadillo_shed"), Items.ARMADILLO_SCUTE, false, of(Items.ARMADILLO_SPAWN_EGG), Collections.singletonList(Pattern.compile("^.*:gameplay/armadillo_shed.*$"))),
                new GameplayLootCategory(Utils.modLoc("chicken_lay"), Items.EGG, false, of(Items.CHICKEN_SPAWN_EGG), Collections.singletonList(Pattern.compile("^.*:gameplay/chicken_lay.*$"))),
                new GameplayLootCategory(Utils.modLoc("carving"), Items.CARVED_PUMPKIN, false, of(Items.SHEARS), Collections.singletonList(Pattern.compile("^.*:carve/.*$"))),
                new GameplayLootCategory(Utils.modLoc("charged_creeper"), Items.CREEPER_HEAD, false, of(Items.CREEPER_SPAWN_EGG), Collections.singletonList(Pattern.compile("^.*:charged_creeper/.*$"))),
                new GameplayLootCategory(Utils.modLoc("harvesting"), Items.FLOWER_POT, false, of(), Collections.singletonList(Pattern.compile("^.*:harvest/.*$"))),
                new GameplayLootCategory(Utils.modLoc("turtle_grow"), Items.TURTLE_SCUTE, false, of(Items.TURTLE_SPAWN_EGG), Collections.singletonList(Pattern.compile("^.*:gameplay/turtle_grow.*$"))),
                new GameplayLootCategory(Utils.modLoc("gameplay_loot"), Items.COMPASS, false, of(), Collections.singletonList(Pattern.compile(".*")))
        );
    }

    @NotNull
    @Unmodifiable
    private static List<TradeLootCategory> defaultTradeCategories() {
        return List.of(new TradeLootCategory(Utils.modLoc("trade_loot"), Items.EMERALD_BLOCK, false, of(), Collections.singletonList(Pattern.compile(".*"))));
    }

    @NotNull
    @Unmodifiable
    private static List<Ingredient> of(Item... items) {
        return Arrays.stream(items).map(Ingredient::of).toList();
    }
}
