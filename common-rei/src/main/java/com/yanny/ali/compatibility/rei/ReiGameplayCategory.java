package com.yanny.ali.compatibility.rei;

import com.yanny.ali.registries.LootCategory;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.LinkedList;
import java.util.List;

public class ReiGameplayCategory extends ReiBaseCategory<ReiGameplayDisplay, String> {
    private final CategoryIdentifier<ReiGameplayDisplay> identifier;
    private final Component title;
    private final ItemStack icon;

    public ReiGameplayCategory(CategoryIdentifier<ReiGameplayDisplay> identifier, Component title, LootCategory<String> lootCategory) {
        super(lootCategory);
        this.identifier = identifier;
        this.title = title;
        this.icon = lootCategory.getIcon();
    }

    @Override
    public List<Widget> setupDisplay(ReiGameplayDisplay display, Rectangle bounds) {
        List<Widget> widgets = new LinkedList<>();
        Component lootName = Component.translatableWithFallback("ali/loot_table/" + display.getId().substring(1), display.getId());

        widgets.add(Widgets.createCategoryBase(bounds));
        widgets.addAll(getBaseWidget(display, bounds, 0, 10));
        widgets.add(Widgets.wrapRenderer(
                new Rectangle(bounds.getX() + 4, bounds.getY() + 4, bounds.getWidth(), 8),
                (graphics, bounds1, mouseX, mouseY, delta) -> {
                    graphics.pose().pushPose();
                    graphics.pose().translate(bounds1.getX(), bounds1.getY(), 0);
                    graphics.drawString(Minecraft.getInstance().font, lootName, 0, 0, 0, false);
                    graphics.pose().popPose();
                }
        ));

        return widgets;
    }

    @Override
    public CategoryIdentifier<ReiGameplayDisplay> getCategoryIdentifier() {
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
