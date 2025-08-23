package com.yanny.ali.compatibility.emi;

import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IWidget;
import com.yanny.ali.api.IWidgetUtils;
import com.yanny.ali.api.RelativeRect;
import com.yanny.ali.compatibility.common.GenericUtils;
import com.yanny.ali.plugin.client.widget.trades.TradeWidget;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.widget.*;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.BiFunction;

public class EmiTradeLoot extends EmiBaseLoot {
    private final String path;

    public EmiTradeLoot(EmiRecipeCategory category, ResourceLocation id, String path, IDataNode trade, List<ItemStack> inputs, List<ItemStack> outputs) {
        super(category, id, trade, 0, 10, inputs, outputs);
        this.path = path;
    }

    @Override
    public int getDisplayHeight() {
        return 10 + getItemsHeight();
    }

    @Override
    protected List<Widget> getAdditionalWidgets(WidgetHolder widgetHolder) {
        String key = path.equals("empty") ? "entity.minecraft.wandering_trader" : "entity.minecraft.villager." + path;
        String id = path.equals("empty") ? "wandering_trader" : path;
        Component text = GenericUtils.ellipsis(key, id, CATEGORY_WIDTH);
        Component fullText = Component.translatableWithFallback(key, id);
        Bounds bounds = new Bounds(0, 0, CATEGORY_WIDTH, 8);
        return List.of(
                new TextWidget(text.getVisualOrderText(), 0, 0, 0, false),
                new TitleWidget(bounds, fullText)
        );
    }

    @Override
    IWidget getRootWidget(IWidgetUtils utils, IDataNode entry, RelativeRect rect, int maxWidth) {
        return new TradeWidget(utils, entry, rect, maxWidth);
    }

    private static class TitleWidget extends TooltipWidget {
        public TitleWidget(Bounds bounds, Component component) {
            super(getTooltipSupplier(bounds, component), 0, 0, bounds.width(), bounds.height());
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
