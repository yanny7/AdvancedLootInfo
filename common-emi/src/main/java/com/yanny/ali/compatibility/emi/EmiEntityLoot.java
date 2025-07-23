package com.yanny.ali.compatibility.emi;

import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.Rect;
import com.yanny.ali.compatibility.common.GenericUtils;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;

import java.util.LinkedList;
import java.util.List;

public class EmiEntityLoot extends EmiBaseLoot {
    private final Entity entity;

    public EmiEntityLoot(EmiRecipeCategory category, ResourceLocation id, Entity entity, IDataNode lootTable, List<ItemStack> items) {
        super(category, id, lootTable, 0, 48, items);
        this.entity = entity;

        SpawnEggItem spawnEgg = SpawnEggItem.byId(entity.getType());

        if (spawnEgg != null) {
            catalysts = List.of(EmiStack.of(spawnEgg));
        }
    }

    @Override
    public int getDisplayHeight() {
        return 48 + getItemsHeight();
    }

    @Override
    protected List<Widget> getAdditionalWidgets(WidgetHolder widgetHolder) {
        List<Widget> widgets = new LinkedList<>();
        ClientLevel level = Minecraft.getInstance().level;

        if (level != null) {
            int length = Minecraft.getInstance().font.width(entity.getDisplayName());

            widgets.add(new Widget() {
                private static final int WIDGET_SIZE = 36;
                final Bounds bounds = new Bounds((widgetHolder.getWidth() - WIDGET_SIZE) / 2, 10, WIDGET_SIZE, WIDGET_SIZE);
                final Rect rect = new Rect(bounds.x(), bounds.y(), bounds.width(), bounds.height());

                @Override
                public Bounds getBounds() {
                    return bounds;
                }

                @Override
                public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
                    GenericUtils.renderEntity(entity, rect, widgetHolder.getWidth(), guiGraphics, mouseX, mouseY);
                }
            });
            widgets.add(new TextWidget(entity.getDisplayName().getVisualOrderText(), (widgetHolder.getWidth() - length) / 2, 0, 0, false));
        }

        catalysts.forEach((catalyst) -> widgets.add(new SlotWidget(catalyst, 0, 0)));
        return widgets;
    }
}
