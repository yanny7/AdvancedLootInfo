package com.yanny.awi.emi.compatibility.emi;

import com.yanny.aci.api.IWidget;
import com.yanny.aci.api.RelativeRect;
import com.yanny.awi.api.IDataNode;
import com.yanny.awi.api.IWidgetUtils;
import com.yanny.awi.compatibility.GenericUtils;
import com.yanny.awi.plugin.client.widget.BiomeWidget;
import com.yanny.awi.plugin.common.nodes.BiomeNode;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.SlotWidget;
import dev.emi.emi.api.widget.TextWidget;
import dev.emi.emi.api.widget.Widget;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;

import java.util.LinkedList;
import java.util.List;

public class EmiBiomeLoot extends EmiBaseLoot {
    private static final int TITLE_HEIGHT = 10;

    private final Identifier biome;
    private final IDataNode biomeNode;
    private final int headerHeight;

    public EmiBiomeLoot(EmiRecipeCategory category, Identifier id, IDataNode biomeNode, List<Block> outputs) {
        super(category, id, biomeNode, 0, computeHeaderHeight(biomeNode), GenericUtils.getSpawnEggs(biomeNode), outputs);
        biome = ((BiomeNode) biomeNode).getBiomeId();
        this.biomeNode = biomeNode;
        headerHeight = computeHeaderHeight(biomeNode);
    }

    @Override
    protected int getHeaderHeight() {
        return headerHeight;
    }

    @Override
    protected List<Widget> getAdditionalWidgets(WidgetHolder widgetHolder) {
        List<Widget> widgets = new LinkedList<>();

        widgets.add(new TextWidget(Component.translatable("biome." + biome.getNamespace() + "." + biome.getPath()).getVisualOrderText(), 0, 0, 0, false));

        for (GenericUtils.SpawnSlot slot : GenericUtils.getSpawnSlots(biomeNode, CATEGORY_WIDTH, TITLE_HEIGHT)) {
            if (slot.egg() != null) {
                SlotWidget widget = new SlotWidget(EmiStack.of(slot.egg()), slot.x(), slot.y()) {
                    @Override
                    protected void addSlotTooltip(List<ClientTooltipComponent> list) {
                        slot.getConditions().forEach((c) -> list.add(ClientTooltipComponent.create(c.getVisualOrderText())));
                        super.addSlotTooltip(list);
                    }
                };

                widget.recipeContext(this);
                widgets.add(widget);
            } else {
                widgets.add(new SlotWidget(EmiStack.EMPTY, slot.x(), slot.y()) {
                    @Override
                    public void drawOverlay(GuiGraphics draw, int mouseX, int mouseY, float delta) {
                        GenericUtils.renderUnknownSpawnEgg(draw, slot.x(), slot.y());
                        super.drawOverlay(draw, mouseX, mouseY, delta);
                    }

                    @Override
                    public List<ClientTooltipComponent> getTooltip(int mouseX, int mouseY) {
                        return slot.getTooltip().stream().map((c) -> ClientTooltipComponent.create(c.getVisualOrderText())).toList();
                    }
                });
            }
        }

        return widgets;
    }

    @Override
    IWidget getRootWidget(IWidgetUtils utils, IDataNode entry, RelativeRect rect, int maxWidth) {
        return new BiomeWidget(utils, entry, rect, maxWidth);
    }

    private static int computeHeaderHeight(IDataNode biomeNode) {
        return TITLE_HEIGHT + GenericUtils.getSpawnSlotsHeight(biomeNode, CATEGORY_WIDTH);
    }
}
