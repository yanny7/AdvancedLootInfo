package com.yanny.ali.rei.compatibility;

import com.mojang.logging.LogUtils;
import com.yanny.ali.compatibility.common.*;
import com.yanny.ali.configuration.AliConfig;
import com.yanny.ali.configuration.LootCategory;
import com.yanny.ali.manager.AliClientRegistry;
import com.yanny.ali.manager.PluginManager;
import com.yanny.ali.rei.compatibility.rei.*;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.client.registry.display.reason.DisplayAdditionReasons;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import org.apache.commons.lang3.function.TriFunction;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@SuppressWarnings("UnstableApiUsage")
public class ReiCompatibility implements REIClientPlugin {
    private static final Logger LOGGER = LogUtils.getLogger();

    private final Map<LootCategory<Block>, Holder<ReiBlockDisplay, BlockLootType, Block>> blockCategories = new LinkedHashMap<>();
    private final Map<LootCategory<EntityType<?>>, Holder<ReiEntityDisplay, EntityLootType, EntityType<?>>> entityCategories = new LinkedHashMap<>();
    private final Map<LootCategory<Identifier>, Holder<ReiGameplayDisplay, GameplayLootType, Identifier>> gameplayCategories = new LinkedHashMap<>();
    private final Map<LootCategory<Identifier>, Holder<ReiTradeDisplay, TradeLootType, Identifier>> tradeCategories = new LinkedHashMap<>();

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

        registerCategory(registry, blockCategories);
        registerCategory(registry, entityCategories);
        registerCategory(registry, gameplayCategories);
        registerCategory(registry, tradeCategories);
    }

    @Override
    public void registerDisplays(DisplayRegistry registry) {
        GenericUtils.register(registry, this::registerData);
    }

    private void registerData(DisplayRegistry registry, byte[] fullCompressedData) {
        AliClientRegistry clientRegistry = PluginManager.CLIENT_REGISTRY;
        AliConfig config = PluginManager.COMMON_REGISTRY.getConfiguration();
        ClientLevel level = Minecraft.getInstance().level;

        LOGGER.info("Adding loot information to REI");

        if (level != null) {
            Map<Holder<ReiBlockDisplay, BlockLootType, Block>, List<BlockLootType>> blockRecipeTypes = new HashMap<>();
            Map<Holder<ReiEntityDisplay, EntityLootType, EntityType<?>>, List<EntityLootType>> entityRecipeTypes = new HashMap<>();
            Map<Holder<ReiGameplayDisplay, GameplayLootType, Identifier>, List<GameplayLootType>> gameplayRecipeTypes = new HashMap<>();
            Map<Holder<ReiTradeDisplay, TradeLootType, Identifier>, List<TradeLootType>> tradeRecipeTypes = new HashMap<>();

            GenericUtils.processData(
                    level,
                    clientRegistry,
                    config,
                    fullCompressedData,
                    (node, location, block, outputs) ->
                            addLootType(blockCategories, blockRecipeTypes, block, () -> new BlockLootType(block, node, outputs)),
                    (node, location, entity, outputs) ->
                            addLootType(entityCategories, entityRecipeTypes, entity, () -> new EntityLootType(entity, location, node, outputs)),
                    (node, location, outputs) ->
                            addLootType(gameplayCategories, gameplayRecipeTypes, location, () -> new GameplayLootType(node, location, outputs)),
                    (tradeEntry, location, profession, inputs, outputs) ->
                            addLootType(tradeCategories, tradeRecipeTypes, location, () -> new TradeLootType(profession, tradeEntry, location.getPath(), inputs, outputs)),
                    (tradeEntry, location, inputs, outputs) ->
                            addLootType(tradeCategories, tradeRecipeTypes, location, () -> new TradeLootType(Set.of(), Set.of(), tradeEntry, location.getPath(), inputs, outputs))
            );

            registerFiller(registry, blockRecipeTypes, ReiCompatibility::blockPredicate);
            registerFiller(registry, entityRecipeTypes, ReiCompatibility::entityPredicate);
            registerFiller(registry, gameplayRecipeTypes, ReiCompatibility::gameplayPredicate);
            registerFiller(registry, tradeRecipeTypes, ReiCompatibility::tradePredicate);
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
    private static BiPredicate<Object, DisplayAdditionReasons> blockPredicate(List<BlockLootType> lootTypes) {
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
    private static BiPredicate<Object, DisplayAdditionReasons> entityPredicate(List<EntityLootType> lootTypes) {
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
    private static BiPredicate<Object, DisplayAdditionReasons> gameplayPredicate(List<GameplayLootType> lootTypes) {
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
    private static BiPredicate<Object, DisplayAdditionReasons> tradePredicate(List<TradeLootType> lootTypes) {
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

    private static <D extends ReiBaseDisplay, T, U> void registerCategory(CategoryRegistry registry, Map<LootCategory<U>, Holder<D, T, U>> categories) {
        for (Map.Entry<LootCategory<U>, Holder<D, T, U>> entry : categories.entrySet()) {
            ReiBaseCategory<D, U> category = entry.getValue().category;
            Ingredient catalyst = entry.getKey().getCatalyst();

            registry.add(category);

            if (catalyst != null) {
                registry.addWorkstations(category.getCategoryIdentifier(), EntryIngredients.ofIngredient(catalyst));
            }
        }
    }

    private static <D extends ReiBaseDisplay, T, U> void registerFiller(DisplayRegistry registry, Map<Holder<D, T, U>, List<T>> categories, Function<List<T>, BiPredicate<Object, DisplayAdditionReasons>> predicate) {
        for (Map.Entry<Holder<D, T, U>, List<T>> entry : categories.entrySet()) {
            registry.registerFillerWithReason(predicate.apply(entry.getValue()), entry.getKey().filler());
            entry.getValue().forEach(registry::add);
        }
    }

    private static <D extends ReiBaseDisplay, T, U> void addLootType(Map<LootCategory<U>, Holder<D, T, U>> categories, Map<Holder<D, T, U>, List<T>> types, U object, Supplier<T> supplier) {
        Holder<D, T, U> recipeHolder = null;

        for (Holder<D, T, U> holder : categories.values()) {
            if (holder.category.getLootCategory().validate(object)) {
                if (holder.category.getLootCategory().isHidden()) {
                    return;
                }

                recipeHolder = holder;
                break;
            }
        }

        if (recipeHolder != null) {
            types.computeIfAbsent(recipeHolder, (b) -> new LinkedList<>()).add(supplier.get());
        }
    }
}
