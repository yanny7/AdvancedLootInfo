package com.yanny.awi.emi.compatibility.emi;

import com.yanny.aci.api.IWidget;
import com.yanny.aci.api.RelativeRect;
import com.yanny.awi.api.IDataNode;
import com.yanny.awi.api.IWidgetUtils;
import com.yanny.awi.plugin.client.widget.BiomeWidget;
import com.yanny.awi.plugin.common.nodes.BiomeNode;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.widget.TextWidget;
import dev.emi.emi.api.widget.Widget;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;

import java.util.Collections;
import java.util.List;

public class EmiBiomeLoot extends EmiBaseLoot {
    private final Identifier biome;

    public EmiBiomeLoot(EmiRecipeCategory category, Identifier id, IDataNode biomeNode, List<Block> outputs) {
        super(category, id, biomeNode, 0, 10, Collections.emptyList(), outputs);
        biome = ((BiomeNode) biomeNode).getBiomeId();
    }

    @Override
    public int getDisplayHeight() {
        return 10 + getItemsHeight();
    }

    @Override
    protected List<Widget> getAdditionalWidgets(WidgetHolder widgetHolder) {
        return List.of(
                new TextWidget(Component.translatable("biome." + biome.getNamespace() + "." + biome.getPath()).getVisualOrderText(), 0, 0, 0, false)
        );
    }

    @Override
    IWidget getRootWidget(IWidgetUtils utils, IDataNode entry, RelativeRect rect, int maxWidth) {
        return new BiomeWidget(utils, entry, rect, maxWidth);
    }
}
