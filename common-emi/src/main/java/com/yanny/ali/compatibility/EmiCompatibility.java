package com.yanny.ali.compatibility;

import com.mojang.logging.LogUtils;
import com.yanny.ali.Utils;
import com.yanny.ali.compatibility.common.GenericUtils;
import com.yanny.ali.compatibility.emi.EmiBlockLoot;
import com.yanny.ali.compatibility.emi.EmiEntityLoot;
import com.yanny.ali.compatibility.emi.EmiGameplayLoot;
import com.yanny.ali.network.AbstractClient;
import com.yanny.ali.platform.Services;
import com.yanny.ali.registries.LootCategories;
import com.yanny.ali.registries.LootCategory;
import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootTable;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@EmiEntrypoint
public class EmiCompatibility implements EmiPlugin {
    private static final Logger LOGGER = LogUtils.getLogger();

    @Override
    public void register(EmiRegistry emiRegistry) {
        registerLootTable(emiRegistry);
    }

    private void registerLootTable(EmiRegistry registry) {
        AbstractClient client = Services.PLATFORM.getInfoPropagator().client();
        ClientLevel level = Minecraft.getInstance().level;

        if (client != null && level != null) {
            Map<ResourceKey<LootTable>, LootTable> map = GenericUtils.getLootTables();
            Map<LootCategory<Block>, EmiRecipeCategory> blockCategoryMap = LootCategories.BLOCK_LOOT_CATEGORIES.entrySet().stream().collect(Collectors.toMap(
                    Map.Entry::getValue,
                    (r) -> new EmiRecipeCategory(r.getKey(), EmiStack.of(r.getValue().getIcon()))
            ));
            Map<LootCategory<Entity>, EmiRecipeCategory> entityCategoryMap = LootCategories.ENTITY_LOOT_CATEGORIES.entrySet().stream().collect(Collectors.toMap(
                    Map.Entry::getValue,
                    (r) -> new EmiRecipeCategory(r.getKey(), EmiStack.of(r.getValue().getIcon()))
            ));
            Map<LootCategory<String>, EmiRecipeCategory> gameplayCategoryMap = LootCategories.GAMEPLAY_LOOT_CATEGORIES.entrySet().stream().collect(Collectors.toMap(
                    Map.Entry::getValue,
                    (r) -> new EmiRecipeCategory(r.getKey(), EmiStack.of(r.getValue().getIcon()))
            ));

            blockCategoryMap.values().forEach(registry::addCategory);
            entityCategoryMap.values().forEach(registry::addCategory);
            gameplayCategoryMap.values().forEach(registry::addCategory);

            EmiRecipeCategory blockCategory = createCategory(LootCategories.BLOCK_LOOT);
            EmiRecipeCategory plantCategory = createCategory(LootCategories.PLANT_LOOT);
            EmiRecipeCategory entityCategory = createCategory(LootCategories.ENTITY_LOOT);
            EmiRecipeCategory gameplayCategory = createCategory(LootCategories.GAMEPLAY_LOOT);

            registry.addCategory(blockCategory);
            registry.addCategory(plantCategory);
            registry.addCategory(entityCategory);
            registry.addCategory(gameplayCategory);

            for (Block block : BuiltInRegistries.BLOCK) {
                ResourceKey<LootTable> location = block.getLootTable();
                LootTable lootEntry = map.get(location);

                if (lootEntry != null) {
                    EmiRecipeCategory category = null;

                    if (LootCategories.PLANT_LOOT.validate(block)) {
                        category = plantCategory;
                    } else {
                        for (Map.Entry<LootCategory<Block>, EmiRecipeCategory> entry : blockCategoryMap.entrySet()) {
                            if (entry.getKey().validate(block)) {
                                category = entry.getValue();
                            }
                        }

                        if (category == null) {
                            category = blockCategory;
                        }
                    }

                    registry.addRecipe(new EmiBlockLoot(category, ResourceLocation.fromNamespaceAndPath(location.location().getNamespace(), "/" + location.location().getPath()), block, lootEntry, GenericUtils.getItems(location)));
                    map.remove(location);
                }
            }

            for (EntityType<?> entityType : BuiltInRegistries.ENTITY_TYPE) {
                List<Entity> entityList = new LinkedList<>();

                if (entityType == EntityType.SHEEP) {
                    for (DyeColor color : DyeColor.values()) {
                        Sheep sheep;

                        try {
                            sheep = (Sheep) entityType.create(level);
                        } catch (Throwable e) {
                            LOGGER.warn("Failed to create colored sheep with color {}: {}", color.getSerializedName(), e.getMessage());
                            continue;
                        }

                        if (sheep != null) {
                            sheep.setColor(color);
                            entityList.add(sheep);
                        }
                    }

                    Sheep sheep;

                    try {
                        sheep = (Sheep) entityType.create(level);
                    } catch (Throwable e) {
                        LOGGER.warn("Failed to create sheep: {}", e.getMessage());
                        continue;
                    }

                    if (sheep != null) {
                        sheep.setSheared(true);
                        entityList.add(sheep);
                    }
                } else {
                    Entity entity;

                    try {
                        entity = entityType.create(level);
                    } catch (Throwable e) {
                        LOGGER.warn("Failed to create entity {}: {}", BuiltInRegistries.ENTITY_TYPE.getKey(entityType), e.getMessage());
                        continue;
                    }

                    entityList.add(entity);
                }

                entityList.forEach((entity) -> {
                    if (entity instanceof Mob mob) {
                        ResourceKey<LootTable> location = mob.getLootTable();
                        LootTable lootEntry = map.get(location);

                        if (lootEntry != null) {
                            EmiRecipeCategory category = null;

                            for (Map.Entry<LootCategory<Entity>, EmiRecipeCategory> entry : entityCategoryMap.entrySet()) {
                                if (entry.getKey().validate(entity)) {
                                    category = entry.getValue();
                                }
                            }

                            if (category == null) {
                                category = entityCategory;
                            }

                            registry.addRecipe(new EmiEntityLoot(category, ResourceLocation.fromNamespaceAndPath(location.location().getNamespace(), "/" + location.location().getPath()), entity, lootEntry, GenericUtils.getItems(location)));
                            map.remove(location);
                        }
                    }
                });
            }

            for (Map.Entry<ResourceKey<LootTable>, LootTable> entry : map.entrySet()) {
                ResourceKey<LootTable> location = entry.getKey();
                EmiRecipeCategory category = null;

                for (Map.Entry<LootCategory<String>, EmiRecipeCategory> gameplayEntry : gameplayCategoryMap.entrySet()) {
                    if (gameplayEntry.getKey().validate(location.location().getPath())) {
                        category = gameplayEntry.getValue();
                    }
                }

                if (category == null) {
                    category = gameplayCategory;
                }

                registry.addRecipe(new EmiGameplayLoot(category, ResourceLocation.fromNamespaceAndPath(location.location().getNamespace(), "/" + location.location().getPath()), entry.getValue(), GenericUtils.getItems(location)));
            }
        }
    }

    @NotNull
    private static EmiRecipeCategory createCategory(LootCategory<?> category) {
        return new EmiRecipeCategory(Utils.modLoc(category.getKey()), EmiStack.of(category.getIcon()));
    }
}
