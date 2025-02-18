package com.yanny.ali.compatibility.rei;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.yanny.ali.mixin.MixinBushBlock;
import com.yanny.ali.registries.LootCategory;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.LinkedList;
import java.util.List;

public class ReiBlockCategory extends ReiBaseCategory<ReiBlockDisplay, Block> {
    private final CategoryIdentifier<ReiBlockDisplay> identifier;
    private final Component title;
    private final ItemStack icon;

    public ReiBlockCategory(CategoryIdentifier<ReiBlockDisplay> identifier, Component title, LootCategory<Block> lootCategory) {
        super(lootCategory);
        this.identifier = identifier;
        this.title = title;
        this.icon = lootCategory.getIcon();
    }

    @Override
    public List<Widget> setupDisplay(ReiBlockDisplay display, Rectangle bounds) {
        boolean isSpecial = display.getBlock() instanceof BushBlock || display.getBlock().asItem() == Items.AIR;
        List<Widget> widgets = new LinkedList<>();

        widgets.add(Widgets.createCategoryBase(bounds));
        widgets.addAll(getBaseWidget(display, bounds, 0, isSpecial ? 30 : 22));

        if (isSpecial) {
            widgets.add(Widgets.createResultSlotBackground(new Point(bounds.getCenterX() - 8, bounds.getY() + 9)));
            widgets.add(Widgets.wrapRenderer(new Rectangle(bounds.getCenterX() - 12, bounds.getY() + 4, 24, 24), new BlockSlotRenderer(display.getBlock())));
        } else {
            widgets.add(Widgets.createSlot(new Point(bounds.getCenterX() - 8, bounds.getY() + 5))
                    .entry(EntryStacks.of(display.getBlock())).markInput());
        }

        return widgets;
    }

    @Override
    public CategoryIdentifier<ReiBlockDisplay> getCategoryIdentifier() {
        return identifier;
    }

    @Override
    public Component getTitle() {
        return title;
    }

    @Override
    public Renderer getIcon() {
        return EntryStacks.of(icon);
    }

    private static class BlockSlotRenderer implements Renderer {
        private final BlockState blockState;
        private final Block block;
        private final boolean isPlant;

        public BlockSlotRenderer(Block block) {
            this.block = block;
            blockState = block.defaultBlockState();
            isPlant = block instanceof BushBlock;
        }

        @Override
        public void render(GuiGraphics guiGraphics, Rectangle bounds, int mouseX, int mouseY, float delta) {
            BlockRenderDispatcher blockRenderer = Minecraft.getInstance().getBlockRenderer();
            PoseStack poseStack = guiGraphics.pose();

            poseStack.pushPose();
            poseStack.translate(bounds.getX(), bounds.getY(), 100);

            if (isPlant) {
                poseStack.translate(14, 8, 100);
                poseStack.scale(9, -9, 9);
                poseStack.mulPose(Axis.XP.rotationDegrees(30f));
                poseStack.mulPose(Axis.YP.rotationDegrees(225f));
                blockRenderer.renderSingleBlock(blockState, poseStack, guiGraphics.bufferSource(), 15728880, OverlayTexture.NO_OVERLAY);

                BlockState base;
                BlockState farmland = Blocks.FARMLAND.defaultBlockState();

                if (block instanceof MixinBushBlock bushBlock && bushBlock.invokeMayPlaceOn(farmland, Minecraft.getInstance().level, BlockPos.ZERO)) {
                    base = farmland;
                } else {
                    base = Blocks.GRASS_BLOCK.defaultBlockState();
                }

                poseStack.translate(0, -1, 0);
                blockRenderer.renderSingleBlock(base, poseStack, guiGraphics.bufferSource(), 15728880, OverlayTexture.NO_OVERLAY);
            } else {
                poseStack.translate(25.5, 21, 100);
                poseStack.scale(18, -18, 18);
                poseStack.mulPose(Axis.XP.rotationDegrees(30f));
                poseStack.mulPose(Axis.YP.rotationDegrees(225f));
                blockRenderer.renderSingleBlock(blockState, poseStack, guiGraphics.bufferSource(), 15728880, OverlayTexture.NO_OVERLAY);
                poseStack.translate(0, -1, 0);
            }

            poseStack.popPose();
        }
    }
}
