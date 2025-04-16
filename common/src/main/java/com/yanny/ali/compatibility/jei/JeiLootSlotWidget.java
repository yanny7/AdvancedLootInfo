package com.yanny.ali.compatibility.jei;

import com.mojang.blaze3d.vertex.PoseStack;
import com.yanny.ali.api.RangeValue;
import com.yanny.ali.api.Rect;
import mezz.jei.api.gui.ingredient.IRecipeSlotDrawable;
import mezz.jei.api.gui.inputs.RecipeSlotUnderMouse;
import mezz.jei.api.gui.widgets.ISlottedRecipeWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.ScreenPosition;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class JeiLootSlotWidget implements ISlottedRecipeWidget {
    private final Rect rect;
    private final IRecipeSlotDrawable slotDrawable;
    @Nullable
    private Component count;
    private boolean isRange = false;

    public JeiLootSlotWidget(LootPoolEntryContainer entry, IRecipeSlotDrawable slotDrawable, int x, int y, Map<Enchantment, Map<Integer, RangeValue>> chance,
                             Map<Enchantment, Map<Integer, RangeValue>> count, List<LootItemFunction> functions,
                             List<LootItemCondition> conditions) {
        this.slotDrawable = slotDrawable;
        rect = new Rect(x, y, 18, 18);
        setCount(count.get(null).get(0));
    }

    @NotNull
    @Override
    public Optional<RecipeSlotUnderMouse> getSlotUnderMouse(double mouseX, double mouseY) {
        if (slotDrawable.isMouseOver(mouseX, mouseY)) {
            return Optional.of(new RecipeSlotUnderMouse(slotDrawable, rect.x(), rect.y()));
        }

        return Optional.empty();
    }

    @NotNull
    @Override
    public ScreenPosition getPosition() {
        return new ScreenPosition(rect.x(), rect.y());
    }

    @Override
    public void drawWidget(GuiGraphics guiGraphics, double mouseX, double mouseY) {
        slotDrawable.draw(guiGraphics);

        if (count != null) {
            Font font = Minecraft.getInstance().font;
            PoseStack stack = guiGraphics.pose();

            stack.pushPose();

            if (isRange) {
                stack.translate(17, 13, 200);
                stack.pushPose();
                stack.scale(0.5f, 0.5f, 0.5f);
                //draw.fill(-font.width(count) - 2, -2, 2, 10, 255<<24 | 0);
                guiGraphics.drawString(font, count, -font.width(count), 0, 16777215, false);
                stack.popPose();
            } else {
                stack.translate(18, 10, 200);
                guiGraphics.drawString(font, count, -font.width(count), 0, 16777215, true);
            }

            stack.popPose();
        }
    }

    private void setCount(RangeValue count) {
        if (count.isRange() || count.min() > 1) {
            this.count = Component.literal(count.toIntString());
            isRange = count.isRange();
        }
    }
}
