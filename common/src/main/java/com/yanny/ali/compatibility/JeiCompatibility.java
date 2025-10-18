package com.yanny.ali.compatibility;

import com.mojang.logging.LogUtils;
import com.yanny.ali.Utils;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.compatibility.common.*;
import com.yanny.ali.compatibility.jei.*;
import com.yanny.ali.manager.AliClientRegistry;
import com.yanny.ali.manager.PluginManager;
import com.yanny.ali.registries.LootCategories;
import com.yanny.ali.registries.LootCategory;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Block;
import org.apache.commons.lang3.mutable.MutableObject;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

@JeiPlugin
public class JeiCompatibility implements IModPlugin {
    private static final Logger LOGGER = LogUtils.getLogger();

    private final Map<LootCategory<Block>, JeiBlockLoot> blockCategories = new HashMap<>();
    private final Map<LootCategory<EntityType<?>>, JeiEntityLoot> entityCategories = new HashMap<>();
    private final Map<LootCategory<String>, JeiGameplayLoot> gameplayCategories = new HashMap<>();
    private final Map<LootCategory<String>, JeiTradeLoot> tradeCategories = new HashMap<>();

    private final MutableObject<JeiBlockLoot> defaultBlockLoot = new MutableObject<>();
    private final MutableObject<JeiEntityLoot> defaultEntityLoot = new MutableObject<>();
    private final MutableObject<JeiGameplayLoot> defaultGameplayLoot = new MutableObject<>();
    private final MutableObject<JeiTradeLoot> defaultTradeLoot = new MutableObject<>();

    @Override
    public void onRuntimeUnavailable() {
        blockCategories.clear();
        entityCategories.clear();
        gameplayCategories.clear();
        tradeCategories.clear();

        defaultBlockLoot.setValue(null);
        defaultEntityLoot.setValue(null);
        defaultGameplayLoot.setValue(null);
        defaultTradeLoot.setValue(null);
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        IGuiHelper guiHelper = registration.getJeiHelpers().getGuiHelper();

        blockCategories.clear();
        entityCategories.clear();
        gameplayCategories.clear();
        tradeCategories.clear();

        blockCategories.putAll(LootCategories.BLOCK_LOOT_CATEGORIES.entrySet().stream()
                .map((e) -> new AbstractMap.SimpleEntry<>(e.getValue(), createCategory(guiHelper, e, JeiBlockLoot::new)))
                .collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue)));
        entityCategories.putAll(LootCategories.ENTITY_LOOT_CATEGORIES.entrySet().stream()
                .map((e) -> new AbstractMap.SimpleEntry<>(e.getValue(), createCategory(guiHelper, e, JeiEntityLoot::new)))
                .collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue)));
        gameplayCategories.putAll(LootCategories.GAMEPLAY_LOOT_CATEGORIES.entrySet().stream()
                .map((e) -> new AbstractMap.SimpleEntry<>(e.getValue(), createCategory(guiHelper, e, JeiGameplayLoot::new)))
                .collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue)));
        tradeCategories.putAll(LootCategories.TRADE_LOOT_CATEGORIES.entrySet().stream()
                .map((e) -> new AbstractMap.SimpleEntry<>(e.getValue(), createCategory(guiHelper, e, JeiTradeLoot::new)))
                .collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue)));

        blockCategories.values().forEach(registration::addRecipeCategories);
        entityCategories.values().forEach(registration::addRecipeCategories);
        gameplayCategories.values().forEach(registration::addRecipeCategories);
        tradeCategories.values().forEach(registration::addRecipeCategories);

        defaultBlockLoot.setValue(getDefaultCategory(guiHelper, JeiBlockLoot::new, registration, blockCategories, LootCategories.BLOCK_LOOT));
        defaultEntityLoot.setValue(getDefaultCategory(guiHelper, JeiEntityLoot::new, registration, entityCategories, LootCategories.ENTITY_LOOT));
        defaultGameplayLoot.setValue(getDefaultCategory(guiHelper, JeiGameplayLoot::new, registration, gameplayCategories, LootCategories.GAMEPLAY_LOOT));
        defaultTradeLoot.setValue(getDefaultCategory(guiHelper, JeiTradeLoot::new, registration, tradeCategories, LootCategories.TRADE_LOOT));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        CompletableFuture<Pair<Map<ResourceLocation, IDataNode>, Map<ResourceLocation, IDataNode>>> futureData = new CompletableFuture<>();

        PluginManager.CLIENT_REGISTRY.setOnDoneListener((lootData, tradeData) -> futureData.complete(Pair.of(lootData, tradeData)));

        if (!futureData.isDone()) {
            LOGGER.info("Blocking this thread until all data are received!");
        }

        try {
            Pair<Map<ResourceLocation, IDataNode>, Map<ResourceLocation, IDataNode>> pair = futureData.get(30, TimeUnit.SECONDS);

            registerData(registration, pair.getLeft(), pair.getRight());
        } catch (TimeoutException e) {
            futureData.cancel(true);
            PluginManager.CLIENT_REGISTRY.clearLootData();
            LOGGER.error("Failed to received data in 30 seconds, registration aborted!");
        } catch (Throwable e) {
            e.printStackTrace();
            LOGGER.error("Failed to finish registering data with error {}", e.getMessage());
        }
    }

    private void registerData(IRecipeRegistration registration, Map<ResourceLocation, IDataNode> lootData, Map<ResourceLocation, IDataNode> tradeData) {
        AliClientRegistry clientRegistry = PluginManager.CLIENT_REGISTRY;
        ClientLevel level = Minecraft.getInstance().level;

        LOGGER.info("Adding loot information to JEI");

        if (level != null) {
            Map<RecipeType<RecipeHolder<BlockLootType>>, List<BlockLootType>> blockRecipeTypes = new HashMap<>();
            Map<RecipeType<RecipeHolder<EntityLootType>>, List<EntityLootType>> entityRecipeTypes = new HashMap<>();
            Map<RecipeType<RecipeHolder<GameplayLootType>>, List<GameplayLootType>> gameplayRecipeTypes = new HashMap<>();
            Map<RecipeType<RecipeHolder<TradeLootType>>, List<TradeLootType>> tradeRecipeTypes = new HashMap<>();

            GenericUtils.processData(
                    level,
                    clientRegistry,
                    lootData,
                    tradeData,
                    (node, location, block, outputs) -> {
                        RecipeType<RecipeHolder<BlockLootType>> recipeType = null;

                        for (JeiBlockLoot recipeCategory : blockCategories.values()) {
                            if (recipeCategory.getLootCategory().validate(block)) {
                                recipeType = recipeCategory.getRecipeType();
                                break;
                            }
                        }

                        if (recipeType == null) {
                            recipeType = defaultBlockLoot.getValue().getRecipeType();
                        }

                        blockRecipeTypes.computeIfAbsent(recipeType, (p) -> new LinkedList<>()).add(new BlockLootType(block, node, Collections.emptyList(), outputs));
                    },
                    (node, location, entity, outputs) -> {
                        RecipeType<RecipeHolder<EntityLootType>> recipeType = null;

                        for (JeiEntityLoot recipeCategory : entityCategories.values()) {
                            if (recipeCategory.getLootCategory().validate(entity)) {
                                recipeType = recipeCategory.getRecipeType();
                                break;
                            }
                        }

                        if (recipeType == null) {
                            recipeType = defaultEntityLoot.getValue().getRecipeType();
                        }

                        entityRecipeTypes.computeIfAbsent(recipeType, (p) -> new LinkedList<>()).add(new EntityLootType(entity, location, node, Collections.emptyList(), outputs));
                    },
                    (node, location, outputs) -> {
                        RecipeType<RecipeHolder<GameplayLootType>> recipeType = null;

                        for (JeiGameplayLoot recipeCategory : gameplayCategories.values()) {
                            if (recipeCategory.getLootCategory().validate(location.getPath())) {
                                recipeType = recipeCategory.getRecipeType();
                                break;
                            }
                        }

                        if (recipeType == null) {
                            recipeType = defaultGameplayLoot.getValue().getRecipeType();
                        }

                        gameplayRecipeTypes.computeIfAbsent(recipeType, (p) -> new LinkedList<>()).add(new GameplayLootType(node, location.getPath(), Collections.emptyList(), outputs));
                    },
                    (node, location, inputs, outputs) -> {
                        RecipeType<RecipeHolder<TradeLootType>> recipeType = null;

                        for (JeiTradeLoot recipeCategory : tradeCategories.values()) {
                            if (recipeCategory.getLootCategory().validate(location.getPath())) {
                                recipeType = recipeCategory.getRecipeType();
                                break;
                            }
                        }

                        if (recipeType == null) {
                            recipeType = defaultTradeLoot.getValue().getRecipeType();
                        }

                        tradeRecipeTypes.computeIfAbsent(recipeType, (p) -> new LinkedList<>()).add(new TradeLootType(node, location.getPath(), inputs, outputs));
                    },
                    (node, location, inputs, outputs) -> {
                        RecipeType<RecipeHolder<TradeLootType>> recipeType = null;

                        for (JeiTradeLoot recipeCategory : tradeCategories.values()) {
                            if (recipeCategory.getLootCategory().validate(location.getPath())) {
                                recipeType = recipeCategory.getRecipeType();
                                break;
                            }
                        }

                        if (recipeType == null) {
                            recipeType = defaultTradeLoot.getValue().getRecipeType();
                        }

                        tradeRecipeTypes.computeIfAbsent(recipeType, (p) -> new LinkedList<>()).add(new TradeLootType(node, location.getPath(), inputs, outputs));
                    }
            );

            for (Map.Entry<RecipeType<RecipeHolder<BlockLootType>>, List<BlockLootType>> entry : blockRecipeTypes.entrySet()) {
                registration.addRecipes(entry.getKey(), entry.getValue().stream().map(RecipeHolder::new).toList());
            }

            for (Map.Entry<RecipeType<RecipeHolder<EntityLootType>>, List<EntityLootType>> entry : entityRecipeTypes.entrySet()) {
                registration.addRecipes(entry.getKey(), entry.getValue().stream().map(RecipeHolder::new).toList());
            }

            for (Map.Entry<RecipeType<RecipeHolder<GameplayLootType>>, List<GameplayLootType>> entry : gameplayRecipeTypes.entrySet()) {
                registration.addRecipes(entry.getKey(), entry.getValue().stream().map(RecipeHolder::new).toList());
            }

            for (Map.Entry<RecipeType<RecipeHolder<TradeLootType>>, List<TradeLootType>> entry : tradeRecipeTypes.entrySet()) {
                registration.addRecipes(entry.getKey(), entry.getValue().stream().map(RecipeHolder::new).toList());
            }
        } else {
            LOGGER.warn("JEI integration was not loaded! Level is null!");
        }
    }

    @NotNull
    @Override
    public ResourceLocation getPluginUid() {
        return Utils.modLoc("jei_plugin");
    }

    private static <T, U, V extends IType> T createCategory(IGuiHelper guiHelper, LootCategory<U> e, LootConstructor<T, U, V> constructor) {
        //noinspection unchecked
        RecipeType<RecipeHolder<V>> recipeType = (RecipeType<RecipeHolder<V>>) (Object) RecipeType.create(Utils.MOD_ID, e.getKey(), RecipeHolder.class);
        Component title = Component.translatable("emi.category." + Utils.MOD_ID + "." + e.getKey().replace('/', '.'));
        return constructor.construct(guiHelper, recipeType, e, title, guiHelper.createDrawableItemStack(e.getIcon()));
    }

    private static <T, U, V extends IType> T createCategory(IGuiHelper guiHelper, Map.Entry<ResourceLocation, LootCategory<U>> e, LootConstructor<T, U, V> constructor) {
        ResourceLocation id = e.getKey();
        //noinspection unchecked
        RecipeType<RecipeHolder<V>> recipeType = (RecipeType<RecipeHolder<V>>) (Object) RecipeType.create(id.getNamespace(), id.getPath(), RecipeHolder.class);
        Component title = Component.translatable("emi.category." + id.getNamespace() + "." + id.getPath().replace('/', '.'));
        return constructor.construct(guiHelper, recipeType, e.getValue(), title, guiHelper.createDrawableItemStack(e.getValue().getIcon()));
    }

    @FunctionalInterface
    private interface LootConstructor<T, U, V extends IType> {
        T construct(IGuiHelper guiHelper, RecipeType<RecipeHolder<V>> recipeType, LootCategory<U> lootCategory, Component title, IDrawable icon);
    }

    @NotNull
    private <T extends IRecipeCategory<?>, U, V extends IType> T getDefaultCategory(IGuiHelper guiHelper, LootConstructor<T, U, V> constructor,
                                                                                    IRecipeCategoryRegistration registry, Map<LootCategory<U>, T> categories,
                                                                                    LootCategory<U> defaultCategory) {
        T recipeCategory = categories.remove(defaultCategory);

        if (recipeCategory != null) {
            return recipeCategory;
        } else {
            T defaultRecipeCategory = createCategory(guiHelper, defaultCategory, constructor);
            registry.addRecipeCategories(defaultRecipeCategory);
            return defaultRecipeCategory;
        }
    }
}
