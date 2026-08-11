package com.yanny.ali.jei.compatibility.jei;

import com.yanny.aci.api.Rect;
import com.yanny.ali.compatibility.common.AbstractScrollWidget;
import com.yanny.ali.mixin.MixinVegetationBlock;
import com.yanny.ali.pip.BlockRenderState;
import com.yanny.ali.platform.Services;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotDrawable;
import mezz.jei.api.gui.inputs.RecipeSlotUnderMouse;
import mezz.jei.api.gui.widgets.ISlottedRecipeWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenPosition;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.VegetationBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix3x2fStack;

import java.util.Optional;

public class JeiBlockSlotWidget implements ISlottedRecipeWidget {
    // Block edge lengths and centers, in GUI pixels relative to the widget. The slot uses an output background, which
    // is centered on the 16x16 ingredient position rather than aligned to it - hence 9 and not half the background.
    private static final float BLOCK_SIZE = 18;
    private static final int BLOCK_CENTER_X = 9;
    private static final int BLOCK_CENTER_Y = 9;
    private static final float PLANT_SIZE = 9;
    private static final int PLANT_CENTER_X = 9;
    private static final int PLANT_CENTER_Y = 5;
    /** One block below the plant, as in the pre-1.21.6 renderer: 0.866 x the block size. */
    private static final int GROUND_CENTER_Y = 13;

    private final BlockState blockState;
    private final Block block;
    private final boolean isPlant;
    private final ScreenPosition position;
    private final Rect rect;
    private final ClientLevel level;
    private final IRecipeSlotDrawable slotDrawable;

    public JeiBlockSlotWidget(IRecipeSlotDrawable slotDrawable, Block block, int x, int y) {
        this.slotDrawable = slotDrawable;
        this.block = block;
        blockState = block.defaultBlockState();
        isPlant = block instanceof VegetationBlock;
        position = new ScreenPosition(x, y);
        rect = new Rect(x, y, 24, 24);
        level = Minecraft.getInstance().level;
    }

    @NotNull
    @Override
    public Optional<RecipeSlotUnderMouse> getSlotUnderMouse(double v, double v1) {
        return Optional.empty();
    }

    @NotNull
    @Override
    public ScreenPosition getPosition() {
        return position;
    }

    @Override
    public void drawWidget(GuiGraphicsExtractor guiGraphics, double mouseX, double mouseY) {
        Matrix3x2fStack poseStack = guiGraphics.pose();

        poseStack.translate(1, 1);
        slotDrawable.draw(guiGraphics);
        poseStack.translate(-1, -1);

        poseStack.pushMatrix();

        if (block.asItem() == Items.AIR) {
            poseStack.translate(rect.x(), rect.y());

            int x = (int) poseStack.m20();
            int y = (int) poseStack.m21();
            ScreenRectangle viewport = AbstractScrollWidget.getCurrentViewport();

            if (isPlant) {
                BlockRenderState renderState = BlockRenderState.centered(blockState, level, x + PLANT_CENTER_X, y + PLANT_CENTER_Y, PLANT_SIZE, viewport);

                BlockState base;
                BlockState farmland = Blocks.FARMLAND.defaultBlockState();

                if (block instanceof MixinVegetationBlock bushBlock && bushBlock.invokeMayPlaceOn(farmland, level, BlockPos.ZERO)) {
                    base = farmland;
                } else {
                    base = Blocks.GRASS_BLOCK.defaultBlockState();
                }

                BlockRenderState farmlandState = BlockRenderState.centered(base, level, x + PLANT_CENTER_X, y + GROUND_CENTER_Y, PLANT_SIZE, viewport);

                Services.getClientPlatform().renderBlockInGui(guiGraphics, farmlandState);
                Services.getClientPlatform().renderBlockInGui(guiGraphics, renderState);
            } else {
                BlockRenderState renderState = BlockRenderState.centered(blockState, level, x + BLOCK_CENTER_X, y + BLOCK_CENTER_Y, BLOCK_SIZE, viewport);
                Services.getClientPlatform().renderBlockInGui(guiGraphics, renderState);
            }
        }

        poseStack.popMatrix();
    }

    public void getTooltip(ITooltipBuilder tooltip, double mouseX, double mouseY) {
        if (slotDrawable.isMouseOver(mouseX, mouseY)) {
            tooltip.add(block.getName());
        }
    }
}
