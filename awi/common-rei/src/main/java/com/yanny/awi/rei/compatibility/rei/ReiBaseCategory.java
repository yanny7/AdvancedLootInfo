package com.yanny.awi.rei.compatibility.rei;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.yanny.aci.api.RangeValue;
import com.yanny.aci.api.RelativeRect;
import com.yanny.aci.tooltip.CoreTooltipUtils;
import com.yanny.aci.tooltip.TooltipNodePalette;
import com.yanny.awi.api.IDataNode;
import com.yanny.awi.api.IWidgetUtils;
import com.yanny.awi.compatibility.AbstractScrollWidget;
import com.yanny.awi.manager.PluginManager;
import com.yanny.awi.plugin.client.ClientUtils;
import com.yanny.awi.plugin.client.widget.BiomeWidget;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2fStack;
import org.joml.Vector3f;
import oshi.util.tuples.Triplet;

import java.util.LinkedList;
import java.util.List;

public abstract class ReiBaseCategory<T extends ReiBaseDisplay> implements DisplayCategory<T> {
    static final int CATEGORY_WIDTH = 9 * 18;
    static final int CATEGORY_HEIGHT = 8 * 18;
    static final int PADDING = 4;

    public ReiBaseCategory() {
    }

    @Override
    public abstract List<Widget> setupDisplay(T display, Rectangle bounds);

    @Override
    public int getDisplayWidth(T display) {
        return CATEGORY_WIDTH;
    }

    @Override
    public int getDisplayHeight() {
        return CATEGORY_HEIGHT;
    }

    protected Triplet<Rectangle, Rectangle, List<Widget>> prepareWidgets(T display, Rectangle bounds, int offset) {
        WidgetHolder holder = getBaseWidget(display, new Rectangle(0, 0, bounds.width, bounds.height), offset);
        Rectangle innerBounds = new Rectangle(0, 0, bounds.width, holder.bounds().getHeight() + offset);
        int height = Math.min(innerBounds.height + 2 * PADDING, bounds.height - 2 * PADDING);
        Rectangle fullBounds = new Rectangle(0, 0, innerBounds.width + 3 * PADDING + AbstractScrollWidget.getScrollbarExtraWidth(), height);

        return new Triplet<>(innerBounds, fullBounds, holder.widgets);
    }

    protected WidgetHolder getBaseWidget(T display, Rectangle bounds, int y) {
        List<Holder> slotWidgets = new LinkedList<>();
        List<Widget> widgets = new LinkedList<>();
        RelativeRect rect = new RelativeRect(0, y, CATEGORY_WIDTH, 0);
        BiomeWidget widget = new BiomeWidget(getUtils(slotWidgets), display.getLootData(), rect, CATEGORY_WIDTH);
        ReiWidgetWrapper widgetWrapper = new ReiWidgetWrapper(widget);

        widgets.add(Widgets.createTooltip(widgetWrapper::getTooltip));
        widgets.add(widgetWrapper);
        slotWidgets.forEach((h) -> {
            int slotX = h.rect.getX() + bounds.getX();
            int slotY = h.rect.getY() + bounds.getY();

            if (h.block.asItem() == Items.AIR && h.block.defaultBlockState().getFluidState().isEmpty()) {
                Rectangle slotRect = new Rectangle(slotX, slotY, 18, 18);

                widgets.add(Widgets.createSlotBase(slotRect));
                widgets.add(Widgets.wrapRenderer(slotRect, new BlockSlotRenderer(h.block)));
                widgets.add(Widgets.createTooltip(slotRect, Component.translatable(h.block.getDescriptionId())));
            } else {
                EntryStack<?> stack;

                if (h.block.defaultBlockState().getFluidState().isEmpty()) {
                    stack = EntryStacks.of(h.block);
                } else {
                    stack = EntryStacks.of(h.block.defaultBlockState().getFluidState().getType());
                }

                stack.tooltip(CoreTooltipUtils.toComponents(h.entry.getTooltip(), 0, Minecraft.getInstance().options.advancedItemTooltips));
                widgets.add(Widgets.createSlot(new Point(slotX + 1, slotY + 1)).entry(stack).markOutput());
            }

            widgets.add(Widgets.wrapRenderer(new Rectangle(slotX, slotY, 18, 18), new SlotCountRenderer(new RangeValue(1))));
        });
        return new WidgetHolder(widgets, widget.getRect());
    }

    @NotNull
    private IWidgetUtils getUtils(List<Holder> widgets) {
        return new ClientUtils() {
            @Nullable
            @Override
            public String getTranslationKey(int index) {
                return null;
            }

            @NotNull
            @Override
            public TooltipNodePalette getTooltipCache() {
                return PluginManager.getInstance().clientRegistry.getTooltipCache();
            }

            @Override
            public void addSlotWidget(Block block, IDataNode entry, RelativeRect rect) {
                widgets.add(new Holder(block, entry, rect));
            }
        };
    }

    private record Holder(Block block, IDataNode entry, RelativeRect rect) {}

    private static class SlotCountRenderer implements Renderer {
        @Nullable
        private Component count;
        private boolean isRange = false;

        public SlotCountRenderer(RangeValue count) {
            if (count.isRange() || count.min() > 1) {
                this.count = Component.literal(count.toIntString());
                isRange = count.isRange();
            }
        }

        @Override
        public void render(GuiGraphics guiGraphics, Rectangle bounds, int mouseX, int mouseY, float delta) {
            if (count != null) {
                Font font = Minecraft.getInstance().font;
                Matrix3x2fStack stack = guiGraphics.pose();


                stack.pushMatrix();
                stack.translate(bounds.getX(), bounds.getY());

                if (isRange) {
                    stack.translate(17, 13);
                    stack.pushMatrix();
                    stack.scale(0.5f);
                    //draw.fill(-font.width(count) - 2, -2, 2, 10, 255<<24 | 0);
                    guiGraphics.drawString(font, count, -font.width(count), 0, -1, false);
                    stack.popMatrix();
                } else {
                    stack.translate(18, 10);
                    guiGraphics.drawString(font, count, -font.width(count), 0, -1, true);
                }

                stack.popMatrix();
            }
        }
    }

    protected record WidgetHolder(List<Widget> widgets, RelativeRect bounds){}

    private static class BlockSlotRenderer implements Renderer {
        private final BlockState blockState;

        public BlockSlotRenderer(Block block) {
            this.blockState = block.defaultBlockState();
        }

        @Override
        public void render(GuiGraphics guiGraphics, Rectangle bounds, int mouseX, int mouseY, float delta) {
            BlockRenderDispatcher blockRenderer = Minecraft.getInstance().getBlockRenderer();
            PoseStack poseStack = guiGraphics.pose();

            Vector3f light0 = new Vector3f(0.6F, -1.0F, 0.8F).normalize();
            Vector3f light1 = new Vector3f(-0.6F, 1.0F, -0.8F).normalize();
            RenderSystem.setShaderLights(light0, light1);

            poseStack.pushPose();
            poseStack.translate(bounds.getX(), bounds.getY(), 100);
            poseStack.translate(15.5, 13.5, 300);
            poseStack.scale(9, -9, 9);
            poseStack.mulPose(Axis.XP.rotationDegrees(30f));
            poseStack.mulPose(Axis.YP.rotationDegrees(225f));
            guiGraphics.drawSpecial((bufferSource) -> blockRenderer.renderSingleBlock(blockState, poseStack, bufferSource, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY));
            poseStack.popPose();
            Lighting.setupForFlatItems();
        }
    }
}
