package com.yanny.ali.pip;

import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.state.pip.PictureInPictureRenderState;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public record BlockRenderState(BlockState state, Level level, int x0, int y0, int x1, int y1, float scale, @Nullable ScreenRectangle scissorArea,
                               @Nullable ScreenRectangle bounds) implements PictureInPictureRenderState {
    @NotNull
    public static BlockRenderState of(BlockState state, Level fakeLevel, int x0, int y0, int x1, int y1, float scale, @Nullable ScreenRectangle scissorArea) {
        ScreenRectangle bounds = PictureInPictureRenderState.getBounds(x0, y0, x1, y1, scissorArea);
        return new BlockRenderState(state, fakeLevel, x0, y0, x1, y1, scale, scissorArea, bounds);
    }
}
