package com.yanny.ali.compatibility.jei;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.yanny.ali.compatibility.common.BlockLootType;
import com.yanny.ali.mixin.MixinBushBlock;
import com.yanny.ali.plugin.client.WidgetUtils;
import com.yanny.ali.registries.LootCategory;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class JeiBlockLoot extends JeiBaseLoot<BlockLootType, Block> {
    private final ClientLevel level;

    public JeiBlockLoot(IGuiHelper guiHelper, RecipeType<BlockLootType> recipeType, LootCategory<Block> lootCategory, Component title, IDrawable icon) {
        super(guiHelper, recipeType, lootCategory, title, icon);
        level = Minecraft.getInstance().level;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, BlockLootType recipe, IFocusGroup iFocusGroup) {
        super.setRecipe(builder, recipe, iFocusGroup);

        boolean isSpecial = recipe.block() instanceof BushBlock || recipe.block().asItem() == Items.AIR;

        if (!isSpecial) {
            builder.addSlot(RecipeIngredientRole.INPUT, 4 * 18 + 1, 1)
                    .addItemStack(recipe.block().asItem().getDefaultInstance());
        }
    }

    @NotNull
    @Override
    public IDrawable getBackground() {
        return guiHelper.createBlankDrawable(getWidth(), getHeight());
    }

    @Override
    public void draw(BlockLootType recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        super.draw(recipe, recipeSlotsView, guiGraphics, mouseX, mouseY);
        boolean isSpecial = recipe.block() instanceof BushBlock || recipe.block().asItem() == Items.AIR;
        boolean isPlant = recipe.block() instanceof BushBlock;
        BlockState blockState = recipe.block().defaultBlockState();
        PoseStack poseStack = guiGraphics.pose();

        poseStack.pushPose();

        if (isSpecial) {
            BlockRenderDispatcher blockRenderer = Minecraft.getInstance().getBlockRenderer();

            poseStack.translate(4 * 18, 4, 0);
            WidgetUtils.blitNineSliced(guiGraphics, WidgetUtils.TEXTURE_LOC, -4, -4, 24, 24, 1, 1, 1, 1, 16, 16, 2, 2);

            if (isPlant) {
                poseStack.translate(14, 8, 100);
                poseStack.scale(9, -9, 9);
                poseStack.mulPose(Axis.XP.rotationDegrees(30f));
                poseStack.mulPose(Axis.YP.rotationDegrees(225f));
                blockRenderer.renderSingleBlock(blockState, poseStack, guiGraphics.bufferSource(), 15728880, OverlayTexture.NO_OVERLAY);

                BlockState base;
                BlockState farmland = Blocks.FARMLAND.defaultBlockState();

                if (recipe.block() instanceof MixinBushBlock bushBlock && bushBlock.invokeMayPlaceOn(farmland, level, BlockPos.ZERO)) {
                    base = farmland;
                } else {
                    base = Blocks.GRASS_BLOCK.defaultBlockState();
                }

                poseStack.translate(0, -1, 0);
                blockRenderer.renderSingleBlock(base, poseStack, guiGraphics.bufferSource(), 15728880, OverlayTexture.NO_OVERLAY);
            } else {
                poseStack.translate(21, 16.5, 100);
                poseStack.scale(18, -18, 18);
                poseStack.mulPose(Axis.XP.rotationDegrees(30f));
                poseStack.mulPose(Axis.YP.rotationDegrees(225f));
                blockRenderer.renderSingleBlock(blockState, poseStack, guiGraphics.bufferSource(), 15728880, OverlayTexture.NO_OVERLAY);
                poseStack.translate(0, -1, 0);
            }

        } else {
            poseStack.translate(4 * 18, 0, 0);
            guiHelper.getSlotDrawable().draw(guiGraphics, 0, 0);
        }

        poseStack.popPose();

        if (isSpecial && mouseX >= 4 * 18 - 4 && mouseX < 4 * 18 + 20 && mouseY >= 0 && mouseY < 24) {
            guiGraphics.renderTooltip(Minecraft.getInstance().font, Component.translatable(recipe.block().getDescriptionId()), (int) mouseX, (int) mouseY);
        }
    }

    @Override
    int getYOffset(BlockLootType recipe) {
        boolean isSpecial = recipe.block() instanceof BushBlock || recipe.block().asItem() == Items.AIR;
        return isSpecial ? 30 : 22;
    }
}
