package com.yanny.ali.compatibility;

import com.mojang.logging.LogUtils;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.compatibility.common.*;
import com.yanny.ali.compatibility.rei.*;
import com.yanny.ali.configuration.LootCategory;
import com.yanny.ali.manager.AliClientRegistry;
import com.yanny.ali.manager.AliCommonRegistry;
import com.yanny.ali.manager.PluginManager;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Block;
import org.apache.commons.lang3.function.TriFunction;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class ReiCompatibility implements REIClientPlugin {
    private static final Logger LOGGER = LogUtils.getLogger();

    private final Map<LootCategory<Block>, Holder<ReiBlockDisplay, BlockLootType, Block>> blockCategories = new HashMap<>();
    private final Map<LootCategory<EntityType<?>>, Holder<ReiEntityDisplay, EntityLootType, EntityType<?>>> entityCategories = new HashMap<>();
    private final Map<LootCategory<ResourceLocation>, Holder<ReiGameplayDisplay, GameplayLootType, ResourceLocation>> gameplayCategories = new HashMap<>();
    private final Map<LootCategory<ResourceLocation>, Holder<ReiTradeDisplay, TradeLootType, ResourceLocation>> tradeCategories = new HashMap<>();

    @Override
    public void registerCategories(CategoryRegistry registry) {
        AliCommonRegistry commonRegistry = PluginManager.COMMON_REGISTRY;

        blockCategories.clear();
        entityCategories.clear();
        gameplayCategories.clear();
        tradeCategories.clear();

        blockCategories.putAll(commonRegistry.getConfiguration().blockCategories.stream().collect(getCollector(ReiBlockDisplay::new, ReiBlockCategory::new)));
        entityCategories.putAll(commonRegistry.getConfiguration().entityCategories.stream().collect(getCollector(ReiEntityDisplay::new, ReiEntityCategory::new)));
        gameplayCategories.putAll(commonRegistry.getConfiguration().gameplayCategories.stream().collect(getCollector(ReiGameplayDisplay::new, ReiGameplayCategory::new)));
        tradeCategories.putAll(commonRegistry.getConfiguration().tradeCategories.stream().collect(getCollector(ReiTradeDisplay::new, ReiTradeCategory::new)));

        for (Holder<ReiBlockDisplay, BlockLootType, Block> holder : blockCategories.values()) {
            registry.add(holder.category);
        }

        for (Holder<ReiEntityDisplay, EntityLootType, EntityType<?>> holder : entityCategories.values()) {
            registry.add(holder.category);
        }

        for (Holder<ReiGameplayDisplay, GameplayLootType, ResourceLocation> holder : gameplayCategories.values()) {
            registry.add(holder.category);
        }

        for (Holder<ReiTradeDisplay, TradeLootType, ResourceLocation> holder : tradeCategories.values()) {
            registry.add(holder.category);
        }
    }

    @Override
    public void registerDisplays(DisplayRegistry registry) {
        CompletableFuture<Pair<Map<ResourceLocation, IDataNode>, Map<ResourceLocation, IDataNode>>> futureData = PluginManager.CLIENT_REGISTRY.getCurrentDataFuture();

        if (futureData.isDone()) {
            LOGGER.info("Data already received, processing instantly.");
        } else {
            LOGGER.info("Blocking this thread until all data are received!");
        }

        try {
            Pair<Map<ResourceLocation, IDataNode>, Map<ResourceLocation, IDataNode>> pair = futureData.get();

            registerData(registry, pair.getLeft(), pair.getRight());
        } catch (ExecutionException e) {
            Throwable cause = e.getCause() != null ? e.getCause() : e;

            if (cause instanceof TimeoutException) {
                futureData.cancel(true);
                PluginManager.CLIENT_REGISTRY.clearLootData();
                LOGGER.error("Failed to received data: Inactivity timeout occurred. Registration aborted!");
            } else {
                LOGGER.error("Failed to finish registering data with error {}", cause.getMessage());
                cause.printStackTrace();
            }
        } catch (Throwable e) {
            e.printStackTrace();
            LOGGER.error("Failed to finish registering data with unexpected error {}", e.getMessage());
        }
    }

    private void registerData(DisplayRegistry registry, Map<ResourceLocation, IDataNode> lootData, Map<ResourceLocation, IDataNode> tradeData) {
        AliClientRegistry clientRegistry = PluginManager.CLIENT_REGISTRY;
        ClientLevel level = Minecraft.getInstance().level;

        LOGGER.info("Adding loot information to REI");

        if (level != null) {
            Map<Holder<ReiBlockDisplay, BlockLootType, Block>, List<BlockLootType>> blockRecipeTypes = new HashMap<>();
            Map<Holder<ReiEntityDisplay, EntityLootType, EntityType<?>>, List<EntityLootType>> entityRecipeTypes = new HashMap<>();
            Map<Holder<ReiGameplayDisplay, GameplayLootType, ResourceLocation>, List<GameplayLootType>> gameplayRecipeTypes = new HashMap<>();
            Map<Holder<ReiTradeDisplay, TradeLootType, ResourceLocation>, List<TradeLootType>> tradeRecipeTypes = new HashMap<>();

            GenericUtils.processData(
                    level,
                    clientRegistry,
                    lootData,
                    tradeData,
                    (node, location, block, outputs) -> {
                        Holder<ReiBlockDisplay, BlockLootType, Block> recipeHolder = null;

                        for (Holder<ReiBlockDisplay, BlockLootType, Block> holder : blockCategories.values()) {
                            if (holder.category.getLootCategory().validate(block)) {
                                if (holder.category.getLootCategory().isHidden()) {
                                    return;
                                }

                                recipeHolder = holder;
                                break;
                            }
                        }

                        if (recipeHolder != null) {
                            blockRecipeTypes.computeIfAbsent(recipeHolder, (b) -> new LinkedList<>()).add(new BlockLootType(block, node, Collections.emptyList(), outputs));
                        }
                    },
                    (node, location, entity, outputs) -> {
                        Holder<ReiEntityDisplay, EntityLootType, EntityType<?>> recipeHolder = null;

                        for (Holder<ReiEntityDisplay, EntityLootType, EntityType<?>> holder : entityCategories.values()) {
                            if (holder.category.getLootCategory().validate(entity)) {
                                if (holder.category.getLootCategory().isHidden()) {
                                    return;
                                }

                                recipeHolder = holder;
                                break;
                            }
                        }

                        if (recipeHolder != null) {
                            entityRecipeTypes.computeIfAbsent(recipeHolder, (b) -> new LinkedList<>()).add(new EntityLootType(entity, location, node, Collections.emptyList(), outputs));
                        }
                    },
                    (node, location, outputs) -> {
                        Holder<ReiGameplayDisplay, GameplayLootType, ResourceLocation> recipeHolder = null;

                        for (Holder<ReiGameplayDisplay, GameplayLootType, ResourceLocation> holder : gameplayCategories.values()) {
                            if (holder.category.getLootCategory().validate(location)) {
                                if (holder.category.getLootCategory().isHidden()) {
                                    return;
                                }

                                recipeHolder = holder;
                                break;
                            }
                        }

                        if (recipeHolder != null) {
                            gameplayRecipeTypes.computeIfAbsent(recipeHolder, (b) -> new LinkedList<>()).add(new GameplayLootType(node, location.getPath(), Collections.emptyList(), outputs));
                        }
                    },
                    (tradeEntry, location, inputs, outputs) -> {
                        Holder<ReiTradeDisplay, TradeLootType, ResourceLocation> recipeHolder = null;

                        for (Holder<ReiTradeDisplay, TradeLootType, ResourceLocation> holder : tradeCategories.values()) {
                            if (holder.category.getLootCategory().validate(location)) {
                                if (holder.category.getLootCategory().isHidden()) {
                                    return;
                                }

                                recipeHolder = holder;
                                break;
                            }
                        }

                        if (recipeHolder != null) {
                            tradeRecipeTypes.computeIfAbsent(recipeHolder, (b) -> new LinkedList<>()).add(new TradeLootType(tradeEntry, location.getPath(), inputs, outputs));
                        }
                    },
                    (tradeEntry, location, inputs, outputs) -> {
                        Holder<ReiTradeDisplay, TradeLootType, ResourceLocation> recipeHolder = null;

                        for (Holder<ReiTradeDisplay, TradeLootType, ResourceLocation> holder : tradeCategories.values()) {
                            if (holder.category.getLootCategory().validate(location)) {
                                if (holder.category.getLootCategory().isHidden()) {
                                    return;
                                }

                                recipeHolder = holder;
                                break;
                            }
                        }

                        if (recipeHolder != null) {
                            tradeRecipeTypes.computeIfAbsent(recipeHolder, (b) -> new LinkedList<>()).add(new TradeLootType(tradeEntry, location.getPath(), inputs, outputs));
                        }
                    }
            );

            for (Map.Entry<Holder<ReiBlockDisplay, BlockLootType, Block>, List<BlockLootType>> entry : blockRecipeTypes.entrySet()) {
                registry.registerFiller(blockPredicate(entry.getValue()), entry.getKey().filler());
                entry.getValue().forEach(registry::add);
            }

            for (Map.Entry<Holder<ReiEntityDisplay, EntityLootType, EntityType<?>>, List<EntityLootType>> entry : entityRecipeTypes.entrySet()) {
                registry.registerFiller(entityPredicate(entry.getValue()), entry.getKey().filler());
                entry.getValue().forEach(registry::add);
            }

            for (Map.Entry<Holder<ReiGameplayDisplay, GameplayLootType, ResourceLocation>, List<GameplayLootType>> entry : gameplayRecipeTypes.entrySet()) {
                registry.registerFiller(gameplayPredicate(entry.getValue()), entry.getKey().filler());
                entry.getValue().forEach(registry::add);
            }

            for (Map.Entry<Holder<ReiTradeDisplay, TradeLootType, ResourceLocation>, List<TradeLootType>> entry : tradeRecipeTypes.entrySet()) {
                registry.registerFiller(tradePredicate(entry.getValue()), entry.getKey().filler());
                entry.getValue().forEach(registry::add);
            }
        } else {
            LOGGER.warn("REI integration was not loaded! Level is null!");
        }
    }

    @NotNull
    private static <D extends ReiBaseDisplay, T, U> Holder<D, T, U> createCategory(LootCategory<U> lootCategory,
                                                                            BiFunction<T, CategoryIdentifier<D>, D> displayFactory,
                                                                            TriFunction<CategoryIdentifier<D>, Component, LootCategory<U>, ReiBaseCategory<D, U>> categoryFactory) {
        CategoryIdentifier<D> identifier = CategoryIdentifier.of(lootCategory.getKey());
        Component title = Component.translatable("emi.category." + lootCategory.getKey().getNamespace() + "." + lootCategory.getKey().getPath().replace('/', '.'));
        Function<T, D> filler = (type) -> displayFactory.apply(type, identifier);
        return new Holder<>(identifier, categoryFactory.apply(identifier, title, lootCategory), filler);
    }

    @NotNull
    private static <D extends ReiBaseDisplay, T, U> Collector<LootCategory<U>, ?, Map<LootCategory<U>, Holder<D, T, U>>> getCollector(
            BiFunction<T, CategoryIdentifier<D>, D> displayFactory,
            TriFunction<CategoryIdentifier<D>, Component, LootCategory<U>, ReiBaseCategory<D, U>> categoryFactory) {
        return Collectors.toMap(
                (e) -> e,
                (e) -> createCategory(e, displayFactory, categoryFactory),
                (a, b) -> a,
                LinkedHashMap::new
        );
    }

    @NotNull
    private Predicate<Object> blockPredicate(List<BlockLootType> lootTypes) {
        return (o) -> {
            if (o != null) {
                if (o instanceof BlockLootType type) {
                    return lootTypes.contains(type);
                }
            }

            return false;
        };
    }

    @NotNull
    private Predicate<Object> entityPredicate(List<EntityLootType> lootTypes) {
        return (o) -> {
            if (o != null) {
                if (o instanceof EntityLootType type) {
                    return lootTypes.contains(type);
                }
            }

            return false;
        };
    }

    @NotNull
    private Predicate<Object> gameplayPredicate(List<GameplayLootType> lootTypes) {
        return (o) -> {
            if (o != null) {
                if (o instanceof GameplayLootType type) {
                    return lootTypes.contains(type);
                }
            }

            return false;
        };
    }

    @NotNull
    private Predicate<Object> tradePredicate(List<TradeLootType> lootTypes) {
        return (o) -> {
            if (o != null) {
                if (o instanceof TradeLootType type) {
                    return lootTypes.contains(type);
                }
            }

            return false;
        };
    }

    private record Holder<D extends ReiBaseDisplay, T, U>(CategoryIdentifier<D> identifier, ReiBaseCategory<D, U> category, Function<T, D> filler) {}
}
