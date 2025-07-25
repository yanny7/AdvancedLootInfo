package com.yanny.ali.compatibility.jei;

import com.yanny.ali.api.Rect;
import com.yanny.ali.mixin.MixinVegetationBlock;
import com.yanny.ali.pip.BlockRenderState;
import com.yanny.ali.platform.Services;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotDrawable;
import mezz.jei.api.gui.inputs.RecipeSlotUnderMouse;
import mezz.jei.api.gui.widgets.ISlottedRecipeWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.ScreenPosition;
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
    public void drawWidget(GuiGraphics guiGraphics, double mouseX, double mouseY) {
        Matrix3x2fStack poseStack = guiGraphics.pose();

        poseStack.pushMatrix();
        slotDrawable.draw(guiGraphics);

        if (block.asItem() == Items.AIR) {
            poseStack.translate(rect.x(), rect.y());

            if (isPlant) {
                int x = (int) guiGraphics.pose().m20() - 7;
                int y = (int) guiGraphics.pose().m21() - 8;

                BlockRenderState renderState = BlockRenderState.of(blockState, level, x, y, rect.width() + x, rect.height() + y, 0.75f, null);

                BlockState base;
                BlockState farmland = Blocks.FARMLAND.defaultBlockState();

                if (block instanceof MixinVegetationBlock bushBlock && bushBlock.invokeMayPlaceOn(farmland, level, BlockPos.ZERO)) {
                    base = farmland;
                } else {
                    base = Blocks.GRASS_BLOCK.defaultBlockState();
                }

                BlockRenderState farmlandState = BlockRenderState.of(base, level, x + 3, y + 7, rect.width() + x + 3, rect.height() + y + 7, 0.75f, null);

                Services.getPlatform().renderBlockInGui(guiGraphics, farmlandState);
                Services.getPlatform().renderBlockInGui(guiGraphics, renderState);
            } else {
                int x = (int) guiGraphics.pose().m20() - 4;
                int y = (int) guiGraphics.pose().m21() - 4;

                BlockRenderState renderState = BlockRenderState.of(blockState, level, x, y, rect.width() + x, rect.height() + y, 1, null);
                Services.getPlatform().renderBlockInGui(guiGraphics, renderState);
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
