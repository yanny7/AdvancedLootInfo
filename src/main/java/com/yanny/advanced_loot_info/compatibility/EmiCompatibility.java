package com.yanny.advanced_loot_info.compatibility;

import com.yanny.advanced_loot_info.AdvancedLootInfoMod;
import com.yanny.advanced_loot_info.Utils;
import com.yanny.advanced_loot_info.loot.LootTableEntry;
import com.yanny.advanced_loot_info.network.NetworkUtils;
import com.yanny.advanced_loot_info.registries.LootCategories;
import com.yanny.advanced_loot_info.registries.LootCategory;
import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@EmiEntrypoint
public class EmiCompatibility implements EmiPlugin {
    @Override
    public void register(EmiRegistry emiRegistry) {
        registerLootTable(emiRegistry);
    }

    private void registerLootTable(EmiRegistry registry) {
        NetworkUtils.Client client = AdvancedLootInfoMod.INFO_PROPAGATOR.client();
        ClientLevel level = Minecraft.getInstance().level;

        if (client != null && level != null) {
            Map<ResourceLocation, LootTableEntry> map = new HashMap<>(client.lootEntries.stream().collect(Collectors.toMap((l) -> l.location, l -> l.value)));
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

            for (Block block : ForgeRegistries.BLOCKS) {
                ResourceLocation location = block.getLootTable();
                LootTableEntry lootEntry = map.get(location);

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

                    registry.addRecipe(new EmiBlockLoot(category, new ResourceLocation(location.getNamespace(), "/" + location.getPath()), block, lootEntry));
                    map.remove(location);
                }
            }

            for (EntityType<?> entityType : ForgeRegistries.ENTITY_TYPES) {
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

                entityList.forEach((entity) -> {
                    if (entity instanceof Mob mob) {
                        ResourceLocation location = mob.getLootTable();
                        LootTableEntry lootEntry = map.get(location);

                        if (lootEntry != null && entityType.create(level) != null) {
                            EmiRecipeCategory category = null;

                            for (Map.Entry<LootCategory<Entity>, EmiRecipeCategory> entry : entityCategoryMap.entrySet()) {
                                if (entry.getKey().validate(entity)) {
                                    category = entry.getValue();
                                }
                            }

                            if (category == null) {
                                category = entityCategory;
                            }

                            registry.addRecipe(new EmiEntityLoot(category, new ResourceLocation(location.getNamespace(), "/" + location.getPath()), entity, lootEntry));
                            map.remove(location);
                        }
                    }
                });
            }

            for (Map.Entry<ResourceLocation, LootTableEntry> entry : map.entrySet()) {
                ResourceLocation location = entry.getKey();
                EmiRecipeCategory category = null;

                for (Map.Entry<LootCategory<String>, EmiRecipeCategory> gameplayEntry : gameplayCategoryMap.entrySet()) {
                    if (gameplayEntry.getKey().validate(location.getPath())) {
                        category = gameplayEntry.getValue();
                    }
                }

                if (category == null) {
                    category = gameplayCategory;
                }

                registry.addRecipe(new EmiGameplayLoot(category, new ResourceLocation(location.getNamespace(), "/" + location.getPath()), entry.getValue()));
            }
        }
    }

    @NotNull
    private static EmiRecipeCategory createCategory(LootCategory<?> category) {
        return new EmiRecipeCategory(Utils.modLoc(category.getKey()), EmiStack.of(category.getIcon()));
    }
}
