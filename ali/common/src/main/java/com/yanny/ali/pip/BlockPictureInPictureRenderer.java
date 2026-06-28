package com.yanny.ali.pip;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.model.BlockDisplayContext;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.cuboid.ItemTransform;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionfc;
import org.joml.Vector3f;

public final class BlockPictureInPictureRenderer extends PictureInPictureRenderer<BlockRenderState> {
    private static final float RENDER_SIZE = 24F;
    private static final ItemTransform DEFAULT_TRANSFORM = new ItemTransform(
            new Vector3f(30, 225, 0), new Vector3f(), new Vector3f(0.625F, 0.625F, 0.625F)
    );
    private static final Quaternionfc LIGHT_FIX_ROT = Axis.YP.rotationDegrees(285);

    private FeatureRenderDispatcher featureRenderDispatcher = null;

    public BlockPictureInPictureRenderer() {
        super();
    }

    @Override
    protected void renderToTexture(BlockRenderState renderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector) {
        if (featureRenderDispatcher == null) {
            featureRenderDispatcher = Minecraft.getInstance().gameRenderer.featureRenderDispatcher();
        }

        float scale = renderState.scale();
        BlockState state = renderState.state();
        SubmitNodeStorage collector = new SubmitNodeStorage();

        poseStack.scale(RENDER_SIZE * scale, -RENDER_SIZE * scale, -RENDER_SIZE * scale);
        DEFAULT_TRANSFORM.apply(false, poseStack.last());
        poseStack.translate(.5, .5, .5);
        poseStack.last().normal().rotate(LIGHT_FIX_ROT);
        poseStack.translate(-.5, -.5, -.5);

        BlockDisplayContext context = BlockDisplayContext.create();
        BlockModelRenderState s = new BlockModelRenderState();
        Minecraft.getInstance().getEntityRenderDispatcher().blockModelResolver.update(s, state, context);
        s.submit(poseStack, collector, LightCoordsUtil.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, 0);
        featureRenderDispatcher.renderAllFeatures(collector);
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
