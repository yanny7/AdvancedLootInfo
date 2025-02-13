package com.yanny.advanced_loot_info.compatibility.emi;

import com.yanny.advanced_loot_info.api.Rect;
import com.yanny.advanced_loot_info.compatibility.common.GenericUtils;
import com.yanny.advanced_loot_info.loot.LootTableEntry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.api.widget.Widget;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraftforge.common.ForgeSpawnEggItem;

import java.util.List;

public class EmiEntityLoot extends EmiBaseLoot {
    private final Entity entity;

    public EmiEntityLoot(EmiRecipeCategory category, ResourceLocation id, Entity entity, LootTableEntry message) {
        super(category, id, message, 0, 48);
        this.entity = entity;

        SpawnEggItem spawnEgg = ForgeSpawnEggItem.fromEntityType(entity.getType());

        if (spawnEgg != null) {
            catalysts = List.of(EmiStack.of(spawnEgg));
        }
    }

    @Override
    public void addWidgets(WidgetHolder widgetHolder) {
        ClientLevel level = Minecraft.getInstance().level;

        if (level != null) {
            int length = Minecraft.getInstance().font.width(entity.getDisplayName());

            widgetHolder.add(new Widget() {
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
            widgetHolder.addText(entity.getDisplayName(), (widgetHolder.getWidth() - length) / 2, 0, 0, false);
            super.addWidgets(widgetHolder);
        }

        catalysts.forEach((catalyst) -> widgetHolder.addSlot(catalyst, 0, 0));
    }

    @Override
    public int getDisplayHeight() {
        return 48 + getItemsHeight();
    }
}
