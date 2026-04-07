package com.yanny.ali.emi.compatibility.emi;

import com.yanny.ali.api.*;
import com.yanny.ali.compatibility.common.AbstractScrollWidget;
import com.yanny.ali.compatibility.common.GenericUtils;
import com.yanny.ali.plugin.client.widget.LootTableWidget;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.widget.*;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import oshi.util.tuples.Triplet;

import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;

public class EmiGameplayLoot extends EmiBaseLoot {
    private final ResourceLocation location;

    public EmiGameplayLoot(EmiRecipeCategory category, ResourceLocation id, IDataNode lootTable, List<ItemStack> outputs) {
        super(category, id, lootTable, 0, 10, Collections.emptyList(), outputs);
        location = id;
    }

    @Override
    public int getDisplayHeight() {
        return 10 + getItemsHeight();
    }

    @Override
    protected List<Widget> getAdditionalWidgets(WidgetHolder widgetHolder) {
        Triplet<Component, Component, Rect> title = GenericUtils.prepareGameplayTitle(location, widgetHolder.getWidth() - AbstractScrollWidget.getScrollbarExtraWidth());
        Rect rect = title.getC();

        return List.of(
                new TextWidget(title.getA().getVisualOrderText(), 0, 0, 0, false),
                new TitleWidget(new Bounds(rect.x(), rect.y(), rect.width(), rect.height()), title.getB())
        );
    }

    @Override
    IWidget getRootWidget(IWidgetUtils utils, IDataNode entry, RelativeRect rect, int maxWidth) {
        return new LootTableWidget(utils, entry, rect, maxWidth);
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
