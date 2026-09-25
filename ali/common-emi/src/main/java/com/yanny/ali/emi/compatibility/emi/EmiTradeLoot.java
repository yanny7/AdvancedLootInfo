package com.yanny.ali.emi.compatibility.emi;

import com.yanny.aci.api.IWidget;
import com.yanny.aci.api.Rect;
import com.yanny.aci.api.RelativeRect;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IWidgetUtils;
import com.yanny.ali.compatibility.common.GenericUtils;
import com.yanny.ali.compatibility.common.TradeLootType;
import com.yanny.ali.compatibility.common.TraderHeader;
import com.yanny.ali.plugin.client.widget.trades.TradeWidget;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.*;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;

public class EmiTradeLoot extends EmiBaseLoot {
    private final TradeLootType type;
    private final TraderHeader header;

    public EmiTradeLoot(EmiRecipeCategory category, TradeLootType type) {
        this(category, type, GenericUtils.prepareTraderHeader(type, CATEGORY_WIDTH));
    }

    private EmiTradeLoot(EmiRecipeCategory category, TradeLootType type, TraderHeader header) {
        super(category, type.id(), type.entry(), 0, header.height(), type.inputs(), type.outputs());
        this.type = type;
        this.header = header;
        this.inputs.addAll(type.pois().stream().map(EmiStack::of).toList());
        this.inputs.addAll(type.accepts().stream().map(EmiStack::of).toList());

        if (header.spawnEgg() != null) {
            catalysts = List.of(EmiStack.of(header.spawnEgg()));
        }
    }

    @Override
    protected int getHeaderHeight() {
        return header.height();
    }

    @Override
    protected int getMinContentHeight() {
        return header.minContentHeight();
    }

    @Override
    protected List<Widget> getAdditionalWidgets(WidgetHolder widgetHolder) {
        List<Widget> widgets = new ArrayList<>();
        Rect titleRect = header.titleRect();

        catalysts.forEach((catalyst) -> widgets.add(new SlotWidget(catalyst, header.spawnEggRect().x(), header.spawnEggRect().y())));
        widgets.add(new TextWidget(header.title().getVisualOrderText(), titleRect.x(), titleRect.y(), 0, false));

        if (header.titleTooltip() != null) {
            widgets.add(new TitleWidget(new Bounds(titleRect.x(), titleRect.y(), titleRect.width(), titleRect.height()), header.titleTooltip()));
        }

        addSlots(widgets, type.pois(), header.pois());

        if (!type.accepts().isEmpty()) {
            widgets.add(new TextWidget(header.acceptsLabel().getVisualOrderText(), header.acceptsLabelRect().x(), header.acceptsLabelRect().y(), 0, false));
            addSlots(widgets, type.accepts(), header.accepts());
        }

        return widgets;
    }

    @Override
    IWidget getRootWidget(IWidgetUtils utils, IDataNode entry, RelativeRect rect, int maxWidth) {
        return new TradeWidget(utils, entry, rect, maxWidth);
    }

    private static void addSlots(List<Widget> widgets, Set<? extends ItemLike> items, List<Rect> rects) {
        int i = 0;

        for (ItemLike item : items) {
            Rect rect = rects.get(i++);
            widgets.add(new SlotWidget(EmiStack.of(item), rect.x(), rect.y()));
        }
    }

    private static class TitleWidget extends TooltipWidget {
        public TitleWidget(Bounds bounds, Component component) {
            super(getTooltipSupplier(bounds, component), bounds.x(), bounds.y(), bounds.width(), bounds.height());
        }

        @NotNull
        private static BiFunction<Integer, Integer, List<ClientTooltipComponent>> getTooltipSupplier(Bounds bounds, Component component) {
            return (mx, my) -> {
                if (bounds.contains(mx, my)) {
                    return List.of(ClientTooltipComponent.create(component.getVisualOrderText()));
                }

                return List.of();
            };
        }
    }
}
