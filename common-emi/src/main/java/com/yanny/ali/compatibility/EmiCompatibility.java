package com.yanny.ali.compatibility;

import com.mojang.logging.LogUtils;
import com.yanny.ali.Utils;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.compatibility.common.GenericUtils;
import com.yanny.ali.compatibility.emi.EmiBlockLoot;
import com.yanny.ali.compatibility.emi.EmiEntityLoot;
import com.yanny.ali.compatibility.emi.EmiGameplayLoot;
import com.yanny.ali.compatibility.emi.EmiTradeLoot;
import com.yanny.ali.manager.AliClientRegistry;
import com.yanny.ali.manager.PluginManager;
import com.yanny.ali.registries.LootCategories;
import com.yanny.ali.registries.LootCategory;
import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Block;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@EmiEntrypoint
public class EmiCompatibility implements EmiPlugin {
    private static final Logger LOGGER = LogUtils.getLogger();

    private final CompletableFuture<Pair<Map<ResourceLocation, IDataNode>, Map<ResourceLocation, IDataNode>>> futureData = new CompletableFuture<>();

    @Override
    public void register(EmiRegistry emiRegistry) {
        PluginManager.CLIENT_REGISTRY.setOnDoneListener((lootData, tradeData) -> futureData.complete(Pair.of(lootData, tradeData)));

        futureData.thenAccept((pair) -> {
            try {
                registerData(emiRegistry, pair.getLeft(), pair.getRight());
            } catch (Throwable e) {
                e.printStackTrace();
                LOGGER.error("Failed to register data with error {}", e.getMessage());
            }
        });

        if (!futureData.isDone()) {
            LOGGER.info("Blocking this thread until all data are received!");
        }

        try {
            futureData.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            LOGGER.error("Failed to finish registering data with error {}", e.getMessage());
        }
    }

    private void registerData(EmiRegistry registry, Map<ResourceLocation, IDataNode> lootData, Map<ResourceLocation, IDataNode> tradeData) {
        AliClientRegistry clientRegistry = PluginManager.CLIENT_REGISTRY;
        ClientLevel level = Minecraft.getInstance().level;

        LOGGER.info("Adding loot information to EMI");

        if (level != null) {
            Map<LootCategory<Block>, EmiRecipeCategory> blockCategoryMap = LootCategories.BLOCK_LOOT_CATEGORIES.entrySet().stream().collect(Collectors.toMap(
                    Map.Entry::getValue,
                    (r) -> new EmiRecipeCategory(r.getKey(), EmiStack.of(r.getValue().getIcon()))
            ));
            Map<LootCategory<EntityType<?>>, EmiRecipeCategory> entityCategoryMap = LootCategories.ENTITY_LOOT_CATEGORIES.entrySet().stream().collect(Collectors.toMap(
                    Map.Entry::getValue,
                    (r) -> new EmiRecipeCategory(r.getKey(), EmiStack.of(r.getValue().getIcon()))
            ));
            Map<LootCategory<String>, EmiRecipeCategory> gameplayCategoryMap = LootCategories.GAMEPLAY_LOOT_CATEGORIES.entrySet().stream().collect(Collectors.toMap(
                    Map.Entry::getValue,
                    (r) -> new EmiRecipeCategory(r.getKey(), EmiStack.of(r.getValue().getIcon()))
            ));
            Map<LootCategory<String>, EmiRecipeCategory> tradeCategoryMap = LootCategories.TRADE_LOOT_CATEGORIES.entrySet().stream().collect(Collectors.toMap(
                    Map.Entry::getValue,
                    (r) -> new EmiRecipeCategory(r.getKey(), EmiStack.of(r.getValue().getIcon()))
            ));

            blockCategoryMap.values().forEach(registry::addCategory);
            entityCategoryMap.values().forEach(registry::addCategory);
            gameplayCategoryMap.values().forEach(registry::addCategory);
            tradeCategoryMap.values().forEach(registry::addCategory);

            EmiRecipeCategory blockCategory = createCategory(LootCategories.BLOCK_LOOT);
            EmiRecipeCategory plantCategory = createCategory(LootCategories.PLANT_LOOT);
            EmiRecipeCategory entityCategory = createCategory(LootCategories.ENTITY_LOOT);
            EmiRecipeCategory gameplayCategory = createCategory(LootCategories.GAMEPLAY_LOOT);
            EmiRecipeCategory tradeCategory = createCategory(LootCategories.TRADE_LOOT);

            registry.addCategory(blockCategory);
            registry.addCategory(plantCategory);
            registry.addCategory(entityCategory);
            registry.addCategory(gameplayCategory);
            registry.addCategory(tradeCategory);

            GenericUtils.processData(
                    level,
                    clientRegistry,
                    lootData,
                    tradeData,
                    (node, location, block, outputs) -> {
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

                        registry.addRecipe(new EmiBlockLoot(category, location, block, node, outputs));
                    },
                    (node, location, entity, outputs) -> {
                        EmiRecipeCategory category = null;

                        for (Map.Entry<LootCategory<EntityType<?>>, EmiRecipeCategory> entry : entityCategoryMap.entrySet()) {
                            if (entry.getKey().validate(entity)) {
                                category = entry.getValue();
                            }
                        }

                        if (category == null) {
                            category = entityCategory;
                        }

                        registry.addRecipe(new EmiEntityLoot(category, location, entity, node, outputs));
                    },
                    (node, location, outputs) -> {
                        EmiRecipeCategory category = null;

                        for (Map.Entry<LootCategory<String>, EmiRecipeCategory> gameplayEntry : gameplayCategoryMap.entrySet()) {
                            if (gameplayEntry.getKey().validate(location.getPath())) {
                                category = gameplayEntry.getValue();
                            }
                        }

                        if (category == null) {
                            category = gameplayCategory;
                        }

                        registry.addRecipe(new EmiGameplayLoot(category, location, node, outputs));
                    },
                    (tradeEntry, location, inputs, outputs) -> {
                        EmiRecipeCategory category = null;

                        for (Map.Entry<LootCategory<String>, EmiRecipeCategory> e : tradeCategoryMap.entrySet()) {
                            if (e.getKey().validate(location.getPath())) {
                                category = e.getValue();
                            }
                        }

                        if (category == null) {
                            category = tradeCategory;
                        }

                        registry.addRecipe(new EmiTradeLoot(category, new ResourceLocation(location.getNamespace(), "/" + location.getPath()), location.getPath(), tradeEntry, inputs, outputs));
                    },
                    (tradeEntry, location, inputs, outputs) -> {
                        EmiRecipeCategory category = null;

                        for (Map.Entry<LootCategory<String>, EmiRecipeCategory> e : tradeCategoryMap.entrySet()) {
                            if (e.getKey().validate(location.getPath())) {
                                category = e.getValue();
                            }
                        }

                        if (category == null) {
                            category = tradeCategory;
                        }

                        registry.addRecipe(new EmiTradeLoot(category, new ResourceLocation(location.getNamespace(), "/" + location.getPath()), location.getPath(), tradeEntry, inputs, outputs));
                    }
            );
        } else {
            LOGGER.warn("EMI integration was not loaded! Level is null!");
        }
    }

    @NotNull
    private static EmiRecipeCategory createCategory(LootCategory<?> category) {
        return new EmiRecipeCategory(Utils.modLoc(category.getKey()), EmiStack.of(category.getIcon()));
    }
}
