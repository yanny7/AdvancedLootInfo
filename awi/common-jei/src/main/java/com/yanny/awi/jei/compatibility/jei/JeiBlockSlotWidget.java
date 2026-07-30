package com.yanny.awi.jei.compatibility.jei;

import com.yanny.aci.api.Rect;
import com.yanny.awi.pip.BlockRenderState;
import com.yanny.awi.platform.Services;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotDrawable;
import mezz.jei.api.gui.inputs.RecipeSlotUnderMouse;
import mezz.jei.api.gui.widgets.ISlottedRecipeWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.ScreenPosition;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix3x2fStack;

import java.util.Optional;

public class JeiBlockSlotWidget implements ISlottedRecipeWidget {
    private final BlockState blockState;
    private final Block block;
    private final ScreenPosition position;
    private final Rect rect;
    private final ClientLevel level;
    private final IRecipeSlotDrawable slotDrawable;


    public JeiBlockSlotWidget(IRecipeSlotDrawable slotDrawable, Block block, int x, int y) {
        this.slotDrawable = slotDrawable;
        this.block = block;
        blockState = block.defaultBlockState();
        position = new ScreenPosition(x, y);
        rect = new Rect(x, y, 18, 18);
        level = Minecraft.getInstance().level;
    }

    @NotNull
    @Override
    public Optional<RecipeSlotUnderMouse> getSlotUnderMouse(double mouseX, double mouseY) {
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

        int x = (int) guiGraphics.pose().m20() - 4;
        int y = (int) guiGraphics.pose().m21() - 4;

        BlockRenderState renderState = BlockRenderState.of(blockState, level, x, y, rect.width() + x, rect.height() + y, 1, null);
        Services.getClientPlatform().renderBlockInGui(guiGraphics, renderState);

        poseStack.popMatrix();
    }

    public void getTooltip(ITooltipBuilder tooltip, double mouseX, double mouseY) {
        if (slotDrawable.isMouseOver(mouseX, mouseY)) {
            tooltip.add(Component.translatable(blockState.getBlock().getDescriptionId()));
        }
    }
}
