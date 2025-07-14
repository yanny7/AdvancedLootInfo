package com.yanny.ali.compatibility;

import com.mojang.logging.LogUtils;
import com.yanny.ali.Utils;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.compatibility.emi.EmiBlockLoot;
import com.yanny.ali.compatibility.emi.EmiEntityLoot;
import com.yanny.ali.compatibility.emi.EmiGameplayLoot;
import com.yanny.ali.manager.AliClientRegistry;
import com.yanny.ali.manager.PluginManager;
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
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

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
        AliClientRegistry clientRegistry = PluginManager.CLIENT_REGISTRY;
        AbstractClient client = Services.PLATFORM.getInfoPropagator().client();
        ClientLevel level = Minecraft.getInstance().level;

        if (client != null && level != null) {
            Map<ResourceKey<LootTable>, IDataNode> map = clientRegistry.getLootData();
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
                IDataNode lootEntry = map.get(location);

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

                    registry.addRecipe(new EmiBlockLoot(category, new ResourceLocation(location.location().getNamespace(), "/" + location.location().getPath()), block, lootEntry, clientRegistry.getItems(location)));
                    map.remove(location);
                }
            }

            for (EntityType<?> entityType : BuiltInRegistries.ENTITY_TYPE) {
                List<Entity> entityList = clientRegistry.createEntities(entityType, level);

                entityList.forEach((entity) -> {
                    if (entity instanceof Mob mob) {
                        ResourceKey<LootTable> location = mob.getLootTable();
                        IDataNode lootEntry = map.get(location);

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

                            registry.addRecipe(new EmiEntityLoot(category, new ResourceLocation(location.location().getNamespace(), "/" + location.location().getPath()), entity, lootEntry, clientRegistry.getItems(location)));
                            map.remove(location);
                        }
                    }
                });
            }

            for (Map.Entry<ResourceKey<LootTable>, IDataNode> entry : map.entrySet()) {
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

                registry.addRecipe(new EmiGameplayLoot(category, new ResourceLocation(location.location().getNamespace(), "/" + location.location().getPath()), entry.getValue(), clientRegistry.getItems(location)));
            }
        }
    }

    @NotNull
    private static EmiRecipeCategory createCategory(LootCategory<?> category) {
        return new EmiRecipeCategory(Utils.modLoc(category.getKey()), EmiStack.of(category.getIcon()));
    }
}
