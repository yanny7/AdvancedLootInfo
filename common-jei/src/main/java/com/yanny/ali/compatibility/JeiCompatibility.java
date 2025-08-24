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
import mezz.jei.api.recipe.types.IRecipeType;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
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

import java.util.*;
import java.util.stream.Collectors;

@JeiPlugin
public class JeiCompatibility implements IModPlugin {
    private static final Logger LOGGER = LogUtils.getLogger();

    private final List<JeiBlockLoot> blockCategoryList = new LinkedList<>();
    private final List<JeiEntityLoot> entityCategoryList = new LinkedList<>();
    private final List<JeiGameplayLoot> gameplayCategoryList = new LinkedList<>();
    private final List<JeiTradeLoot> tradeCategoryList = new LinkedList<>();

    @Override
    public void onRuntimeUnavailable() {
        blockCategoryList.clear();
        entityCategoryList.clear();
        gameplayCategoryList.clear();
        tradeCategoryList.clear();
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        IGuiHelper guiHelper = registration.getJeiHelpers().getGuiHelper();

        blockCategoryList.add(createCategory(guiHelper, LootCategories.PLANT_LOOT, JeiBlockLoot::new));
        blockCategoryList.addAll(LootCategories.BLOCK_LOOT_CATEGORIES.entrySet().stream()
                .map((e) -> createCategory(guiHelper, e, JeiBlockLoot::new))
                .collect(Collectors.toSet()));
        blockCategoryList.add(createCategory(guiHelper, LootCategories.BLOCK_LOOT, JeiBlockLoot::new));

        entityCategoryList.addAll(LootCategories.ENTITY_LOOT_CATEGORIES.entrySet().stream()
                .map((e) -> createCategory(guiHelper, e, JeiEntityLoot::new))
                .collect(Collectors.toSet()));
        entityCategoryList.add(createCategory(guiHelper, LootCategories.ENTITY_LOOT, JeiEntityLoot::new));

        gameplayCategoryList.addAll(LootCategories.GAMEPLAY_LOOT_CATEGORIES.entrySet().stream()
                .map((e) -> createCategory(guiHelper, e, JeiGameplayLoot::new))
                .collect(Collectors.toSet()));
        gameplayCategoryList.add(createCategory(guiHelper, LootCategories.GAMEPLAY_LOOT, JeiGameplayLoot::new));

        tradeCategoryList.addAll(LootCategories.TRADE_LOOT_CATEGORIES.entrySet().stream()
                .map((e) -> createCategory(guiHelper, e, JeiTradeLoot::new))
                .collect(Collectors.toSet()));
        tradeCategoryList.add(createCategory(guiHelper, LootCategories.TRADE_LOOT, JeiTradeLoot::new));

        blockCategoryList.forEach(registration::addRecipeCategories);
        entityCategoryList.forEach(registration::addRecipeCategories);
        gameplayCategoryList.forEach(registration::addRecipeCategories);
        tradeCategoryList.forEach(registration::addRecipeCategories);
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        LOGGER.info("Registering recipes");
        PluginManager.CLIENT_REGISTRY.setOnDoneListener((lootData, tradeData) -> registerData(registration, lootData, tradeData));
    }

    private void registerData(IRecipeRegistration registration, Map<ResourceKey<LootTable>, IDataNode> lootData, Map<ResourceLocation, IDataNode> tradeData) {
        AliClientRegistry clientRegistry = PluginManager.CLIENT_REGISTRY;
        ClientLevel level = Minecraft.getInstance().level;

        LOGGER.info("Adding loot information to JEI");

        if (level != null) {
            Map<IRecipeType<RecipeHolder<BlockLootType>>, List<BlockLootType>> blockRecipeTypes = new HashMap<>();
            Map<IRecipeType<RecipeHolder<EntityLootType>>, List<EntityLootType>> entityRecipeTypes = new HashMap<>();
            Map<IRecipeType<RecipeHolder<GameplayLootType>>, List<GameplayLootType>> gameplayRecipeTypes = new HashMap<>();
            Map<IRecipeType<RecipeHolder<TradeLootType>>, List<TradeLootType>> tradeRecipeTypes = new HashMap<>();

            for (Block block : BuiltInRegistries.BLOCK) {
                block.getLootTable().ifPresent((location) -> {
                    IDataNode lootEntry = lootData.get(location);

                if (lootEntry != null) {
                    IRecipeType<RecipeHolder<BlockLootType>> recipeType = null;

                        for (JeiBlockLoot recipeCategory : blockCategoryList) {
                            if (recipeCategory.getLootCategory().validate(block)) {
                                recipeType = recipeCategory.getRecipeType();
                                break;
                            }
                        }

                    if (recipeType != null) {
                        blockRecipeTypes.computeIfAbsent(recipeType, (p) -> new LinkedList<>()).add(new BlockLootType(block, lootEntry, clientRegistry.getLootItems(location), Collections.emptyList()));
                    }

                        lootData.remove(location);
                    }
                });
            }

            for (EntityType<?> entityType : BuiltInRegistries.ENTITY_TYPE) {
                List<Entity> entityList = clientRegistry.createEntities(entityType, level);

                for (Entity entity : entityList) {
                    if (entity instanceof Mob mob) {
                        mob.getLootTable().ifPresent((location) -> {
                            IDataNode lootEntry = lootData.get(location);

                        if (lootEntry != null) {
                            IRecipeType<RecipeHolder<EntityLootType>> recipeType = null;

                                for (JeiEntityLoot recipeCategory : entityCategoryList) {
                                    if (recipeCategory.getLootCategory().validate(entity)) {
                                        recipeType = recipeCategory.getRecipeType();
                                        break;
                                    }
                                }

                            if (recipeType != null) {
                                entityRecipeTypes.computeIfAbsent(recipeType, (p) -> new LinkedList<>()).add(new EntityLootType(entity, lootEntry, clientRegistry.getLootItems(location), Collections.emptyList()));
                            }

                                lootData.remove(location);
                            }
                        });
                    }
                }
            }

            for (Map.Entry<ResourceKey<LootTable>, IDataNode> entry : lootData.entrySet()) {
                ResourceKey<LootTable> location = entry.getKey();
                IRecipeType<RecipeHolder<GameplayLootType>> recipeType = null;

                for (JeiGameplayLoot recipeCategory : gameplayCategoryList) {
                    if (recipeCategory.getLootCategory().validate(location.location().getPath())) {
                        recipeType = recipeCategory.getRecipeType();
                        break;
                    }
                }

                if (recipeType != null) {
                    gameplayRecipeTypes.computeIfAbsent(recipeType, (p) -> new LinkedList<>()).add(new GameplayLootType(entry.getValue(), "/" + location.location().getPath(), clientRegistry.getLootItems(location), Collections.emptyList()));
                }
            }

            List<Map.Entry<ResourceKey<VillagerProfession>, VillagerProfession>> entries = BuiltInRegistries.VILLAGER_PROFESSION.entrySet()
                    .stream()
                    .sorted(Comparator.comparing(a -> a.getKey().location().getPath()))
                    .toList();

            for (Map.Entry<ResourceKey<VillagerProfession>, VillagerProfession> entry : entries) {
                ResourceLocation location = entry.getKey().location();
                IDataNode tradeEntry = tradeData.get(location);

                if (tradeEntry != null) {
                    IRecipeType<RecipeHolder<TradeLootType>> recipeType = null;
                    List<ItemStack> inputs = clientRegistry.getTradeInputItems(location).stream().map(Item::getDefaultInstance).toList();
                    List<ItemStack> outputs = clientRegistry.getTradeOutputItems(location).stream().map(Item::getDefaultInstance).toList();

                    for (JeiTradeLoot recipeCategory : tradeCategoryList) {
                        if (recipeCategory.getLootCategory().validate(location.getPath())) {
                            recipeType = recipeCategory.getRecipeType();
                            break;
                        }
                    }

                    if (recipeType != null) {
                        tradeRecipeTypes.computeIfAbsent(recipeType, (p) -> new LinkedList<>()).add(new TradeLootType(tradeEntry, location.getPath(), inputs, outputs));
                    }

                    tradeData.remove(location);
                }
            }

            for (Map.Entry<ResourceLocation, IDataNode> entry : tradeData.entrySet()) {
                ResourceLocation location = entry.getKey();
                IDataNode tradeEntry = tradeData.get(location);

                if (tradeEntry != null) {
                    IRecipeType<RecipeHolder<TradeLootType>> recipeType = null;
                    List<ItemStack> inputs = clientRegistry.getTradeInputItems(location).stream().map(Item::getDefaultInstance).toList();
                    List<ItemStack> outputs = clientRegistry.getTradeOutputItems(location).stream().map(Item::getDefaultInstance).toList();

                    for (JeiTradeLoot recipeCategory : tradeCategoryList) {
                        if (recipeCategory.getLootCategory().validate(location.getPath())) {
                            recipeType = recipeCategory.getRecipeType();
                            break;
                        }
                    }

                    if (recipeType != null) {
                        tradeRecipeTypes.computeIfAbsent(recipeType, (p) -> new LinkedList<>()).add(new TradeLootType(tradeEntry, location.getPath(), inputs, outputs));
                    }
                }
            }

            for (Map.Entry<IRecipeType<RecipeHolder<BlockLootType>>, List<BlockLootType>> entry : blockRecipeTypes.entrySet()) {
                registration.addRecipes(entry.getKey(), entry.getValue().stream().map((v) -> new RecipeHolder<>(v, null, null)).toList());
            }

            for (Map.Entry<IRecipeType<RecipeHolder<EntityLootType>>, List<EntityLootType>> entry : entityRecipeTypes.entrySet()) {
                registration.addRecipes(entry.getKey(), entry.getValue().stream().map((v) -> new RecipeHolder<>(v, null, null)).toList());
            }

            for (Map.Entry<IRecipeType<RecipeHolder<GameplayLootType>>, List<GameplayLootType>> entry : gameplayRecipeTypes.entrySet()) {
                registration.addRecipes(entry.getKey(), entry.getValue().stream().map((v) -> new RecipeHolder<>(v, null, null)).toList());
            }

            for (Map.Entry<IRecipeType<RecipeHolder<TradeLootType>>, List<TradeLootType>> entry : tradeRecipeTypes.entrySet()) {
                registration.addRecipes(entry.getKey(), entry.getValue().stream().map((v) -> new RecipeHolder<>(v, null, null)).toList());
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
        IRecipeType<RecipeHolder<V>> recipeType = (IRecipeType<RecipeHolder<V>>) (Object) IRecipeType.create(Utils.MOD_ID, e.getKey(), RecipeHolder.class);
        Component title = Component.translatable("emi.category." + Utils.MOD_ID + "." + e.getKey().replace('/', '.'));
        return constructor.construct(guiHelper, recipeType, e, title, guiHelper.createDrawableItemStack(e.getIcon()));
    }

    private static <T, U, V extends IType> T createCategory(IGuiHelper guiHelper, Map.Entry<ResourceLocation, LootCategory<U>> e, LootConstructor<T, U, V> constructor) {
        ResourceLocation id = e.getKey();
        IRecipeType<RecipeHolder<V>> recipeType = (IRecipeType<RecipeHolder<V>>) (Object) IRecipeType.create(id.getNamespace(), id.getPath(), RecipeHolder.class);
        Component title = Component.translatable("emi.category." + id.getNamespace() + "." + id.getPath().replace('/', '.'));
        return constructor.construct(guiHelper, recipeType, e.getValue(), title, guiHelper.createDrawableItemStack(e.getValue().getIcon()));
    }

    @FunctionalInterface
    private interface LootConstructor<T, U, V extends IType> {
        T construct(IGuiHelper guiHelper, IRecipeType<RecipeHolder<V>> recipeType, LootCategory<U> lootCategory, Component title, IDrawable icon);
    }
}
