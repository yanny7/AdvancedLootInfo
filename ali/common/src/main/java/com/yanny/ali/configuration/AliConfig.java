package com.yanny.ali.configuration;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.yanny.aci.configuration.ConfigCodecs;
import com.yanny.aci.configuration.ICoreConfig;
import com.yanny.aci.configuration.SpawnInfoFilter;
import com.yanny.aci.configuration.TooltipColors;
import com.yanny.ali.Utils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.*;
import java.util.function.Supplier;
import java.util.regex.Pattern;

public class AliConfig implements ICoreConfig {
    public static final int CURRENT_VERSION = 2;

    private static final List<ResourceLocation> DEFAULT_BLOCK_LOOT_CONDITIONS = List.of(ResourceLocation.withDefaultNamespace("survives_explosion"));
    private static final List<ResourceLocation> DEFAULT_BLOCK_LOOT_FUNCTIONS = List.of(ResourceLocation.withDefaultNamespace("explosion_decay"));
    private static final List<ResourceLocation> DEFAULT_IGNORED_PREDICATE_CONDITIONS = List.of(
            ResourceLocation.withDefaultNamespace("random_chance"),
            ResourceLocation.withDefaultNamespace("random_chance_with_enchanted_bonus"),
            ResourceLocation.withDefaultNamespace("table_bonus"),
            ResourceLocation.withDefaultNamespace("survives_explosion")
    );

    private static final MapCodec<AliConfig> BASE_CODEC = RecordCodecBuilder.mapCodec((instance) ->
        instance.group(
                field(Codec.INT, "configVersion", () -> 0).forGetter((c) -> c.configVersion),
                field(categories(BlockLootCategory.CODEC, "blockCategories"), "blockCategories", AliConfig::defaultBlockCategories).forGetter(c -> c.blockCategories),
                field(categories(EntityLootCategory.CODEC, "entityCategories"), "entityCategories", AliConfig::defaultEntityCategories).forGetter(c -> c.entityCategories),
                field(categories(GameplayLootCategory.CODEC, "gameplayCategories"), "gameplayCategories", AliConfig::defaultGameplayCategories).forGetter(c -> c.gameplayCategories),
                field(categories(TradeLootCategory.CODEC, "tradeCategories"), "tradeCategories", AliConfig::defaultTradeCategories).forGetter(c -> c.tradeCategories),
                field(ids("disabledEntities"), "disabledEntities", Collections::emptyList).forGetter((c) -> c.disabledEntities),
                field(Codec.BOOL, "logMoreStatistics", () -> false).forGetter((c) -> c.logMoreStatistics),
                field(Codec.BOOL, "showInGameNames", () -> true).forGetter((c) -> c.showInGameNames),
                field(Codec.BOOL, "hideDefaultBlockLoot", () -> true).forGetter((c) -> c.hideDefaultBlockLoot),
                field(Codec.BOOL, "showUnboundedGlobalLootModifiers", () -> false).forGetter((c) -> c.showUnboundedGlobalLootModifiers),
                field(Codec.BOOL, "showEntitiesWithoutLoot", () -> false).forGetter((c) -> c.showEntitiesWithoutLoot),
                field(ids("defaultBlockLootConditions"), "defaultBlockLootConditions", () -> DEFAULT_BLOCK_LOOT_CONDITIONS).forGetter((c) -> c.defaultBlockLootConditions),
                field(ids("defaultBlockLootFunctions"), "defaultBlockLootFunctions", () -> DEFAULT_BLOCK_LOOT_FUNCTIONS).forGetter((c) -> c.defaultBlockLootFunctions),
                field(ids("ignoredPredicateConditions"), "ignoredPredicateConditions", () -> DEFAULT_IGNORED_PREDICATE_CONDITIONS).forGetter((c) -> c.ignoredPredicateConditions),
                field(Codec.unboundedMap(ResourceLocation.CODEC, ResourceLocation.CODEC.listOf()), "entityLootTables", Collections::emptyMap).forGetter((c) -> c.entityLootTables),
                field(TooltipColors.codec(Utils.MOD_ID), "tooltipColors", TooltipColors::new).forGetter((c) -> c.tooltipColors)
        ).apply(instance, (version, blocks, entities, gameplay, trades, disabled, log, show, hideDefaultLoot, showUnboundedGlm, showEntitiesWithoutLoot, defaultConditions, defaultFunctions, ignoredPredicates, entityLoot, colors) -> {
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
            config.showUnboundedGlobalLootModifiers = showUnboundedGlm;
            config.showEntitiesWithoutLoot = showEntitiesWithoutLoot;
            config.defaultBlockLootConditions = new ArrayList<>(defaultConditions);
            config.defaultBlockLootFunctions = new ArrayList<>(defaultFunctions);
            config.ignoredPredicateConditions = new ArrayList<>(ignoredPredicates);
            config.entityLootTables = new LinkedHashMap<>(entityLoot);
            config.tooltipColors = colors;
            return config;
        })
    );

    private static final MapCodec<Pair<SpawnInfoFilter, Boolean>> EXTRA_CODEC = RecordCodecBuilder.mapCodec((instance) ->
        instance.group(
                field(SpawnInfoFilter.codec(Utils.MOD_ID), "spawnInfo", SpawnInfoFilter::new).forGetter(Pair::getFirst),
                field(Codec.BOOL, "showCharts", () -> true).forGetter(Pair::getSecond)
        ).apply(instance, Pair::of)
    );

    public static final Codec<AliConfig> CODEC = Codec.mapPair(BASE_CODEC, EXTRA_CODEC).xmap((pair) -> {
        AliConfig config = pair.getFirst();

        config.spawnInfo = pair.getSecond().getFirst();
        config.showCharts = pair.getSecond().getSecond();
        return config;
    }, (config) -> Pair.of(config, Pair.of(config.spawnInfo, config.showCharts))).codec();

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
    public SpawnInfoFilter spawnInfo = new SpawnInfoFilter();

    public boolean logMoreStatistics = false;
    public boolean showInGameNames = true;
    public boolean hideDefaultBlockLoot = true;
    public boolean showUnboundedGlobalLootModifiers = false;
    public boolean showEntitiesWithoutLoot = false;
    public boolean showCharts = true;

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
    private static <A> MapCodec<A> field(Codec<A> codec, String name, Supplier<? extends A> fallback) {
        return ConfigCodecs.field(Utils.MOD_ID, codec, name, fallback);
    }

    @NotNull
    private static <T extends LootCategory<?>> Codec<List<T>> categories(MapCodec<T> codec, String name) {
        return ConfigCodecs.lenientList(Utils.MOD_ID, codec.codec(), name);
    }

    @NotNull
    private static Codec<List<ResourceLocation>> ids(String name) {
        return ConfigCodecs.lenientList(Utils.MOD_ID, ResourceLocation.CODEC, name);
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
                new GameplayLootCategory(Utils.modLoc("archaeology_loot"), Items.DECORATED_POT, false, of(Items.BRUSH, Items.SUSPICIOUS_SAND, Items.SUSPICIOUS_GRAVEL), List.of(Pattern.compile("^.*:archaeology/.*$"))),
                new GameplayLootCategory(Utils.modLoc("hero_loot"), Items.EMERALD, false, of(), List.of(Pattern.compile("^.*:gameplay/hero_of_the_village/.*$"))),
                new GameplayLootCategory(Utils.modLoc("cat_morning_gift"), Items.PHANTOM_MEMBRANE, false, of(Items.CAT_SPAWN_EGG), List.of(Pattern.compile("^.*:gameplay/cat_morning_gift.*$"))),
                new GameplayLootCategory(Utils.modLoc("piglin_bartering"), Items.GOLD_INGOT, false, of(Items.PIGLIN_SPAWN_EGG, Items.GOLD_INGOT), List.of(Pattern.compile("^.*:gameplay/piglin_bartering.*$"))),
                new GameplayLootCategory(Utils.modLoc("sniffer_digging"), Items.SNIFFER_EGG, false, of(Items.SNIFFER_SPAWN_EGG), List.of(Pattern.compile("^.*:gameplay/sniffer_digging.*$"))),
                new GameplayLootCategory(Utils.modLoc("panda_sneeze"), Items.BAMBOO, false, of(Items.PANDA_SPAWN_EGG), Collections.singletonList(Pattern.compile("^.*:gameplay/panda_sneeze.*$"))),
                new GameplayLootCategory(Utils.modLoc("shearing"), Items.SHEARS, false, of(Items.SHEARS), Collections.singletonList(Pattern.compile("^.*:shearing/.*$"))),
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
