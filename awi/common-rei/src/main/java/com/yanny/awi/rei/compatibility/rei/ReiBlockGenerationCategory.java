package com.yanny.awi.rei.compatibility.rei;

import com.yanny.aci.api.Rect;
import com.yanny.aci.api.RelativeRect;
import com.yanny.aci.compatibility.AbstractScrollWidget;
import com.yanny.awi.compatibility.BlockGeneration;
import com.yanny.awi.compatibility.GenericUtils;
import com.yanny.awi.plugin.client.widget.BlockGenerationWidget;
import com.yanny.awi.plugin.client.widget.LabelWidget;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

import java.util.LinkedList;
import java.util.List;

public class ReiBlockGenerationCategory implements DisplayCategory<ReiBlockGenerationDisplay> {
    private static final int HEADER_HEIGHT = 22;
    private static final int SLOT_SIZE = 18;

    private final CategoryIdentifier<ReiBlockGenerationDisplay> identifier;
    private final Component title;
    private final ItemStack icon;

    public ReiBlockGenerationCategory(CategoryIdentifier<ReiBlockGenerationDisplay> identifier, Component title, Item icon) {
        this.identifier = identifier;
        this.title = title;
        this.icon = icon.getDefaultInstance();
    }

    @Override
    public List<Widget> setupDisplay(ReiBlockGenerationDisplay display, Rectangle bounds) {
        BlockGeneration.Page page = display.getPage();
        BlockGenerationWidget content = new BlockGenerationWidget(new RelativeRect(0, HEADER_HEIGHT, 0, 0), ReiBaseCategory.CATEGORY_WIDTH, page.dimensions());
        ReiWidgetWrapper contentWrapper = new ReiWidgetWrapper(content);
        int contentHeight = HEADER_HEIGHT + content.getRect().getHeight();
        int padding = ReiBaseCategory.PADDING;
        int height = Math.min(contentHeight + 2 * padding, bounds.height - 2 * padding);
        Rectangle fullBounds = new Rectangle(0, 0, ReiBaseCategory.CATEGORY_WIDTH + 3 * padding + AbstractScrollWidget.getScrollbarExtraWidth(), height);
        List<Widget> innerWidgets = new LinkedList<>();
        List<Widget> widgets = new LinkedList<>();

        for (int i = 0; i < page.blocks().size(); i++) {
            Block block = page.blocks().get(i);
            int x = i * SLOT_SIZE;

            if (GenericUtils.rendersAsBlockModel(block)) {
                Rectangle slotRect = new Rectangle(x, 0, SLOT_SIZE, SLOT_SIZE);

                innerWidgets.add(Widgets.createSlotBase(slotRect));
                innerWidgets.add(Widgets.wrapRenderer(slotRect, new ReiBaseCategory.BlockSlotRenderer(block)));
                innerWidgets.add(Widgets.createTooltip(slotRect, Component.translatable(block.getDescriptionId())));
            } else {
                innerWidgets.add(Widgets.createSlot(new Point(x + 1, 1)).entry(ReiBaseCategory.toStack(block)).markOutput());
            }
        }

        innerWidgets.add(Widgets.createLabel(new Point(page.blocks().size() * SLOT_SIZE + 4, 5), page.blocks().get(0).getName())
                .leftAligned().noShadow().color(LabelWidget.COLOR));
        innerWidgets.add(Widgets.createTooltip(contentWrapper::getTooltip));
        innerWidgets.add(contentWrapper);

        fullBounds.move(bounds.getCenterX() - fullBounds.width / 2, bounds.y + padding);
        widgets.add(Widgets.createCategoryBase(fullBounds));
        widgets.add(Widgets.withTranslate(
                new ReiScrollWidget(new Rect(0, 0, fullBounds.width - 2 * padding, fullBounds.height - 2 * padding), ReiBaseCategory.CATEGORY_WIDTH, contentHeight, innerWidgets),
                fullBounds.x + padding,
                fullBounds.y + padding
        ));
        return widgets;
    }

    @Override
    public int getDisplayWidth(ReiBlockGenerationDisplay display) {
        return ReiBaseCategory.CATEGORY_WIDTH;
    }

    @Override
    public int getDisplayHeight() {
        return ReiBaseCategory.getAvailableHeight();
    }

    @Override
    public int getFixedDisplaysPerPage() {
        return 1;
    }

    @Override
    public CategoryIdentifier<ReiBlockGenerationDisplay> getCategoryIdentifier() {
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
}
