package com.yanny.ali.compatibility.emi;

import com.yanny.ali.compatibility.common.GenericUtils;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.widget.*;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.storage.loot.LootTable;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.BiFunction;

public class EmiGameplayLoot extends EmiBaseLoot {
    public EmiGameplayLoot(EmiRecipeCategory category, ResourceLocation id, LootTable lootTable, List<Item> items) {
        super(category, id, lootTable, 0, 10, items);
    }

    @Override
    public int getDisplayHeight() {
        return 10 + getItemsHeight();
    }

    @Override
    protected List<Widget> getAdditionalWidgets(WidgetHolder widgetHolder) {
        String key = "ali/loot_table/" + id.getPath().substring(1);
        Component text = GenericUtils.ellipsis(key, id.getPath(), widgetHolder.getWidth() - 10);
        Component fullText = Component.translatableWithFallback(key, id.getPath());
        Bounds bounds = new Bounds(0, 0, widgetHolder.getWidth() - 10, 8);
        return List.of(
                new TextWidget(text.getVisualOrderText(), 0, 0, 0, false),
                new TitleWidget(bounds, fullText)
        );
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
