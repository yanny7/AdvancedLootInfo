package com.yanny.ali.compatibility;

import com.yanny.ali.CommonAliMod;
import com.yanny.ali.Utils;
import com.yanny.ali.compatibility.common.BlockLootType;
import com.yanny.ali.compatibility.common.EntityLootType;
import com.yanny.ali.compatibility.common.GameplayLootType;
import com.yanny.ali.compatibility.rei.*;
import com.yanny.ali.network.Client;
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
        Client client = CommonAliMod.INFO_PROPAGATOR.client();
        ClientLevel level = Minecraft.getInstance().level;

        for (Holder<ReiBlockDisplay, BlockLootType, Block> holder : blockCategoryList) {
            registry.registerFiller(blockPredicate(holder.category.getLootCategory()), holder.filler);
        }

        for (Holder<ReiEntityDisplay, EntityLootType, Entity> holder : entityCategoryList) {
            registry.registerFiller(entityPredicate(holder.category.getLootCategory()), holder.filler);
        }

        for (Holder<ReiGameplayDisplay, GameplayLootType, String> holder : gameplayCategoryList) {
            registry.registerFiller(gameplayPredicate(holder.category.getLootCategory()), holder.filler);
        }

        if (client != null && level != null) {
            Map<ResourceLocation, LootTableEntry> map = new HashMap<>(client.lootEntries.stream().collect(Collectors.toMap((l) -> l.location, l -> l.value)));

            for (Block block : BuiltInRegistries.BLOCK) {
                ResourceLocation location = block.getLootTable();
                LootTableEntry lootEntry = map.get(location);

                if (lootEntry != null) {
                    registry.add(new BlockLootType(block, lootEntry));
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
                            registry.add(new EntityLootType(entity, lootEntry));
                            map.remove(location);
                        }
                    }
                }
            }

            for (Map.Entry<ResourceLocation, LootTableEntry> entry : map.entrySet()) {
                ResourceLocation location = entry.getKey();

                registry.add(new GameplayLootType(entry.getValue(), "/" + location.getPath()));
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
    private Predicate<Object> blockPredicate(LootCategory<Block> lootCategory) {
        return (o) -> {
            if (o != null) {
                if (o instanceof BlockLootType type) {
                    return lootCategory.validate(type.block());
                }
            }

            return false;
        };
    }

    @NotNull
    private Predicate<Object> entityPredicate(LootCategory<Entity> lootCategory) {
        return (o) -> {
            if (o != null) {
                if (o instanceof EntityLootType type) {
                    return lootCategory.validate(type.entity());
                }
            }

            return false;
        };
    }

    @NotNull
    private Predicate<Object> gameplayPredicate(LootCategory<String> lootCategory) {
        return (o) -> {
            if (o != null) {
                if (o instanceof GameplayLootType type) {
                    return lootCategory.validate(type.id().substring(1));
                }
            }

            return false;
        };
    }

    private record Holder<D extends ReiBaseDisplay, T, U>(CategoryIdentifier<D> identifier, ReiBaseCategory<D, U> category, Function<T, D> filler) {}
}
