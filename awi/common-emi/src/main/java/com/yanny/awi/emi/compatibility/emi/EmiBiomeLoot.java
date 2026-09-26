package com.yanny.awi.emi.compatibility.emi;

import com.yanny.aci.api.IWidget;
import com.yanny.aci.api.Rect;
import com.yanny.aci.api.RelativeRect;
import com.yanny.awi.api.IDataNode;
import com.yanny.awi.api.IWidgetUtils;
import com.yanny.awi.compatibility.GenericUtils;
import com.yanny.awi.plugin.client.widget.BiomeWidget;
import com.yanny.awi.plugin.common.nodes.BiomeNode;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.api.widget.TextWidget;
import dev.emi.emi.api.widget.Widget;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class EmiBiomeLoot extends EmiBaseLoot {
    private final ResourceLocation biome;
    private final IDataNode biomeNode;

    public EmiBiomeLoot(EmiRecipeCategory category, ResourceLocation id, IDataNode biomeNode, List<Block> outputs) {
        super(category, id, biomeNode, 0, 10, Collections.emptyList(), outputs);
        biome = ((BiomeNode) biomeNode).getBiomeId();
        this.biomeNode = biomeNode;
    }

    @Override
    protected int getHeaderHeight() {
        return 10;
    }

    @Override
    protected List<Widget> getAdditionalWidgets(WidgetHolder widgetHolder) {
        List<Widget> widgets = new LinkedList<>();

        widgets.add(new TextWidget(Component.translatable("biome." + biome.getNamespace() + "." + biome.getPath()).getVisualOrderText(), 0, 0, 0, false));

        if (GenericUtils.hasSpawnInfo(biomeNode)) {
            Rect rect = GenericUtils.getSpawnInfoRect(CATEGORY_WIDTH);

            widgets.add(new Widget() {
                final Bounds bounds = new Bounds(rect.x(), rect.y(), rect.width(), rect.height());

                @Override
                public Bounds getBounds() {
                    return bounds;
                }

                @Override
                public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
                    GenericUtils.renderSpawnInfoIcon(biomeNode, rect, guiGraphics);
                }

                @Override
                public List<ClientTooltipComponent> getTooltip(int mouseX, int mouseY) {
                    return GenericUtils.getSpawnTooltip(biomeNode).stream().map((c) -> ClientTooltipComponent.create(c.getVisualOrderText())).toList();
                }
            });
        }

        return widgets;
    }

    @Override
    IWidget getRootWidget(IWidgetUtils utils, IDataNode entry, RelativeRect rect, int maxWidth) {
        return new BiomeWidget(utils, entry, rect, maxWidth);
    }
}
