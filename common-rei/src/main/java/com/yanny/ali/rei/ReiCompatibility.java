package com.yanny.ali.rei;

import com.mojang.logging.LogUtils;
import com.yanny.ali.compatibility.common.*;
import com.yanny.ali.configuration.AliConfig;
import com.yanny.ali.configuration.LootCategory;
import com.yanny.ali.manager.AliClientRegistry;
import com.yanny.ali.manager.PluginManager;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.client.registry.display.reason.DisplayAdditionReasons;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Block;
import org.apache.commons.lang3.function.TriFunction;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.*;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class ReiCompatibility implements REIClientPlugin {
    private static final Logger LOGGER = LogUtils.getLogger();

    private final Map<LootCategory<Block>, Holder<ReiBlockDisplay, BlockLootType, Block>> blockCategories = new LinkedHashMap<>();
    private final Map<LootCategory<EntityType<?>>, Holder<ReiEntityDisplay, EntityLootType, EntityType<?>>> entityCategories = new LinkedHashMap<>();
    private final Map<LootCategory<ResourceLocation>, Holder<ReiGameplayDisplay, GameplayLootType, ResourceLocation>> gameplayCategories = new LinkedHashMap<>();
    private final Map<LootCategory<ResourceLocation>, Holder<ReiTradeDisplay, TradeLootType, ResourceLocation>> tradeCategories = new LinkedHashMap<>();

    @Override
    public void registerCategories(CategoryRegistry registry) {
        AliConfig config = PluginManager.COMMON_REGISTRY.getConfiguration();

        blockCategories.clear();
        entityCategories.clear();
        gameplayCategories.clear();
        tradeCategories.clear();

        blockCategories.putAll(config.blockCategories.stream().collect(getCollector(ReiBlockDisplay::new, ReiBlockCategory::new)));
        entityCategories.putAll(config.entityCategories.stream().collect(getCollector(ReiEntityDisplay::new, ReiEntityCategory::new)));
        gameplayCategories.putAll(config.gameplayCategories.stream().collect(getCollector(ReiGameplayDisplay::new, ReiGameplayCategory::new)));
        tradeCategories.putAll(config.tradeCategories.stream().collect(getCollector(ReiTradeDisplay::new, ReiTradeCategory::new)));

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
        CompletableFuture<byte[]> futureData = PluginManager.CLIENT_REGISTRY.getCurrentDataFuture();

        if (futureData.isDone()) {
            LOGGER.info("Data already received, processing instantly.");
        } else {
            LOGGER.info("Blocking this thread until all data are received!");
        }

        try {
            byte[] fullCompressedData = futureData.get();

            registerData(registry, fullCompressedData);
            LOGGER.info("Data registration finished successfully.");
        } catch (ExecutionException e) {
            Throwable cause = e.getCause() != null ? e.getCause() : e;

            if (cause instanceof TimeoutException || cause instanceof CancellationException) {
                LOGGER.error("Failed to receive data: Operation aborted or timed out. Registration aborted!");
            } else {
                LOGGER.error("Failed to finish registering data with error {}", cause.getMessage());
                cause.printStackTrace();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOGGER.error("Registration thread interrupted!");
        } catch (Throwable e) {
            e.printStackTrace();
            LOGGER.error("Failed to finish registering data with unexpected error {}", e.getMessage());
        }
    }

    private void registerData(DisplayRegistry registry, byte[] fullCompressedData) {
        AliClientRegistry clientRegistry = PluginManager.CLIENT_REGISTRY;
        AliConfig config = PluginManager.COMMON_REGISTRY.getConfiguration();
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
                    config,
                    fullCompressedData,
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
                            gameplayRecipeTypes.computeIfAbsent(recipeHolder, (b) -> new LinkedList<>()).add(new GameplayLootType(node, location, Collections.emptyList(), outputs));
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
                registry.registerFillerWithReason(blockPredicate(entry.getValue()), entry.getKey().filler());
                entry.getValue().forEach(registry::add);
            }

            for (Map.Entry<Holder<ReiEntityDisplay, EntityLootType, EntityType<?>>, List<EntityLootType>> entry : entityRecipeTypes.entrySet()) {
                registry.registerFillerWithReason(entityPredicate(entry.getValue()), entry.getKey().filler());
                entry.getValue().forEach(registry::add);
            }

            for (Map.Entry<Holder<ReiGameplayDisplay, GameplayLootType, ResourceLocation>, List<GameplayLootType>> entry : gameplayRecipeTypes.entrySet()) {
                registry.registerFillerWithReason(gameplayPredicate(entry.getValue()), entry.getKey().filler());
                entry.getValue().forEach(registry::add);
            }

            for (Map.Entry<Holder<ReiTradeDisplay, TradeLootType, ResourceLocation>, List<TradeLootType>> entry : tradeRecipeTypes.entrySet()) {
                registry.registerFillerWithReason(tradePredicate(entry.getValue()), entry.getKey().filler());
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
        BiFunction<T, DisplayAdditionReasons, D> filler = (type, r) -> displayFactory.apply(type, identifier);
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
    private BiPredicate<Object, DisplayAdditionReasons> blockPredicate(List<BlockLootType> lootTypes) {
        return (o, r) -> {
            if (o != null) {
                if (o instanceof BlockLootType type) {
                    return lootTypes.contains(type);
                }
            }

            return false;
        };
    }

    @NotNull
    private BiPredicate<Object, DisplayAdditionReasons> entityPredicate(List<EntityLootType> lootTypes) {
        return (o, r) -> {
            if (o != null) {
                if (o instanceof EntityLootType type) {
                    return lootTypes.contains(type);
                }
            }

            return false;
        };
    }

    @NotNull
    private BiPredicate<Object, DisplayAdditionReasons> gameplayPredicate(List<GameplayLootType> lootTypes) {
        return (o, r) -> {
            if (o != null) {
                if (o instanceof GameplayLootType type) {
                    return lootTypes.contains(type);
                }
            }

            return false;
        };
    }

    @NotNull
    private BiPredicate<Object, DisplayAdditionReasons> tradePredicate(List<TradeLootType> lootTypes) {
        return (o, r) -> {
            if (o != null) {
                if (o instanceof TradeLootType type) {
                    return lootTypes.contains(type);
                }
            }

            return false;
        };
    }

    private record Holder<D extends ReiBaseDisplay, T, U>(CategoryIdentifier<D> identifier, ReiBaseCategory<D, U> category, BiFunction<T, DisplayAdditionReasons, D> filler) {}
}
