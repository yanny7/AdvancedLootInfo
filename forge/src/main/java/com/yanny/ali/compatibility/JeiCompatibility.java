package com.yanny.ali.compatibility;

import com.mojang.logging.LogUtils;
import com.yanny.ali.AdvancedLootInfoMod;
import com.yanny.ali.Utils;
import com.yanny.ali.compatibility.common.BlockLootType;
import com.yanny.ali.compatibility.common.EntityLootType;
import com.yanny.ali.compatibility.common.GameplayLootType;
import com.yanny.ali.compatibility.jei.JeiBlockLoot;
import com.yanny.ali.compatibility.jei.JeiEntityLoot;
import com.yanny.ali.compatibility.jei.JeiGameplayLoot;
import com.yanny.ali.network.NetworkUtils;
import com.yanny.ali.plugin.entry.LootTableEntry;
import com.yanny.ali.registries.LootCategories;
import com.yanny.ali.registries.LootCategory;
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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@JeiPlugin
public class JeiCompatibility implements IModPlugin {
    private static final Logger LOGGER = LogUtils.getLogger();

    private final List<JeiBlockLoot> blockCategoryList = new LinkedList<>();
    private final List<JeiEntityLoot> entityCategoryList = new LinkedList<>();
    private final List<JeiGameplayLoot> gameplayCategoryList = new LinkedList<>();

    @Override
    public void registerCategories(@NotNull IRecipeCategoryRegistration registration) {
        IGuiHelper guiHelper = registration.getJeiHelpers().getGuiHelper();

        blockCategoryList.clear();
        entityCategoryList.clear();
        gameplayCategoryList.clear();

        blockCategoryList.add(createCategory(guiHelper, LootCategories.PLANT_LOOT, BlockLootType.class, JeiBlockLoot::new));
        blockCategoryList.addAll(LootCategories.BLOCK_LOOT_CATEGORIES.entrySet().stream()
                .map((e) -> createCategory(guiHelper, e, BlockLootType.class, JeiBlockLoot::new))
                .collect(Collectors.toSet()));
        blockCategoryList.add(createCategory(guiHelper, LootCategories.BLOCK_LOOT, BlockLootType.class, JeiBlockLoot::new));

        entityCategoryList.addAll(LootCategories.ENTITY_LOOT_CATEGORIES.entrySet().stream()
                .map((e) -> createCategory(guiHelper, e, EntityLootType.class, JeiEntityLoot::new))
                .collect(Collectors.toSet()));
        entityCategoryList.add(createCategory(guiHelper, LootCategories.ENTITY_LOOT, EntityLootType.class, JeiEntityLoot::new));

        gameplayCategoryList.addAll(LootCategories.GAMEPLAY_LOOT_CATEGORIES.entrySet().stream()
                .map((e) -> createCategory(guiHelper, e, GameplayLootType.class, JeiGameplayLoot::new))
                .collect(Collectors.toSet()));
        gameplayCategoryList.add(createCategory(guiHelper, LootCategories.GAMEPLAY_LOOT, GameplayLootType.class, JeiGameplayLoot::new));

        blockCategoryList.forEach(registration::addRecipeCategories);
        entityCategoryList.forEach(registration::addRecipeCategories);
        gameplayCategoryList.forEach(registration::addRecipeCategories);
    }

    @Override
    public void registerRecipes(@NotNull IRecipeRegistration registration) {
        NetworkUtils.Client client = AdvancedLootInfoMod.INFO_PROPAGATOR.client();
        ClientLevel level = Minecraft.getInstance().level;

        if (client != null && level != null) {
            Map<ResourceLocation, LootTableEntry> map = new HashMap<>(client.lootEntries.stream().collect(Collectors.toMap((l) -> l.location, l -> l.value)));
            Map<RecipeType<BlockLootType>, List<BlockLootType>> blockRecipeTypes = new HashMap<>();
            Map<RecipeType<EntityLootType>, List<EntityLootType>> entityRecipeTypes = new HashMap<>();
            Map<RecipeType<GameplayLootType>, List<GameplayLootType>> gameplayRecipeTypes = new HashMap<>();

            for (Block block : ForgeRegistries.BLOCKS) {
                ResourceLocation location = block.getLootTable();
                LootTableEntry lootEntry = map.get(location);

                if (lootEntry != null) {
                    RecipeType<BlockLootType> recipeType = null;

                    for (JeiBlockLoot recipeCategory : blockCategoryList) {
                        if (recipeCategory.getLootCategory().validate(block)) {
                            recipeType = recipeCategory.getRecipeType();
                            break;
                        }
                    }

                    if (recipeType != null) {
                        blockRecipeTypes.computeIfAbsent(recipeType, (p) -> new LinkedList<>()).add(new BlockLootType(block, lootEntry));
                    }

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

                for (Entity entity : entityList) {
                    if (entity instanceof Mob mob) {
                        ResourceLocation location = mob.getLootTable();
                        LootTableEntry lootEntry = map.get(location);

                        if (lootEntry != null && entityType.create(level) != null) {
                            RecipeType<EntityLootType> recipeType = null;

                            for (JeiEntityLoot recipeCategory : entityCategoryList) {
                                if (recipeCategory.getLootCategory().validate(entity)) {
                                    recipeType = recipeCategory.getRecipeType();
                                    break;
                                }
                            }

                            if (recipeType != null) {
                                entityRecipeTypes.computeIfAbsent(recipeType, (p) -> new LinkedList<>()).add(new EntityLootType(entity, lootEntry));
                            }

                            map.remove(location);
                        }
                    }
                }
            }

            for (Map.Entry<ResourceLocation, LootTableEntry> entry : map.entrySet()) {
                ResourceLocation location = entry.getKey();
                RecipeType<GameplayLootType> recipeType = null;

                for (JeiGameplayLoot recipeCategory : gameplayCategoryList) {
                    if (recipeCategory.getLootCategory().validate(location.getPath())) {
                        recipeType = recipeCategory.getRecipeType();
                        break;
                    }
                }

                if (recipeType != null) {
                    gameplayRecipeTypes.computeIfAbsent(recipeType, (p) -> new LinkedList<>()).add(new GameplayLootType(entry.getValue(), "/" + location.getPath()));
                }
            }

            for (Map.Entry<RecipeType<BlockLootType>, List<BlockLootType>> entry : blockRecipeTypes.entrySet()) {
                registration.addRecipes(entry.getKey(), entry.getValue());
            }

            for (Map.Entry<RecipeType<EntityLootType>, List<EntityLootType>> entry : entityRecipeTypes.entrySet()) {
                registration.addRecipes(entry.getKey(), entry.getValue());
            }

            for (Map.Entry<RecipeType<GameplayLootType>, List<GameplayLootType>> entry : gameplayRecipeTypes.entrySet()) {
                registration.addRecipes(entry.getKey(), entry.getValue());
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

    private static <T, U, V> T createCategory(IGuiHelper guiHelper, LootCategory<U> e, Class<V> clazz, LootConstructor<T, U, V> constructor) {
        RecipeType<V> recipeType = RecipeType.create(Utils.MOD_ID, e.getKey(), clazz);
        Component title = Component.translatable("emi.category." + Utils.MOD_ID + "." + e.getKey().replace('/', '.'));
        return constructor.construct(guiHelper, recipeType, e, title, guiHelper.createDrawableItemStack(e.getIcon()));
    }

    private static <T, U, V> T createCategory(IGuiHelper guiHelper, Map.Entry<ResourceLocation, LootCategory<U>> e, Class<V> clazz, LootConstructor<T, U, V> constructor) {
        ResourceLocation id = e.getKey();
        RecipeType<V> recipeType = RecipeType.create(id.getNamespace(), id.getPath(), clazz);
        Component title = Component.translatable("emi.category." + id.getNamespace() + "." + id.getPath().replace('/', '.'));
        return constructor.construct(guiHelper, recipeType, e.getValue(), title, guiHelper.createDrawableItemStack(e.getValue().getIcon()));
    }

    @FunctionalInterface
    private interface LootConstructor<T, U, V> {
        T construct(IGuiHelper guiHelper, RecipeType<V> recipeType, LootCategory<U> lootCategory, Component title, IDrawable icon);
    }
}
