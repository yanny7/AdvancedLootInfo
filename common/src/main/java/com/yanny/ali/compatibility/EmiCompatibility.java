package com.yanny.ali.compatibility;

import com.mojang.logging.LogUtils;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.compatibility.common.GenericUtils;
import com.yanny.ali.compatibility.emi.EmiBlockLoot;
import com.yanny.ali.compatibility.emi.EmiEntityLoot;
import com.yanny.ali.compatibility.emi.EmiGameplayLoot;
import com.yanny.ali.compatibility.emi.EmiTradeLoot;
import com.yanny.ali.configuration.LootCategory;
import com.yanny.ali.manager.AliClientRegistry;
import com.yanny.ali.manager.AliCommonRegistry;
import com.yanny.ali.manager.PluginManager;
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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@EmiEntrypoint
public class EmiCompatibility implements EmiPlugin {
    private static final Logger LOGGER = LogUtils.getLogger();

    @Override
    public void register(EmiRegistry emiRegistry) {
        CompletableFuture<Pair<Map<ResourceLocation, IDataNode>, Map<ResourceLocation, IDataNode>>> futureData = PluginManager.CLIENT_REGISTRY.getCurrentDataFuture();

        if (futureData.isDone()) {
            LOGGER.info("Data already received, processing instantly.");
        } else {
            LOGGER.info("Blocking this thread until all data are received!");
        }

        try {
            Pair<Map<ResourceLocation, IDataNode>, Map<ResourceLocation, IDataNode>> pair = futureData.get();

            registerData(emiRegistry, pair.getLeft(), pair.getRight());
        } catch (ExecutionException e) {
            Throwable cause = e.getCause() != null ? e.getCause() : e;

            if (cause instanceof TimeoutException) {
                futureData.cancel(true);
                PluginManager.CLIENT_REGISTRY.clearLootData();
                LOGGER.error("Failed to received data: Inactivity timeout occurred. Registration aborted!");
            } else {
                LOGGER.error("Failed to finish registering data with error {}", cause.getMessage());
                cause.printStackTrace();
            }
        } catch (Throwable e) {
            e.printStackTrace();
            LOGGER.error("Failed to finish registering data with unexpected error {}", e.getMessage());
        }
    }

    private void registerData(EmiRegistry registry, Map<ResourceLocation, IDataNode> lootData, Map<ResourceLocation, IDataNode> tradeData) {
        AliClientRegistry clientRegistry = PluginManager.CLIENT_REGISTRY;
        AliCommonRegistry commonRegistry = PluginManager.COMMON_REGISTRY;
        ClientLevel level = Minecraft.getInstance().level;

        LOGGER.info("Adding loot information to EMI");

        if (level != null) {
            Map<LootCategory<Block>, EmiRecipeCategory> blockCategories = commonRegistry.getConfiguration().blockCategories.stream().collect(getCollector());
            Map<LootCategory<EntityType<?>>, EmiRecipeCategory> entityCategories = commonRegistry.getConfiguration().entityCategories.stream().collect(getCollector());
            Map<LootCategory<ResourceLocation>, EmiRecipeCategory> gameplayCategories = commonRegistry.getConfiguration().gameplayCategories.stream().collect(getCollector());
            Map<LootCategory<ResourceLocation>, EmiRecipeCategory> tradeCategories = commonRegistry.getConfiguration().tradeCategories.stream().collect(getCollector());

            blockCategories.values().forEach(registry::addCategory);
            entityCategories.values().forEach(registry::addCategory);
            gameplayCategories.values().forEach(registry::addCategory);
            tradeCategories.values().forEach(registry::addCategory);

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
                                break;
                            }
                        }

                        if (category != null) {
                            registry.addRecipe(new EmiBlockLoot(category, location, block, node, outputs));
                        }
                    },
                    (node, location, entity, outputs) -> {
                        EmiRecipeCategory category = null;

                        for (Map.Entry<LootCategory<EntityType<?>>, EmiRecipeCategory> entry : entityCategories.entrySet()) {
                            if (entry.getKey().validate(entity)) {
                                if (entry.getKey().isHidden()) {
                                    return;
                                }

                                category = entry.getValue();
                                break;
                            }
                        }

                        if (category != null) {
                            registry.addRecipe(new EmiEntityLoot(category, location, entity, node, outputs));
                        }
                    },
                    (node, location, outputs) -> {
                        EmiRecipeCategory category = null;

                        for (Map.Entry<LootCategory<ResourceLocation>, EmiRecipeCategory> entry : gameplayCategories.entrySet()) {
                            if (entry.getKey().validate(location)) {
                                if (entry.getKey().isHidden()) {
                                    return;
                                }

                                category = entry.getValue();
                                break;
                            }
                        }

                        if (category != null) {
                            registry.addRecipe(new EmiGameplayLoot(category, location, node, outputs));
                        }
                    },
                    (tradeEntry, location, inputs, outputs) -> {
                        EmiRecipeCategory category = null;

                        for (Map.Entry<LootCategory<ResourceLocation>, EmiRecipeCategory> entry : tradeCategories.entrySet()) {
                            if (entry.getKey().validate(location)) {
                                if (entry.getKey().isHidden()) {
                                    return;
                                }

                                category = entry.getValue();
                                break;
                            }
                        }

                        if (category != null) {
                            registry.addRecipe(new EmiTradeLoot(category, location, tradeEntry, inputs, outputs));
                        }
                    },
                    (tradeEntry, location, inputs, outputs) -> {
                        EmiRecipeCategory category = null;

                        for (Map.Entry<LootCategory<ResourceLocation>, EmiRecipeCategory> entry : tradeCategories.entrySet()) {
                            if (entry.getKey().validate(location)) {
                                if (entry.getKey().isHidden()) {
                                    return;
                                }

                                category = entry.getValue();
                                break;
                            }
                        }

                        if (category != null) {
                            registry.addRecipe(new EmiTradeLoot(category, location, tradeEntry, inputs, outputs));
                        }
                    }
            );
        } else {
            LOGGER.warn("EMI integration was not loaded! Level is null!");
        }
    }

    @NotNull
    private static  <T> Collector<LootCategory<T>, ?, Map<LootCategory<T>, EmiRecipeCategory>> getCollector() {
        return Collectors.toMap(
                (r) -> r,
                (r) -> new EmiRecipeCategory(r.getKey(), EmiStack.of(r.getIcon())),
                (e, r) -> e,
                LinkedHashMap::new
        );
    }
}
