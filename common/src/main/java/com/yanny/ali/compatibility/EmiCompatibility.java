package com.yanny.ali.compatibility;

import com.mojang.logging.LogUtils;
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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@EmiEntrypoint
public class EmiCompatibility implements EmiPlugin {
    private static final Logger LOGGER = LogUtils.getLogger();

    @Override
    public void register(EmiRegistry emiRegistry) {
        CompletableFuture<Pair<Map<ResourceLocation, IDataNode>, Map<ResourceLocation, IDataNode>>> futureData = new CompletableFuture<>();

        PluginManager.CLIENT_REGISTRY.setOnDoneListener((lootData, tradeData) -> futureData.complete(Pair.of(lootData, tradeData)));

        if (!futureData.isDone()) {
            LOGGER.info("Blocking this thread until all data are received!");
        }

        try {
            Pair<Map<ResourceLocation, IDataNode>, Map<ResourceLocation, IDataNode>> pair = futureData.get(30, TimeUnit.SECONDS);

            registerData(emiRegistry, pair.getLeft(), pair.getRight());
        } catch (TimeoutException e) {
            futureData.cancel(true);
            PluginManager.CLIENT_REGISTRY.clearLootData();
            LOGGER.error("Failed to received data in 30 seconds, registration aborted!");
        } catch (Throwable e) {
            e.printStackTrace();
            LOGGER.error("Failed to finish registering data with error {}", e.getMessage());
        }
    }

    private void registerData(EmiRegistry registry, Map<ResourceLocation, IDataNode> lootData, Map<ResourceLocation, IDataNode> tradeData) {
        AliClientRegistry clientRegistry = PluginManager.CLIENT_REGISTRY;
        ClientLevel level = Minecraft.getInstance().level;

        LOGGER.info("Adding loot information to EMI");

        if (level != null) {
            Map<LootCategory<Block>, EmiRecipeCategory> blockCategories = LootCategories.BLOCK_LOOT_CATEGORIES.entrySet().stream().collect(getCollector());
            Map<LootCategory<EntityType<?>>, EmiRecipeCategory> entityCategories = LootCategories.ENTITY_LOOT_CATEGORIES.entrySet().stream().collect(getCollector());
            Map<LootCategory<String>, EmiRecipeCategory> gameplayCategories = LootCategories.GAMEPLAY_LOOT_CATEGORIES.entrySet().stream().collect(getCollector());
            Map<LootCategory<String>, EmiRecipeCategory> tradeCategories = LootCategories.TRADE_LOOT_CATEGORIES.entrySet().stream().collect(getCollector());

            blockCategories.values().forEach(registry::addCategory);
            entityCategories.values().forEach(registry::addCategory);
            gameplayCategories.values().forEach(registry::addCategory);
            tradeCategories.values().forEach(registry::addCategory);

            EmiRecipeCategory defaultBlockCategory = getDefaultCategory(registry, blockCategories, LootCategories.BLOCK_LOOT);
            EmiRecipeCategory defaultEntityCategory = getDefaultCategory(registry, entityCategories, LootCategories.ENTITY_LOOT);
            EmiRecipeCategory defaultGameplayCategory = getDefaultCategory(registry, gameplayCategories, LootCategories.GAMEPLAY_LOOT);
            EmiRecipeCategory defaultTradeCategory = getDefaultCategory(registry, tradeCategories, LootCategories.TRADE_LOOT);

            GenericUtils.processData(
                    level,
                    clientRegistry,
                    lootData,
                    tradeData,
                    (node, location, block, outputs) -> {
                        EmiRecipeCategory category = null;

                        for (Map.Entry<LootCategory<Block>, EmiRecipeCategory> entry : blockCategories.entrySet()) {
                            if (entry.getKey().validate(block)) {
                                if (entry.getKey().isHidden()) {
                                    return;
                                }

                                category = entry.getValue();
                            }
                        }

                        if (category == null) {
                            if (LootCategories.BLOCK_LOOT.isHidden()) {
                                return;
                            }

                            category = defaultBlockCategory;
                        }

                        registry.addRecipe(new EmiBlockLoot(category, location, block, node, outputs));
                    },
                    (node, location, entity, outputs) -> {
                        EmiRecipeCategory category = null;

                        for (Map.Entry<LootCategory<EntityType<?>>, EmiRecipeCategory> entry : entityCategories.entrySet()) {
                            if (entry.getKey().validate(entity)) {
                                if (entry.getKey().isHidden()) {
                                    return;
                                }

                                category = entry.getValue();
                            }
                        }

                        if (category == null) {
                            if (LootCategories.ENTITY_LOOT.isHidden()) {
                                return;
                            }

                            category = defaultEntityCategory;
                        }

                        registry.addRecipe(new EmiEntityLoot(category, location, entity, node, outputs));
                    },
                    (node, location, outputs) -> {
                        EmiRecipeCategory category = null;

                        for (Map.Entry<LootCategory<String>, EmiRecipeCategory> entry : gameplayCategories.entrySet()) {
                            if (entry.getKey().validate(location.getPath())) {
                                if (entry.getKey().isHidden()) {
                                    return;
                                }

                                category = entry.getValue();
                            }
                        }

                        if (category == null) {
                            if (LootCategories.GAMEPLAY_LOOT.isHidden()) {
                                return;
                            }

                            category = defaultGameplayCategory;
                        }

                        registry.addRecipe(new EmiGameplayLoot(category, location, node, outputs));
                    },
                    (tradeEntry, location, inputs, outputs) -> {
                        EmiRecipeCategory category = null;

                        for (Map.Entry<LootCategory<String>, EmiRecipeCategory> entry : tradeCategories.entrySet()) {
                            if (entry.getKey().validate(location.getPath())) {
                                if (entry.getKey().isHidden()) {
                                    return;
                                }

                                category = entry.getValue();
                            }
                        }

                        if (category == null) {
                            if (LootCategories.TRADE_LOOT.isHidden()) {
                                return;
                            }

                            category = defaultTradeCategory;
                        }

                        registry.addRecipe(new EmiTradeLoot(category, location, tradeEntry, inputs, outputs));
                    },
                    (tradeEntry, location, inputs, outputs) -> {
                        EmiRecipeCategory category = null;

                        for (Map.Entry<LootCategory<String>, EmiRecipeCategory> entry : tradeCategories.entrySet()) {
                            if (entry.getKey().validate(location.getPath())) {
                                if (entry.getKey().isHidden()) {
                                    return;
                                }

                                category = entry.getValue();
                            }
                        }

                        if (category == null) {
                            if (LootCategories.TRADE_LOOT.isHidden()) {
                                return;
                            }

                            category = defaultTradeCategory;
                        }

                        registry.addRecipe(new EmiTradeLoot(category, location, tradeEntry, inputs, outputs));
                    }
            );
        } else {
            LOGGER.warn("EMI integration was not loaded! Level is null!");
        }
    }

    @NotNull
    private static <T> EmiRecipeCategory getDefaultCategory(EmiRegistry registry, Map<LootCategory<T>, EmiRecipeCategory> categories, LootCategory<T> defaultCategory) {
        EmiRecipeCategory recipeCategory = categories.remove(defaultCategory);

        if (recipeCategory != null) {
            return recipeCategory;
        } else {
            EmiRecipeCategory defaultRecipeCategory = new EmiRecipeCategory(defaultCategory.getKey(), EmiStack.of(defaultCategory.getIcon()));
            registry.addCategory(defaultRecipeCategory);
            return defaultRecipeCategory;
        }
    }

    @NotNull
    private static  <T> Collector<Map.Entry<ResourceLocation, LootCategory<T>>, ?, Map<LootCategory<T>, EmiRecipeCategory>> getCollector() {
        return Collectors.toMap(
                Map.Entry::getValue,
                (r) -> new EmiRecipeCategory(r.getKey(), EmiStack.of(r.getValue().getIcon()))
        );
    }
}
