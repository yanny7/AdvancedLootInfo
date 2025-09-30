package com.yanny.ali.compatibility.emi;

import com.yanny.ali.api.*;
import com.yanny.ali.compatibility.common.EntityStorage;
import com.yanny.ali.compatibility.common.GenericUtils;
import com.yanny.ali.manager.PluginManager;
import com.yanny.ali.plugin.client.widget.LootTableWidget;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.Level;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class EmiEntityLoot extends EmiBaseLoot {
    private final EntityType<?> entityType;
    private final ResourceLocation variant;

    public EmiEntityLoot(EmiRecipeCategory category, ResourceLocation id, EntityType<?> entityType, IDataNode lootTable, List<ItemStack> outputs) {
        super(category, id, lootTable, 0, 48, Collections.emptyList(), outputs);
        this.entityType = entityType;
        this.variant = id;

        SpawnEggItem spawnEgg = SpawnEggItem.byId(entityType);

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
            int length = Minecraft.getInstance().font.width(entityType.getDescription());

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
                    Level level = Minecraft.getInstance().level;

                    if (level != null) {
                        Entity entity = EntityStorage.getEntity(PluginManager.COMMON_REGISTRY, entityType, level, variant);
                        GenericUtils.renderEntity(entity, rect, widgetHolder.getWidth(), guiGraphics, mouseX, mouseY);
                    }
                }
            });
            widgets.add(new TextWidget(entityType.getDescription().getVisualOrderText(), (widgetHolder.getWidth() - length) / 2, 0, 0, false));
        }

        catalysts.forEach((catalyst) -> widgets.add(new SlotWidget(catalyst, 0, 0)));
        return widgets;
    }

    @Override
    IWidget getRootWidget(IWidgetUtils utils, IDataNode entry, RelativeRect rect, int maxWidth) {
        return new LootTableWidget(utils, entry, rect, maxWidth);
    }
}
