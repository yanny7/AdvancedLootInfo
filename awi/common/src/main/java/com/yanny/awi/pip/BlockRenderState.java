package com.yanny.awi.pip;

import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.renderer.state.gui.pip.PictureInPictureRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public record BlockRenderState(BlockState state, Level level, int x0, int y0, int x1, int y1, float scale, @Nullable ScreenRectangle scissorArea,
                               @Nullable ScreenRectangle bounds) implements PictureInPictureRenderState {
    /** GUI pixels a one-block edge occupies at {@code scale = 1}: {@link BlockPictureInPictureRenderer}'s render size times the item transform's 0.625 scale. */
    private static final float PIXELS_PER_BLOCK = 15F;
    /** Half the height of a unit cube under the 30/225 GUI rotation, in block units - the widest of the two projected half-extents. */
    private static final float PROJECTED_HALF_EXTENT = 0.787F;

    @NotNull
    public static BlockRenderState of(BlockState state, Level fakeLevel, int x0, int y0, int x1, int y1, float scale, @Nullable ScreenRectangle scissorArea) {
        ScreenRectangle bounds = PictureInPictureRenderState.getBounds(x0, y0, x1, y1, scissorArea);
        return new BlockRenderState(state, fakeLevel, x0, y0, x1, y1, scale, scissorArea, bounds);
    }

    /**
     * Renders the block centered on the given point with an edge length of {@code blockSize} GUI pixels. The render
     * area is derived from the size rather than from the slot, because the block is drawn into a texture of exactly
     * that area - a box smaller than the projected cube crops it.
     */
    @NotNull
    public static BlockRenderState centered(BlockState state, Level fakeLevel, int centerX, int centerY, float blockSize, @Nullable ScreenRectangle scissorArea) {
        int half = Mth.ceil(blockSize * PROJECTED_HALF_EXTENT) + 1;
        return of(state, fakeLevel, centerX - half, centerY - half, centerX + half, centerY + half, blockSize / PIXELS_PER_BLOCK, scissorArea);
    }
}
