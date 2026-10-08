package com.yanny.ali.emi.compatibility.emi;

import com.mojang.blaze3d.vertex.PoseStack;
import com.yanny.aci.api.NumberInterval;
import com.yanny.aci.compatibility.ScrollableTooltip;
import com.yanny.aci.number.NumberFormatter;
import com.yanny.aci.tooltip.CoreTooltipUtils;
import com.yanny.aci.tooltip.TooltipLine;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IItemNode;
import com.yanny.ali.plugin.client.TooltipUtils;
import com.yanny.ali.plugin.client.WidgetUtils;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.SlotWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class EmiLootSlotWidget extends SlotWidget {
    private static final long CYCLE_MILLIS = 1000;

    private final List<Option> options;
    private final boolean hasPredicates;

    public EmiLootSlotWidget(List<IDataNode> options, int x, int y) {
        super(getIngredient(options.get(0)), x, y);
        this.options = options.stream().map(Option::of).toList();
        this.hasPredicates = options.stream().anyMatch((o) -> ((IItemNode) o).hasPredicates());
    }

    @Override
    public EmiIngredient getStack() {
        return getOption().ingredient;
    }

    @Override
    protected void addSlotTooltip(List<ClientTooltipComponent> list) {
        List<TooltipLine> lines = CoreTooltipUtils.toLines(getOption().entry.getTooltip(), 0, Minecraft.getInstance().options.advancedItemTooltips, TooltipUtils.getStyle(), TooltipUtils.getNumberOptions());

        if (!lines.isEmpty()) {
            list.add(new ScrollableTooltip(lines));
        }

        super.addSlotTooltip(list);
    }

    @Override
    public void drawBackground(GuiGraphics draw, int mouseX, int mouseY, float delta) {
        super.drawBackground(draw, mouseX, mouseY, delta);

        if (hasPredicates) {
            draw.fill(x + 1, y + 1, x + WidgetUtils.SLOT_SIZE - 1, y + WidgetUtils.SLOT_SIZE - 1, WidgetUtils.PREDICATES_COLOR);
        }
    }

    @Override
    public void drawOverlay(GuiGraphics draw, int mouseX, int mouseY, float delta) {
        Option option = getOption();

        if (option.count != null) {
            Font font = Minecraft.getInstance().font;
            PoseStack stack = draw.pose();

            stack.pushPose();

            if (option.isRange) {
                stack.translate(x + 17, y + 13, 200);
                stack.pushPose();
                stack.scale(0.5f, 0.5f, 0.5f);
                draw.drawString(font, option.count, -font.width(option.count), 0, -1, false);
                stack.popPose();
            } else {
                stack.translate(x + 18, y + 10, 200);
                draw.drawString(font, option.count, -font.width(option.count), 0, -1, true);
            }

            stack.popPose();
        }

        super.drawOverlay(draw, mouseX, mouseY, delta);
    }

    private Option getOption() {
        if (options.size() == 1) {
            return options.get(0);
        }

        return options.get((int) (System.currentTimeMillis() / CYCLE_MILLIS % options.size()));
    }

    private static EmiIngredient getIngredient(IDataNode entry) {
        return ((IItemNode) entry).getItem().map(EmiStack::of, EmiIngredient::of);
    }

    private record Option(IDataNode entry, EmiIngredient ingredient, @Nullable Component count, boolean isRange) {
        private static Option of(IDataNode entry) {
            NumberInterval count = TooltipUtils.getSlotCount((IItemNode) entry);

            if (!count.isPoint() || count.lo() > 1) {
                return new Option(entry, getIngredient(entry), Component.literal(NumberFormatter.slot(count)), !count.isPoint());
            }

            return new Option(entry, getIngredient(entry), null, false);
        }
    }
}
