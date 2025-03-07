package com.yanny.ali.compatibility;

import com.yanny.ali.Utils;
import com.yanny.ali.compatibility.common.BlockLootType;
import com.yanny.ali.compatibility.common.EntityLootType;
import com.yanny.ali.compatibility.common.GameplayLootType;
import com.yanny.ali.compatibility.rei.*;
import com.yanny.ali.network.AbstractClient;
import com.yanny.ali.platform.Services;
import com.yanny.ali.plugin.entry.LootTableEntry;
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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import org.apache.commons.lang3.function.TriFunction;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ReiCompatibility implements REIClientPlugin {
    private final List<Holder<ReiBlockDisplay, BlockLootType, Block>> blockCategoryList = new LinkedList<>();
    private final List<Holder<ReiEntityDisplay, EntityLootType, Entity>> entityCategoryList = new LinkedList<>();
    private final List<Holder<ReiGameplayDisplay, GameplayLootType, String>> gameplayCategoryList = new LinkedList<>();

    @Override
    public void registerCategories(CategoryRegistry registry) {
        blockCategoryList.clear();
        entityCategoryList.clear();
        gameplayCategoryList.clear();

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

        for (Holder<ReiBlockDisplay, BlockLootType, Block> holder : blockCategoryList) {
            registry.add(holder.category);
        }

        for (Holder<ReiEntityDisplay, EntityLootType, Entity> holder : entityCategoryList) {
            registry.add(holder.category);
        }

        for (Holder<ReiGameplayDisplay, GameplayLootType, String> holder : gameplayCategoryList) {
            registry.add(holder.category);
        }
    }

    @Override
    public void registerDisplays(DisplayRegistry registry) {
        AbstractClient client = Services.PLATFORM.getInfoPropagator().client();
        ClientLevel level = Minecraft.getInstance().level;

        if (client != null && level != null) {
            Map<ResourceLocation, LootTableEntry> map = new HashMap<>(client.lootEntries.stream().collect(Collectors.toMap((l) -> l.location, l -> l.value)));
            Map<Holder<ReiBlockDisplay, BlockLootType, Block>, List<BlockLootType>> blockRecipeTypes = new HashMap<>();
            Map<Holder<ReiEntityDisplay, EntityLootType, Entity>, List<EntityLootType>> entityRecipeTypes = new HashMap<>();
            Map<Holder<ReiGameplayDisplay, GameplayLootType, String>, List<GameplayLootType>> gameplayRecipeTypes = new HashMap<>();

            for (Block block : BuiltInRegistries.BLOCK) {
                ResourceLocation location = block.getLootTable();
                LootTableEntry lootEntry = map.get(location);

                if (lootEntry != null) {
                    for (Holder<ReiBlockDisplay, BlockLootType, Block> holder : blockCategoryList) {
                        if (holder.category.getLootCategory().validate(block)) {
                            blockRecipeTypes.computeIfAbsent(holder, (b) -> new LinkedList<>()).add(new BlockLootType(block, lootEntry));
                            break;
                        }
                    }

                    map.remove(location);
                }
            }

            for (EntityType<?> entityType : BuiltInRegistries.ENTITY_TYPE) {
                List<Entity> entityList = new LinkedList<>();

                if (entityType == EntityType.SHEEP) {
                    for (DyeColor color : DyeColor.values()) {
                        Sheep sheep = (Sheep) entityType.create(level);

                        if (sheep != null) {
                            sheep.setColor(color);
                            entityList.add(sheep);
                        }
                    }

                    Sheep sheep = (Sheep) entityType.create(level);

                    if (sheep != null) {
                        sheep.setSheared(true);
                        entityList.add(sheep);
                    }
                } else {
                    entityList.add(entityType.create(level));
                }

                for (Entity entity : entityList) {
                    if (entity instanceof Mob mob) {
                        ResourceLocation location = mob.getLootTable();
                        LootTableEntry lootEntry = map.get(location);

                        if (lootEntry != null && entityType.create(level) != null) {
                            for (Holder<ReiEntityDisplay, EntityLootType, Entity> holder : entityCategoryList) {
                                if (holder.category.getLootCategory().validate(entity)) {
                                    entityRecipeTypes.computeIfAbsent(holder, (b) -> new LinkedList<>()).add(new EntityLootType(entity, lootEntry));
                                    break;
                                }
                            }

                            map.remove(location);
                        }
                    }
                }
            }

            for (Map.Entry<ResourceLocation, LootTableEntry> entry : new HashMap<>(map).entrySet()) {
                ResourceLocation location = entry.getKey();

                for (Holder<ReiGameplayDisplay, GameplayLootType, String> holder : gameplayCategoryList) {
                    if (holder.category.getLootCategory().validate(location.getPath())) {
                        gameplayRecipeTypes.computeIfAbsent(holder, (b) -> new LinkedList<>()).add(new GameplayLootType(entry.getValue(), "/" + location.getPath()));
                        break;
                    }
                }

                map.remove(location);
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

    private record Holder<D extends ReiBaseDisplay, T, U>(CategoryIdentifier<D> identifier, ReiBaseCategory<D, U> category, Function<T, D> filler) {}
}
