package com.yanny.ali.jei.compatibility;

import com.mojang.logging.LogUtils;
import com.yanny.ali.Utils;
import com.yanny.ali.compatibility.common.*;
import com.yanny.ali.configuration.AliConfig;
import com.yanny.ali.configuration.LootCategory;
import com.yanny.ali.jei.compatibility.jei.*;
import com.yanny.ali.manager.AliClientRegistry;
import com.yanny.ali.manager.PluginManager;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@JeiPlugin
public class JeiCompatibility implements IModPlugin {
    private static final Logger LOGGER = LogUtils.getLogger();

    private final Map<LootCategory<Block>, JeiBlockLoot> blockCategories = new LinkedHashMap<>();
    private final Map<LootCategory<EntityType<?>>, JeiEntityLoot> entityCategories = new LinkedHashMap<>();
    private final Map<LootCategory<ResourceLocation>, JeiGameplayLoot> gameplayCategories = new LinkedHashMap<>();
    private final Map<LootCategory<ResourceLocation>, JeiTradeLoot> tradeCategories = new LinkedHashMap<>();

    @Override
    public void onRuntimeUnavailable() {
        blockCategories.clear();
        entityCategories.clear();
        gameplayCategories.clear();
        tradeCategories.clear();
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        AliConfig config = PluginManager.COMMON_REGISTRY.getConfiguration();
        IGuiHelper guiHelper = registration.getJeiHelpers().getGuiHelper();

        blockCategories.clear();
        entityCategories.clear();
        gameplayCategories.clear();
        tradeCategories.clear();

        blockCategories.putAll(config.blockCategories.stream().collect(getCollector(guiHelper, JeiBlockLoot::new)));
        entityCategories.putAll(config.entityCategories.stream().collect(getCollector(guiHelper, JeiEntityLoot::new)));
        gameplayCategories.putAll(config.gameplayCategories.stream().collect(getCollector(guiHelper, JeiGameplayLoot::new)));
        tradeCategories.putAll(config.tradeCategories.stream().collect(getCollector(guiHelper, JeiTradeLoot::new)));

        blockCategories.values().forEach(registration::addRecipeCategories);
        entityCategories.values().forEach(registration::addRecipeCategories);
        gameplayCategories.values().forEach(registration::addRecipeCategories);
        tradeCategories.values().forEach(registration::addRecipeCategories);
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        GenericUtils.register(registration, this::registerData);
    }

    private void registerData(IRecipeRegistration registration, byte[] fullCompressedData) {
        AliClientRegistry clientRegistry = PluginManager.CLIENT_REGISTRY;
        AliConfig config = PluginManager.COMMON_REGISTRY.getConfiguration();
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
                    config,
                    fullCompressedData,
                    (node, location, block, outputs) -> {
                        RecipeType<RecipeHolder<BlockLootType>> recipeType = null;

                        for (JeiBlockLoot recipeCategory : blockCategories.values()) {
                            if (recipeCategory.getLootCategory().validate(block)) {
                                if (recipeCategory.getLootCategory().isHidden()) {
                                    return;
                                }

                                recipeType = recipeCategory.getRecipeType();
                                break;
                            }
                        }

                        if (recipeType != null) {
                            blockRecipeTypes.computeIfAbsent(recipeType, (p) -> new LinkedList<>()).add(new BlockLootType(block, node, Collections.emptyList(), outputs));
                        }
                    },
                    (node, location, entity, outputs) -> {
                        RecipeType<RecipeHolder<EntityLootType>> recipeType = null;

                        for (JeiEntityLoot recipeCategory : entityCategories.values()) {
                            if (recipeCategory.getLootCategory().validate(entity)) {
                                if (recipeCategory.getLootCategory().isHidden()) {
                                    return;
                                }

                                recipeType = recipeCategory.getRecipeType();
                                break;
                            }
                        }

                        if (recipeType != null) {
                            entityRecipeTypes.computeIfAbsent(recipeType, (p) -> new LinkedList<>()).add(new EntityLootType(entity, location, node, Collections.emptyList(), outputs));
                        }
                    },
                    (node, location, outputs) -> {
                        RecipeType<RecipeHolder<GameplayLootType>> recipeType = null;

                        for (JeiGameplayLoot recipeCategory : gameplayCategories.values()) {
                            if (recipeCategory.getLootCategory().validate(location)) {
                                if (recipeCategory.getLootCategory().isHidden()) {
                                    return;
                                }

                                recipeType = recipeCategory.getRecipeType();
                                break;
                            }
                        }

                        if (recipeType != null) {
                            gameplayRecipeTypes.computeIfAbsent(recipeType, (p) -> new LinkedList<>()).add(new GameplayLootType(node, location, Collections.emptyList(), outputs));
                        }
                    },
                    (node, location, profession, inputs, outputs) -> {
                        RecipeType<RecipeHolder<TradeLootType>> recipeType = null;

                        for (JeiTradeLoot recipeCategory : tradeCategories.values()) {
                            if (recipeCategory.getLootCategory().validate(location)) {
                                if (recipeCategory.getLootCategory().isHidden()) {
                                    return;
                                }

                                recipeType = recipeCategory.getRecipeType();
                                break;
                            }
                        }

                        if (recipeType != null) {
                            Set<Block> pois = GenericUtils.getJobSites(profession);
                            Set<Item> accepts = GenericUtils.getRequestedItems(profession);

                            tradeRecipeTypes.computeIfAbsent(recipeType, (p) -> new LinkedList<>()).add(new TradeLootType(pois, accepts, node, location.getPath(), inputs, outputs));
                        }
                    },
                    (node, location, inputs, outputs) -> {
                        RecipeType<RecipeHolder<TradeLootType>> recipeType = null;

                        for (JeiTradeLoot recipeCategory : tradeCategories.values()) {
                            if (recipeCategory.getLootCategory().validate(location)) {
                                if (recipeCategory.getLootCategory().isHidden()) {
                                    return;
                                }

                                recipeType = recipeCategory.getRecipeType();
                                break;
                            }
                        }

                        if (recipeType != null) {
                            tradeRecipeTypes.computeIfAbsent(recipeType, (p) -> new LinkedList<>()).add(new TradeLootType(Set.of(), Set.of(), node, location.getPath(), inputs, outputs));
                        }
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
        RecipeType<RecipeHolder<V>> recipeType = (RecipeType<RecipeHolder<V>>) (Object) RecipeType.create(e.getKey().getNamespace(), e.getKey().getPath(), RecipeHolder.class);
        Component title = Component.translatable("emi.category." + e.getKey().getNamespace() + "." + e.getKey().getPath().replace('/', '.'));
        return constructor.construct(guiHelper, recipeType, e, title, guiHelper.createDrawableItemStack(e.getIcon().getDefaultInstance()));
    }

    @NotNull
    private static  <T, U, V extends IType> Collector<LootCategory<U>, ?, Map<LootCategory<U>, T>> getCollector(IGuiHelper guiHelper, LootConstructor<T, U, V> supplier) {
        return Collectors.toMap(
                (e) -> e,
                (e) -> createCategory(guiHelper, e, supplier),
                (a, b) -> a,
                LinkedHashMap::new
        );
    }

    @FunctionalInterface
    private interface LootConstructor<T, U, V extends IType> {
        T construct(IGuiHelper guiHelper, RecipeType<RecipeHolder<V>> recipeType, LootCategory<U> lootCategory, Component title, IDrawable icon);
    }
}
