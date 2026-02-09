package com.yanny.ali.rei.compatibility.rei;

import com.yanny.ali.api.Rect;
import com.yanny.ali.configuration.LootCategory;
import com.yanny.ali.mixin.MixinVegetationBlock;
import com.yanny.ali.pip.BlockRenderState;
import com.yanny.ali.platform.Services;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.VegetationBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Matrix3x2fStack;
import oshi.util.tuples.Triplet;

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
        this.icon = lootCategory.getIcon().getDefaultInstance();
    }

    @Override
    public List<Widget> setupDisplay(ReiBlockDisplay display, Rectangle bounds) {
        boolean isSpecial = display.getBlock() instanceof VegetationBlock || display.getBlock().asItem() == Items.AIR;
        List<Widget> widgets = new LinkedList<>();
        Triplet<Rectangle, Rectangle, List<Widget>> prepared = prepareWidgets(display, bounds, isSpecial ? OUT_SLOT_SIZE + PADDING : SLOT_SIZE + PADDING);
        Rectangle innerBounds = prepared.getA();
        Rectangle fullBounds = prepared.getB();
        List<Widget> innerWidgets = new LinkedList<>(prepared.getC());

        if (isSpecial) {
            innerWidgets.add(Widgets.createResultSlotBackground(new Point(innerBounds.getCenterX() - ITEM_SIZE / 2, innerBounds.getY() + OUT_SLOT_OFFSET)));
            innerWidgets.add(Widgets.wrapRenderer(new Rectangle(innerBounds.getCenterX() - ITEM_SIZE / 2, innerBounds.getY(), OUT_SLOT_SIZE, OUT_SLOT_SIZE), new BlockSlotRenderer(display.getBlock())));
        } else {
            innerWidgets.add(Widgets.createSlot(new Point(innerBounds.getCenterX() - ITEM_SIZE / 2, innerBounds.getY() + SLOT_OFFSET)).entry(EntryStacks.of(display.getBlock())).markInput());
        }

        fullBounds.move(bounds.getCenterX() - fullBounds.width / 2, bounds.y + PADDING);
        widgets.add(Widgets.createCategoryBase(fullBounds));
        widgets.add(Widgets.withTranslate(
                new ReiScrollWidget(new Rect(0, 0, fullBounds.width - 2 * PADDING, fullBounds.height - 2 * PADDING), innerBounds.height, innerWidgets),
                fullBounds.x + PADDING,
                fullBounds.y + PADDING
        ));
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
        private final ClientLevel level;

        public BlockSlotRenderer(Block block) {
            this.block = block;
            blockState = block.defaultBlockState();
            isPlant = block instanceof VegetationBlock;
            level = Minecraft.getInstance().level;
        }

        @Override
        public void render(GuiGraphics guiGraphics, Rectangle bounds, int mouseX, int mouseY, float delta) {
            Matrix3x2fStack poseStack = guiGraphics.pose();

            poseStack.pushMatrix();
            poseStack.translate(bounds.getX(), bounds.getY());

            if (isPlant) {
                int x = (int) guiGraphics.pose().m20() - 6;
                int y = (int) guiGraphics.pose().m21() - 4;

                BlockRenderState renderState = BlockRenderState.of(blockState, level, x + 3, y, bounds.width + x, bounds.height + y, 0.8f, null);

                BlockState base;
                BlockState farmland = Blocks.FARMLAND.defaultBlockState();

                if (block instanceof MixinVegetationBlock bushBlock && bushBlock.invokeMayPlaceOn(farmland, level, BlockPos.ZERO)) {
                    base = farmland;
                } else {
                    base = Blocks.GRASS_BLOCK.defaultBlockState();
                }

                BlockRenderState farmlandState = BlockRenderState.of(base, level, x, y + 8, bounds.width + x + 3, bounds.height + y + 7, 0.8f, null);

                Services.getPlatform().renderBlockInGui(guiGraphics, farmlandState);
                Services.getPlatform().renderBlockInGui(guiGraphics, renderState);
            } else {
                int x = (int) guiGraphics.pose().m20() - 5;
                int y = (int) guiGraphics.pose().m21();

                BlockRenderState renderState = BlockRenderState.of(blockState, level, x, y, bounds.width + x, bounds.height + y, 1.1F, null);
                Services.getPlatform().renderBlockInGui(guiGraphics, renderState);
            }

            poseStack.popMatrix();
        }
    }
}
