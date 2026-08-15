package com.yanny.ali.rei.compatibility.rei;

import com.yanny.aci.api.Rect;
import com.yanny.aci.compatibility.AbstractScrollWidget;
import com.yanny.ali.configuration.LootCategory;
import com.yanny.ali.mixin.MixinVegetationBlock;
import com.yanny.ali.pip.BlockRenderState;
import com.yanny.ali.platform.Services;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.compat.GuiGraphics;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.navigation.ScreenRectangle;
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
    // Block edge lengths and centers, in GUI pixels relative to the slot - kept at the values the pre-1.21.6 renderer
    // produced, where the block was placed by its corner rather than by its center.
    private static final float BLOCK_SIZE = 18;
    private static final int BLOCK_CENTER_X = 8;
    private static final int BLOCK_CENTER_Y = 13;
    private static final float PLANT_SIZE = 9;
    private static final int PLANT_CENTER_X = 8;
    private static final int PLANT_CENTER_Y = 8;
    private static final int GROUND_CENTER_Y = 16;

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

            poseStack.popMatrix();
        }
    }
}
