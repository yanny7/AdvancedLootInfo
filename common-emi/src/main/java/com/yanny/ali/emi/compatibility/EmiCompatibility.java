package com.yanny.ali.emi.compatibility;

import com.mojang.logging.LogUtils;
import com.yanny.ali.compatibility.common.GenericUtils;
import com.yanny.ali.configuration.AliConfig;
import com.yanny.ali.configuration.LootCategory;
import com.yanny.ali.emi.compatibility.emi.EmiBlockLoot;
import com.yanny.ali.emi.compatibility.emi.EmiEntityLoot;
import com.yanny.ali.emi.compatibility.emi.EmiGameplayLoot;
import com.yanny.ali.emi.compatibility.emi.EmiTradeLoot;
import com.yanny.ali.manager.AliClientRegistry;
import com.yanny.ali.manager.AliCommonRegistry;
import com.yanny.ali.manager.PluginManager;
import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@EmiEntrypoint
public class EmiCompatibility implements EmiPlugin {
    private static final Logger LOGGER = LogUtils.getLogger();

    @Override
    public void register(EmiRegistry emiRegistry) {
        GenericUtils.register(emiRegistry, this::registerData);
    }

    private void registerData(EmiRegistry registry, byte[] fullCompressedData) {
        AliClientRegistry clientRegistry = PluginManager.CLIENT_REGISTRY;
        AliCommonRegistry commonRegistry = PluginManager.COMMON_REGISTRY;
        AliConfig config = commonRegistry.getConfiguration();
        ClientLevel level = Minecraft.getInstance().level;

        LOGGER.info("Adding loot information to EMI");

        if (level != null) {
            Map<LootCategory<Block>, EmiRecipeCategory> blockCategories = config.blockCategories.stream().collect(getCollector());
            Map<LootCategory<EntityType<?>>, EmiRecipeCategory> entityCategories = config.entityCategories.stream().collect(getCollector());
            Map<LootCategory<ResourceLocation>, EmiRecipeCategory> gameplayCategories = config.gameplayCategories.stream().collect(getCollector());
            Map<LootCategory<ResourceLocation>, EmiRecipeCategory> tradeCategories = config.tradeCategories.stream().collect(getCollector());

            blockCategories.values().forEach(registry::addCategory);
            entityCategories.values().forEach(registry::addCategory);
            gameplayCategories.values().forEach(registry::addCategory);
            tradeCategories.values().forEach(registry::addCategory);

            GenericUtils.processData(
                    level,
                    clientRegistry,
                    config,
                    fullCompressedData,
                    (node, location, block, outputs) -> {
                        Map.Entry<LootCategory<Block>, EmiRecipeCategory> category = null;

                        for (Map.Entry<LootCategory<Block>, EmiRecipeCategory> entry : blockCategories.entrySet()) {
                            if (entry.getKey().validate(block)) {
                                if (entry.getKey().isHidden()) {
                                    return;
                                }

                                category = entry;
                                break;
                            }
                        }

                        if (category != null) {
                            registry.addRecipe(new EmiBlockLoot(category.getValue(), location, block, node, outputs));

                            if (!category.getKey().getCatalyst().isEmpty()) {
                                registry.addWorkstation(category.getValue(), EmiIngredient.of(category.getKey().getCatalyst()));
                            }
                        }
                    },
                    (node, location, entity, outputs) -> {
                        Map.Entry<LootCategory<EntityType<?>>, EmiRecipeCategory> category = null;

                        for (Map.Entry<LootCategory<EntityType<?>>, EmiRecipeCategory> entry : entityCategories.entrySet()) {
                            if (entry.getKey().validate(entity)) {
                                if (entry.getKey().isHidden()) {
                                    return;
                                }

                                category = entry;
                                break;
                            }
                        }

                        if (category != null) {
                            registry.addRecipe(new EmiEntityLoot(category.getValue(), location, entity, node, outputs));

                            if (!category.getKey().getCatalyst().isEmpty()) {
                                registry.addWorkstation(category.getValue(), EmiIngredient.of(category.getKey().getCatalyst()));
                            }
                        }
                    },
                    (node, location, outputs) -> {
                        Map.Entry<LootCategory<ResourceLocation>, EmiRecipeCategory> category = null;

                        for (Map.Entry<LootCategory<ResourceLocation>, EmiRecipeCategory> entry : gameplayCategories.entrySet()) {
                            if (entry.getKey().validate(location)) {
                                if (entry.getKey().isHidden()) {
                                    return;
                                }

                                category = entry;
                                break;
                            }
                        }

                        if (category != null) {
                            registry.addRecipe(new EmiGameplayLoot(category.getValue(), location, node, outputs));

                            if (!category.getKey().getCatalyst().isEmpty()) {
                                registry.addWorkstation(category.getValue(), EmiIngredient.of(category.getKey().getCatalyst()));
                            }
                        }
                    },
                    (tradeEntry, location, profession, inputs, outputs) -> {
                        Map.Entry<LootCategory<ResourceLocation>, EmiRecipeCategory> category = null;

                        for (Map.Entry<LootCategory<ResourceLocation>, EmiRecipeCategory> entry : tradeCategories.entrySet()) {
                            if (entry.getKey().validate(location)) {
                                if (entry.getKey().isHidden()) {
                                    return;
                                }

                                category = entry;
                                break;
                            }
                        }

                        if (category != null) {
                            registry.addRecipe(new EmiTradeLoot(category.getValue(), location, profession, tradeEntry, inputs, outputs));

                            if (!category.getKey().getCatalyst().isEmpty()) {
                                registry.addWorkstation(category.getValue(), EmiIngredient.of(category.getKey().getCatalyst()));
                            }
                        }
                    },
                    (tradeEntry, location, inputs, outputs) -> {
                        Map.Entry<LootCategory<ResourceLocation>, EmiRecipeCategory> category = null;

                        for (Map.Entry<LootCategory<ResourceLocation>, EmiRecipeCategory> entry : tradeCategories.entrySet()) {
                            if (entry.getKey().validate(location)) {
                                if (entry.getKey().isHidden()) {
                                    return;
                                }

                                category = entry;
                                break;
                            }
                        }

                        if (category != null) {
                            registry.addRecipe(new EmiTradeLoot(category.getValue(), location, null, tradeEntry, inputs, outputs));

                            if (!category.getKey().getCatalyst().isEmpty()) {
                                registry.addWorkstation(category.getValue(), EmiIngredient.of(category.getKey().getCatalyst()));
                            }
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
