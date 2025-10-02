package com.yanny.ali.compatibility;

import com.mojang.logging.LogUtils;
import com.yanny.ali.Utils;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.compatibility.common.*;
import com.yanny.ali.compatibility.rei.*;
import com.yanny.ali.manager.AliClientRegistry;
import com.yanny.ali.manager.PluginManager;
import com.yanny.ali.registries.LootCategories;
import com.yanny.ali.registries.LootCategory;
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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ReiCompatibility implements REIClientPlugin {
    private static final Logger LOGGER = LogUtils.getLogger();

    private final List<Holder<ReiBlockDisplay, BlockLootType, Block>> blockCategoryList = new LinkedList<>();
    private final List<Holder<ReiEntityDisplay, EntityLootType, EntityType<?>>> entityCategoryList = new LinkedList<>();
    private final List<Holder<ReiGameplayDisplay, GameplayLootType, String>> gameplayCategoryList = new LinkedList<>();
    private final List<Holder<ReiTradeDisplay, TradeLootType, String>> tradeCategoryList = new LinkedList<>();

    private final CompletableFuture<Pair<Map<ResourceLocation, IDataNode>, Map<ResourceLocation, IDataNode>>> futureData = new CompletableFuture<>();

    @Override
    public void registerCategories(CategoryRegistry registry) {
        blockCategoryList.clear();
        entityCategoryList.clear();
        gameplayCategoryList.clear();
        tradeCategoryList.clear();

        blockCategoryList.add(createCategory(LootCategories.PLANT_LOOT, ReiBlockDisplay::new, ReiBlockCategory::new));
        blockCategoryList.addAll(LootCategories.BLOCK_LOOT_CATEGORIES.entrySet().stream()
                .map((e) -> createCategory(e, ReiBlockDisplay::new, ReiBlockCategory::new))
                .collect(Collectors.toSet()));
        blockCategoryList.add(createCategory(LootCategories.BLOCK_LOOT, ReiBlockDisplay::new, ReiBlockCategory::new));

        entityCategoryList.addAll(LootCategories.ENTITY_LOOT_CATEGORIES.entrySet().stream()
                .map((e) -> createCategory(e, ReiEntityDisplay::new, ReiEntityCategory::new))
                .collect(Collectors.toSet()));
        entityCategoryList.add(createCategory(LootCategories.ENTITY_LOOT, ReiEntityDisplay::new, ReiEntityCategory::new));

        gameplayCategoryList.addAll(LootCategories.GAMEPLAY_LOOT_CATEGORIES.entrySet().stream()
                .map((e) -> createCategory(e, ReiGameplayDisplay::new, ReiGameplayCategory::new))
                .collect(Collectors.toSet()));
        gameplayCategoryList.add(createCategory(LootCategories.GAMEPLAY_LOOT, ReiGameplayDisplay::new, ReiGameplayCategory::new));

        tradeCategoryList.addAll(LootCategories.TRADE_LOOT_CATEGORIES.entrySet().stream()
                .map((e) -> createCategory(e, ReiTradeDisplay::new, ReiTradeCategory::new))
                .collect(Collectors.toSet()));
        tradeCategoryList.add(createCategory(LootCategories.TRADE_LOOT, ReiTradeDisplay::new, ReiTradeCategory::new));

        for (Holder<ReiBlockDisplay, BlockLootType, Block> holder : blockCategoryList) {
            registry.add(holder.category);
        }

        for (Holder<ReiEntityDisplay, EntityLootType, EntityType<?>> holder : entityCategoryList) {
            registry.add(holder.category);
        }

        for (Holder<ReiGameplayDisplay, GameplayLootType, String> holder : gameplayCategoryList) {
            registry.add(holder.category);
        }

        for (Holder<ReiTradeDisplay, TradeLootType, String> holder : tradeCategoryList) {
            registry.add(holder.category);
        }
    }

    @Override
    public void registerDisplays(DisplayRegistry registry) {
        PluginManager.CLIENT_REGISTRY.setOnDoneListener((lootData, tradeData) -> futureData.complete(Pair.of(lootData, tradeData)));

        if (!futureData.isDone()) {
            LOGGER.info("Blocking this thread until all data are received!");
        }

        try {
            Pair<Map<ResourceLocation, IDataNode>, Map<ResourceLocation, IDataNode>> pair = futureData.get(10, TimeUnit.SECONDS);

            registerData(registry, pair.getLeft(), pair.getRight());
        } catch (TimeoutException e) {
            futureData.cancel(true);
            PluginManager.CLIENT_REGISTRY.clearLootData();
            LOGGER.error("Failed to received data in 10 seconds, registration aborted!");
        } catch (Throwable e) {
            e.printStackTrace();
            LOGGER.error("Failed to finish registering data with error {}", e.getMessage());
        }
    }

    private void registerData(DisplayRegistry registry, Map<ResourceLocation, IDataNode> lootData, Map<ResourceLocation, IDataNode> tradeData) {
        AliClientRegistry clientRegistry = PluginManager.CLIENT_REGISTRY;
        ClientLevel level = Minecraft.getInstance().level;

        LOGGER.info("Adding loot information to REI");

        if (level != null) {
            Map<Holder<ReiBlockDisplay, BlockLootType, Block>, List<BlockLootType>> blockRecipeTypes = new HashMap<>();
            Map<Holder<ReiEntityDisplay, EntityLootType, EntityType<?>>, List<EntityLootType>> entityRecipeTypes = new HashMap<>();
            Map<Holder<ReiGameplayDisplay, GameplayLootType, String>, List<GameplayLootType>> gameplayRecipeTypes = new HashMap<>();
            Map<Holder<ReiTradeDisplay, TradeLootType, String>, List<TradeLootType>> tradeRecipeTypes = new HashMap<>();

            GenericUtils.processData(
                    level,
                    clientRegistry,
                    lootData,
                    tradeData,
                    (node, location, block, outputs) -> {
                        for (Holder<ReiBlockDisplay, BlockLootType, Block> holder : blockCategoryList) {
                            if (holder.category.getLootCategory().validate(block)) {
                                blockRecipeTypes.computeIfAbsent(holder, (b) -> new LinkedList<>()).add(new BlockLootType(block, node, Collections.emptyList(), outputs));
                                break;
                            }
                        }
                    },
                    (node, location, entity, outputs) -> {
                        for (Holder<ReiEntityDisplay, EntityLootType, EntityType<?>> holder : entityCategoryList) {
                            if (holder.category.getLootCategory().validate(entity)) {
                                entityRecipeTypes.computeIfAbsent(holder, (b) -> new LinkedList<>()).add(new EntityLootType(entity, location, node, Collections.emptyList(), outputs));
                                break;
                            }
                        }
                    },
                    (node, location, outputs) -> {
                        for (Holder<ReiGameplayDisplay, GameplayLootType, String> holder : gameplayCategoryList) {
                            if (holder.category.getLootCategory().validate(location.getPath())) {
                                gameplayRecipeTypes.computeIfAbsent(holder, (b) -> new LinkedList<>()).add(new GameplayLootType(node, location.getPath(), Collections.emptyList(), outputs));
                                break;
                            }
                        }
                    },
                    (tradeEntry, location, inputs, outputs) -> {
                        for (Holder<ReiTradeDisplay, TradeLootType, String> holder : tradeCategoryList) {
                            if (holder.category.getLootCategory().validate(location.getPath())) {
                                tradeRecipeTypes.computeIfAbsent(holder, (b) -> new LinkedList<>()).add(new TradeLootType(tradeEntry, location.getPath(), inputs, outputs));
                                break;
                            }
                        }
                    },
                    (tradeEntry, location, inputs, outputs) -> {
                        for (Holder<ReiTradeDisplay, TradeLootType, String> holder : tradeCategoryList) {
                            if (holder.category.getLootCategory().validate(location.getPath())) {
                                tradeRecipeTypes.computeIfAbsent(holder, (b) -> new LinkedList<>()).add(new TradeLootType(tradeEntry, location.getPath(), inputs, outputs));
                                break;
                            }
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

            for (Map.Entry<Holder<ReiGameplayDisplay, GameplayLootType, String>, List<GameplayLootType>> entry : gameplayRecipeTypes.entrySet()) {
                registry.registerFiller(gameplayPredicate(entry.getValue()), entry.getKey().filler());
                entry.getValue().forEach(registry::add);
            }

            for (Map.Entry<Holder<ReiTradeDisplay, TradeLootType, String>, List<TradeLootType>> entry : tradeRecipeTypes.entrySet()) {
                registry.registerFiller(tradePredicate(entry.getValue()), entry.getKey().filler());
                entry.getValue().forEach(registry::add);
            }
        } else {
            LOGGER.warn("REI integration was not loaded! Level is null!");
        }
    }

    @NotNull
    private <D extends ReiBaseDisplay, T, U> Holder<D, T, U> createCategory(Map.Entry<ResourceLocation, LootCategory<U>> e,
                                                                         BiFunction<T, CategoryIdentifier<D>, D> displayFactory,
                                                                         TriFunction<CategoryIdentifier<D>, Component, LootCategory<U>, ReiBaseCategory<D, U>> categoryFactory) {
        ResourceLocation id = e.getKey();
        CategoryIdentifier<D> identifier = CategoryIdentifier.of(id.getNamespace(), id.getPath());
        Component title = Component.translatable("emi.category." + id.getNamespace() + "." + id.getPath().replace('/', '.'));
        Function<T, D> filler = (type) -> displayFactory.apply(type, identifier);
        return new Holder<>(identifier, categoryFactory.apply(identifier, title, e.getValue()), filler);
    }

    @NotNull
    private <D extends ReiBaseDisplay, T, U> Holder<D, T, U> createCategory(LootCategory<U> lootCategory,
                                                                            BiFunction<T, CategoryIdentifier<D>, D> displayFactory,
                                                                            TriFunction<CategoryIdentifier<D>, Component, LootCategory<U>, ReiBaseCategory<D, U>> categoryFactory) {
        CategoryIdentifier<D> identifier = CategoryIdentifier.of(Utils.MOD_ID, lootCategory.getKey());
        Component title = Component.translatable("emi.category." + Utils.MOD_ID + "." + lootCategory.getKey().replace('/', '.'));
        Function<T, D> filler = (type) -> displayFactory.apply(type, identifier);
        return new Holder<>(identifier, categoryFactory.apply(identifier, title, lootCategory), filler);
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
