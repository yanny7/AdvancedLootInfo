package com.yanny.awi.emi.compatibility;

import com.mojang.logging.LogUtils;
import com.yanny.awi.api.IDataNode;
import com.yanny.awi.compatibility.GenericUtils;
import com.yanny.awi.emi.compatibility.emi.EmiBiomeLoot;
import com.yanny.awi.manager.AwiClientRegistry;
import com.yanny.awi.manager.PluginManager;
import com.yanny.awi.plugin.common.nodes.BiomeNode;
import com.yanny.awi.plugin.common.nodes.LevelStemNode;
import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import org.slf4j.Logger;

import java.util.List;
import java.util.Map;

@EmiEntrypoint
public class EmiCompatibility implements EmiPlugin {
    private static final Logger LOGGER = LogUtils.getLogger();

    @Override
    public void register(EmiRegistry emiRegistry) {
        GenericUtils.register(emiRegistry, this::registerData);
    }

    private void registerData(EmiRegistry registry, byte[] fullCompressedData) {
        AwiClientRegistry clientRegistry = PluginManager.getInstance().clientRegistry;
        ClientLevel level = Minecraft.getInstance().level;

        LOGGER.info("Adding worldgen information to EMI");

        if (level != null) {
            Map<ResourceLocation, LevelStemNode> worldgenData = GenericUtils.decompressWorldgenData(clientRegistry, fullCompressedData, level.registryAccess());

            worldgenData.forEach((key, levelNode) -> {
                EmiRecipeCategory category = new RecipeCategory(key);

                registry.addCategory(category);

                for (IDataNode biomeNode : levelNode.nodes()) {
                    List<Block> blocks = GenericUtils.collectBlocks(biomeNode);
                    registry.addRecipe(new EmiBiomeLoot(category, ((BiomeNode) biomeNode).getBiomeId(), biomeNode, blocks));
                }
            });
        }
    }

    private static class RecipeCategory extends EmiRecipeCategory {
        private final Component title;

        public RecipeCategory(ResourceLocation id) {
            super(id, EmiStack.of(Items.GLOBE_BANNER_PATTERN));
            title = GenericUtils.getFormattedCategoryTitle(id);
        }

        @Override
        public Component getName() {
            return title;
        }
    }
}
