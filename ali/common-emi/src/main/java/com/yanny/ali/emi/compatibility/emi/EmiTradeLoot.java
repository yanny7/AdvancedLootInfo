package com.yanny.ali.emi.compatibility.emi;

import com.yanny.ali.api.*;
import com.yanny.ali.compatibility.common.AbstractScrollWidget;
import com.yanny.ali.compatibility.common.GenericUtils;
import com.yanny.ali.plugin.client.widget.trades.TradeWidget;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import oshi.util.tuples.Triplet;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;

public class EmiTradeLoot extends EmiBaseLoot {
    private final String path;
    private final Set<Block> pois;
    private final Set<Item> accepts;

    public EmiTradeLoot(EmiRecipeCategory category, ResourceLocation id, @Nullable VillagerProfession profession, IDataNode trade, List<ItemStack> inputs, List<ItemStack> outputs) {
        this(category, id, GenericUtils.getJobSites(profession), GenericUtils.getRequestedItems(profession), trade, inputs, outputs);
    }

    public EmiTradeLoot(EmiRecipeCategory category, ResourceLocation id, Set<Block> pois, Set<Item> accepts, IDataNode trade, List<ItemStack> inputs, List<ItemStack> outputs) {
        super(category, id, trade, 0, (pois.isEmpty() ? 10 : 20) + (accepts.isEmpty() ? 0 : 20), inputs, outputs);
        this.path = id.getPath();
        this.pois = pois;
        this.inputs.addAll(pois.stream().map(EmiStack::of).toList());
        this.inputs.addAll(accepts.stream().map(EmiStack::of).toList());
        this.accepts = accepts;
    }

    @Override
    public int getDisplayHeight() {
        return (pois.isEmpty() ? 10 : 20) + (accepts.isEmpty() ? 0 : 20) + getItemsHeight();
    }

    @Override
    protected List<Widget> getAdditionalWidgets(WidgetHolder widgetHolder) {
        Triplet<Component, Component, Rect> title = GenericUtils.prepareTraderTitle(path, CATEGORY_WIDTH - AbstractScrollWidget.getScrollbarExtraWidth());
        List<Widget> widgets = new ArrayList<>();
        Rect rect = title.getC();

        widgets.add(new TextWidget(title.getA().getVisualOrderText(), 0, 0, 0, false));
        widgets.add(new TitleWidget(new Bounds(rect.x(), rect.y(), rect.width(), rect.height()), title.getB()));

        if (!pois.isEmpty()) {
            int i = 1;

            for (Block block : pois) {
                widgets.add(new SlotWidget(EmiStack.of(block), CATEGORY_WIDTH - i * 18, 0));
                i++;
            }
        }

        if (!accepts.isEmpty()) {
            int i = 0;
            int yOffset = pois.isEmpty() ? 10 : 20;
            Component t = Component.translatable("ali.util.advanced_loot_info.accepts");
            int width = Minecraft.getInstance().font.width(t);

            widgets.add(new TextWidget(t.getVisualOrderText(), 0, yOffset + 5, 0, false));

            for (Item item : accepts) {
                widgets.add(new SlotWidget(EmiStack.of(item), width + 2 + i * 18, yOffset));
                i++;
            }
        }

        return widgets;
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
