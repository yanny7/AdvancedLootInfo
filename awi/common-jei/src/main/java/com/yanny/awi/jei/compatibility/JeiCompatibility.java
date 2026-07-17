package com.yanny.awi.jei.compatibility;

import com.mojang.logging.LogUtils;
import com.yanny.awi.Utils;
import com.yanny.awi.api.IDataNode;
import com.yanny.awi.compatibility.GenericUtils;
import com.yanny.awi.jei.compatibility.jei.JeiBiomeLoot;
import com.yanny.awi.jei.compatibility.jei.RecipeHolder;
import com.yanny.awi.manager.AwiClientRegistry;
import com.yanny.awi.manager.PluginManager;
import com.yanny.awi.plugin.common.nodes.BiomeNode;
import com.yanny.awi.plugin.common.nodes.LevelStemNode;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JeiPlugin
public class JeiCompatibility implements IModPlugin {
    private static final Logger LOGGER = LogUtils.getLogger();

    private static final Map<IRecipeCategory<RecipeHolder>, List<RecipeHolder>> dimensions = new HashMap<>();

    @Override
    public void onRuntimeUnavailable() {
        dimensions.clear();
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        GenericUtils.register(registration, this::registerData);
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        dimensions.forEach((category, recipes) -> registration.addRecipes(category.getRecipeType(), recipes));
    }

    private void registerData(IRecipeCategoryRegistration registration, byte[] fullCompressedData) {
        AwiClientRegistry clientRegistry = PluginManager.getInstance().clientRegistry;
        ClientLevel level = Minecraft.getInstance().level;
        IGuiHelper guiHelper = registration.getJeiHelpers().getGuiHelper();

        LOGGER.info("Adding worldgen information to JEI");

        if (level != null) {
            Map<Identifier, LevelStemNode> worldgenData = GenericUtils.decompressWorldgenData(clientRegistry, fullCompressedData, level.registryAccess());

            worldgenData.forEach((key, levelNode) -> {
                RecipeType<RecipeHolder> type = new RecipeType<>(key, RecipeHolder.class);
                Component title = GenericUtils.getFormattedCategoryTitle(key);
                IRecipeCategory<RecipeHolder> category = new JeiBiomeLoot(guiHelper, type, title, guiHelper.createDrawableItemLike(Items.GLOBE_BANNER_PATTERN));
                List<RecipeHolder> recipes = new ArrayList<>();

                registration.addRecipeCategories(category);

                for (IDataNode biomeNode : levelNode.nodes()) {
                    List<Block> blocks = GenericUtils.collectBlocks(biomeNode);
                    recipes.add(new RecipeHolder(biomeNode, ((BiomeNode) biomeNode).getBiomeId(), blocks));
                }

                dimensions.put(category, recipes);
            });
        }
    }

    @NotNull
    @Override
    public Identifier getPluginUid() {
        return Utils.modLoc("jei_plugin");
    }
}
