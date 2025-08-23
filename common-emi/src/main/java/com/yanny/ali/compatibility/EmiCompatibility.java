package com.yanny.ali.compatibility;

import com.mojang.logging.LogUtils;
import com.yanny.ali.Utils;
import com.yanny.ali.api.IDataNode;
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
import dev.emi.emi.runtime.EmiReloadManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.registries.BuiltInRegistries;
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
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@EmiEntrypoint
public class EmiCompatibility implements EmiPlugin {
    private static final Logger LOGGER = LogUtils.getLogger();

    @Override
    public void register(EmiRegistry emiRegistry) {
        PluginManager.CLIENT_REGISTRY.setOnDoneListener((lootData, tradeData) -> registerData(emiRegistry, lootData, tradeData));
    }

    private void registerData(EmiRegistry registry, Map<ResourceKey<LootTable>, IDataNode> lootData, Map<ResourceLocation, IDataNode> tradeData) {
        AliClientRegistry clientRegistry = PluginManager.CLIENT_REGISTRY;
        ClientLevel level = Minecraft.getInstance().level;

        LOGGER.info("Adding loot information to EMI");

        if (level != null) {
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

            for (Block block : BuiltInRegistries.BLOCK) {
                ResourceKey<LootTable> location = block.getLootTable();
                IDataNode lootEntry = lootData.get(location);

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

                    registry.addRecipe(new EmiBlockLoot(category, ResourceLocation.fromNamespaceAndPath(location.location().getNamespace(), "/" + location.location().getPath()), block, lootEntry, clientRegistry.getLootItems(location)));
                    lootData.remove(location);
                }
            }

            for (EntityType<?> entityType : BuiltInRegistries.ENTITY_TYPE) {
                List<Entity> entityList = clientRegistry.createEntities(entityType, level);

                entityList.forEach((entity) -> {
                    if (entity instanceof Mob mob) {
                        ResourceKey<LootTable> location = mob.getLootTable();
                        IDataNode lootEntry = lootData.get(location);

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

                            registry.addRecipe(new EmiEntityLoot(category, ResourceLocation.fromNamespaceAndPath(location.location().getNamespace(), "/" + location.location().getPath()), entity, lootEntry, clientRegistry.getLootItems(location)));
                            lootData.remove(location);
                        }
                    }
                });
            }

            for (Map.Entry<ResourceKey<LootTable>, IDataNode> entry : lootData.entrySet()) {
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

                registry.addRecipe(new EmiGameplayLoot(category, ResourceLocation.fromNamespaceAndPath(location.location().getNamespace(), "/" + location.location().getPath()), entry.getValue(), clientRegistry.getLootItems(location)));
            }

            List<Map.Entry<ResourceKey<VillagerProfession>, VillagerProfession>> entries = BuiltInRegistries.VILLAGER_PROFESSION.entrySet()
                    .stream()
                    .sorted(Comparator.comparing(a -> a.getKey().location().getPath()))
                    .toList();

            for (Map.Entry<ResourceKey<VillagerProfession>, VillagerProfession> entry : entries) {
                ResourceLocation location = entry.getKey().location();
                IDataNode tradeEntry = tradeData.get(location);

                if (tradeEntry != null) {
                    EmiRecipeCategory category = null;
                    List<ItemStack> inputs = clientRegistry.getTradeInputItems(location).stream().map(Item::getDefaultInstance).toList();
                    List<ItemStack> outputs = clientRegistry.getTradeOutputItems(location).stream().map(Item::getDefaultInstance).toList();

                    for (Map.Entry<LootCategory<String>, EmiRecipeCategory> e : tradeCategoryMap.entrySet()) {
                        if (e.getKey().validate(location.getPath())) {
                            category = e.getValue();
                        }
                    }

                    if (category == null) {
                        category = tradeCategory;
                    }

                    registry.addRecipe(new EmiTradeLoot(category, new ResourceLocation(location.getNamespace(), "/" + location.getPath()), location.getPath(), tradeEntry, inputs, outputs));
                    tradeData.remove(location);
                }
            }

            for (Map.Entry<ResourceLocation, IDataNode> entry : tradeData.entrySet()) {
                ResourceLocation location = entry.getKey();
                IDataNode tradeEntry = tradeData.get(location);

                if (tradeEntry != null) {
                    EmiRecipeCategory category = null;
                    List<ItemStack> inputs = clientRegistry.getTradeInputItems(location).stream().map(Item::getDefaultInstance).toList();
                    List<ItemStack> outputs = clientRegistry.getTradeOutputItems(location).stream().map(Item::getDefaultInstance).toList();

                    for (Map.Entry<LootCategory<String>, EmiRecipeCategory> e : tradeCategoryMap.entrySet()) {
                        if (e.getKey().validate(location.getPath())) {
                            category = e.getValue();
                        }
                    }

                    if (category == null) {
                        category = tradeCategory;
                    }

                    registry.addRecipe(new EmiTradeLoot(category, new ResourceLocation(location.getNamespace(), "/" + location.getPath()), location.getPath(), tradeEntry, inputs, outputs));
                    tradeData.remove(location);
                }
            }
        }

        if (EmiReloadManager.isLoaded()) {
            LOGGER.info("Loot information was added too late, requesting reload EMI");
            EmiReloadManager.reload();
        }
    }

    @NotNull
    private static EmiRecipeCategory createCategory(LootCategory<?> category) {
        return new EmiRecipeCategory(Utils.modLoc(category.getKey()), EmiStack.of(category.getIcon()));
    }
}
