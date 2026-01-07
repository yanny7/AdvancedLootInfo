package com.yanny.ali.neoforge.pip;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.yanny.ali.pip.BlockRenderState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.block.model.ItemTransform;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.RenderTypeHelper;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionfc;
import org.joml.Vector3f;

import java.util.List;

public final class BlockPictureInPictureRenderer extends PictureInPictureRenderer<BlockRenderState> {
    private static final float RENDER_SIZE = 24F;
    private static final ItemTransform DEFAULT_TRANSFORM = new ItemTransform(
            new Vector3f(30, 225, 0), new Vector3f(), new Vector3f(0.625F, 0.625F, 0.625F)
    );
    private static final Quaternionfc LIGHT_FIX_ROT = Axis.YP.rotationDegrees(285);
    private static final RandomSource RANDOM = RandomSource.create();

    public BlockPictureInPictureRenderer(MultiBufferSource.BufferSource bufferSource) {
        super(bufferSource);
    }

    @Override
    protected void renderToTexture(BlockRenderState renderState, PoseStack poseStack) {
        float scale = renderState.scale();
        BlockState state = renderState.state();
        Level fakeLevel = renderState.level();

        poseStack.scale(RENDER_SIZE * scale, -RENDER_SIZE * scale, -RENDER_SIZE * scale);
        DEFAULT_TRANSFORM.apply(false, poseStack.last());
        poseStack.translate(.5, .5, .5);
        poseStack.last().normal().rotate(LIGHT_FIX_ROT);
        poseStack.translate(-.5, -.5, -.5);

        BlockRenderDispatcher blockRenderer = Minecraft.getInstance().getBlockRenderer();
        BlockStateModel model = blockRenderer.getBlockModel(state);
        List<BlockModelPart> modelParts = model.collectParts(RANDOM);
        RANDOM.setSeed(state.getSeed(BlockPos.ZERO));
        blockRenderer.getModelRenderer().tesselateBlock(
                fakeLevel,
                modelParts,
                state,
                BlockPos.ZERO,
                poseStack,
                bufferSource.getBuffer(RenderTypeHelper.getEntityRenderType(ChunkSectionLayer.SOLID)),
                false,
                OverlayTexture.NO_OVERLAY
        );
    }

    @Override
    protected float getTranslateY(int height, int guiScale) {
        return height / 2F;
    }

    @NotNull
    @Override
    protected String getTextureLabel() {
        return "block block-in-ui";
    }

    @NotNull
    @Override
    public Class<BlockRenderState> getRenderStateClass() {
        return BlockRenderState.class;
    }
}
