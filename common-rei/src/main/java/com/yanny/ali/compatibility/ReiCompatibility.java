package com.yanny.ali.compatibility;

import com.mojang.logging.LogUtils;
import com.yanny.ali.Utils;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.compatibility.common.BlockLootType;
import com.yanny.ali.compatibility.common.EntityLootType;
import com.yanny.ali.compatibility.common.GameplayLootType;
import com.yanny.ali.compatibility.common.TradeLootType;
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
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootTable;
import org.apache.commons.lang3.function.TriFunction;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ReiCompatibility implements REIClientPlugin {
    private static final Logger LOGGER = LogUtils.getLogger();

    private final List<Holder<ReiBlockDisplay, BlockLootType, Block>> blockCategoryList = new LinkedList<>();
    private final List<Holder<ReiEntityDisplay, EntityLootType, Entity>> entityCategoryList = new LinkedList<>();
    private final List<Holder<ReiGameplayDisplay, GameplayLootType, String>> gameplayCategoryList = new LinkedList<>();
    private final List<Holder<ReiTradeDisplay, TradeLootType, String>> tradeCategoryList = new LinkedList<>();

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

        for (Holder<ReiEntityDisplay, EntityLootType, Entity> holder : entityCategoryList) {
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
        PluginManager.CLIENT_REGISTRY.setOnDoneListener((lootData, tradeData) -> registerData(registry, lootData, tradeData));
    }

    private void registerData(DisplayRegistry registry, Map<ResourceKey<LootTable>, IDataNode> lootData, Map<ResourceLocation, IDataNode> tradeData) {
        AliClientRegistry clientRegistry = PluginManager.CLIENT_REGISTRY;
        ClientLevel level = Minecraft.getInstance().level;

        LOGGER.info("Adding loot information to REI");

        if (level != null) {
            Map<Holder<ReiBlockDisplay, BlockLootType, Block>, List<BlockLootType>> blockRecipeTypes = new HashMap<>();
            Map<Holder<ReiEntityDisplay, EntityLootType, Entity>, List<EntityLootType>> entityRecipeTypes = new HashMap<>();
            Map<Holder<ReiGameplayDisplay, GameplayLootType, String>, List<GameplayLootType>> gameplayRecipeTypes = new HashMap<>();
            Map<Holder<ReiTradeDisplay, TradeLootType, String>, List<TradeLootType>> tradeRecipeTypes = new HashMap<>();

            for (Block block : BuiltInRegistries.BLOCK) {
                ResourceKey<LootTable> location = block.getLootTable();
                IDataNode node = lootData.get(location);

                if (node != null) {
                    for (Holder<ReiBlockDisplay, BlockLootType, Block> holder : blockCategoryList) {
                        if (holder.category.getLootCategory().validate(block)) {
                            blockRecipeTypes.computeIfAbsent(holder, (b) -> new LinkedList<>()).add(new BlockLootType(block, node, clientRegistry.getLootItems(location), Collections.emptyList()));
                            break;
                        }
                    }

                    lootData.remove(location);
                }
            }

            for (EntityType<?> entityType : BuiltInRegistries.ENTITY_TYPE) {
                List<Entity> entityList = clientRegistry.createEntities(entityType, level);

                for (Entity entity : entityList) {
                    if (entity instanceof Mob mob) {
                        ResourceKey<LootTable> location = mob.getLootTable();
                        IDataNode node = lootData.get(location);

                        if (node != null) {
                            for (Holder<ReiEntityDisplay, EntityLootType, Entity> holder : entityCategoryList) {
                                if (holder.category.getLootCategory().validate(entity)) {
                                    entityRecipeTypes.computeIfAbsent(holder, (b) -> new LinkedList<>()).add(new EntityLootType(entity, node, clientRegistry.getLootItems(location), Collections.emptyList()));
                                    break;
                                }
                            }

                            lootData.remove(location);
                        }
                    }
                }
            }

            for (Map.Entry<ResourceKey<LootTable>, IDataNode> entry : new HashMap<>(lootData).entrySet()) {
                ResourceKey<LootTable> location = entry.getKey();

                for (Holder<ReiGameplayDisplay, GameplayLootType, String> holder : gameplayCategoryList) {
                    if (holder.category.getLootCategory().validate(location.location().getPath())) {
                        gameplayRecipeTypes.computeIfAbsent(holder, (b) -> new LinkedList<>()).add(new GameplayLootType(entry.getValue(), "/" + location.location().getPath(), clientRegistry.getLootItems(location), Collections.emptyList()));
                        break;
                    }
                }

                lootData.remove(location);
            }

            List<Map.Entry<ResourceKey<VillagerProfession>, VillagerProfession>> entries = BuiltInRegistries.VILLAGER_PROFESSION.entrySet()
                    .stream()
                    .sorted(Comparator.comparing(a -> a.getKey().location().getPath()))
                    .toList();

            for (Map.Entry<ResourceKey<VillagerProfession>, VillagerProfession> entry : entries) {
                ResourceLocation location = entry.getKey().location();
                IDataNode tradeEntry = tradeData.get(location);

                if (tradeEntry != null) {
                    List<ItemStack> inputs = clientRegistry.getTradeInputItems(location).stream().map(Item::getDefaultInstance).toList();
                    List<ItemStack> outputs = clientRegistry.getTradeOutputItems(location).stream().map(Item::getDefaultInstance).toList();

                    for (Holder<ReiTradeDisplay, TradeLootType, String> holder : tradeCategoryList) {
                        if (holder.category.getLootCategory().validate(location.getPath())) {
                            tradeRecipeTypes.computeIfAbsent(holder, (b) -> new LinkedList<>()).add(new TradeLootType(tradeEntry, location.getPath(), inputs, outputs));
                            break;
                        }
                    }

                    tradeData.remove(location);
                }
            }

            for (Map.Entry<ResourceLocation, IDataNode> entry : tradeData.entrySet()) {
                ResourceLocation location = entry.getKey();
                IDataNode tradeEntry = tradeData.get(location);

                if (tradeEntry != null) {
                    List<ItemStack> inputs = clientRegistry.getTradeInputItems(location).stream().map(Item::getDefaultInstance).toList();
                    List<ItemStack> outputs = clientRegistry.getTradeOutputItems(location).stream().map(Item::getDefaultInstance).toList();

                    for (Holder<ReiTradeDisplay, TradeLootType, String> holder : tradeCategoryList) {
                        if (holder.category.getLootCategory().validate(location.getPath())) {
                            tradeRecipeTypes.computeIfAbsent(holder, (b) -> new LinkedList<>()).add(new TradeLootType(tradeEntry, location.getPath(), inputs, outputs));
                            break;
                        }
                    }
                }
            }

            for (Map.Entry<Holder<ReiBlockDisplay, BlockLootType, Block>, List<BlockLootType>> entry : blockRecipeTypes.entrySet()) {
                registry.registerFiller(blockPredicate(entry.getValue()), entry.getKey().filler());
                entry.getValue().forEach(registry::add);
            }

            for (Map.Entry<Holder<ReiEntityDisplay, EntityLootType, Entity>, List<EntityLootType>> entry : entityRecipeTypes.entrySet()) {
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
        }

        if (!me.shedaniel.rei.api.common.plugins.PluginManager.getClientInstance().isReloading()) {
            LOGGER.info("Loot information was added too late, requesting reload REI");
            me.shedaniel.rei.api.common.plugins.PluginManager.getClientInstance().startReload();
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
