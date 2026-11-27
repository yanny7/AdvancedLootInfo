package com.yanny.awi.emi.compatibility.emi;

import com.yanny.aci.api.Rect;
import com.yanny.aci.api.RelativeRect;
import com.yanny.aci.compatibility.AbstractScrollWidget;
import com.yanny.awi.api.IBlockNode;
import com.yanny.awi.api.IDataNode;
import com.yanny.awi.compatibility.BlockGeneration;
import com.yanny.awi.compatibility.GenericUtils;
import com.yanny.awi.plugin.client.widget.BlockGenerationWidget;
import com.yanny.awi.plugin.client.widget.LabelWidget;
import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.widget.SlotWidget;
import dev.emi.emi.api.widget.TextWidget;
import dev.emi.emi.api.widget.Widget;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.List;

public class EmiBlockGenerationRecipe extends BasicEmiRecipe {
    private static final int HEADER_HEIGHT = 22;
    private static final int SLOT_SIZE = 18;

    private final BlockGeneration.Page page;
    private int contentHeight = -1;

    public EmiBlockGenerationRecipe(EmiRecipeCategory category, BlockGeneration.Page page) {
        super(category, id(page), EmiBaseLoot.CATEGORY_WIDTH + AbstractScrollWidget.getScrollbarExtraWidth(), 0);
        this.page = page;
        page.blocks().forEach((block) -> outputs.add(EmiBaseLoot.toStack(block)));
    }

    @Override
    public void addWidgets(WidgetHolder widgetHolder) {
        Rect rect = new Rect(0, 0, EmiBaseLoot.CATEGORY_WIDTH + AbstractScrollWidget.getScrollbarExtraWidth(), Math.min(getDisplayHeight(), widgetHolder.getHeight()));
        BlockGenerationWidget content = createContent();
        List<Widget> widgets = new ArrayList<>();

        for (int i = 0; i < page.blocks().size(); i++) {
            Block block = page.blocks().get(i);

            if (GenericUtils.rendersAsBlockModel(block)) {
                EmiBlockSlotWidget slot = new EmiBlockSlotWidget(node(block), block, i * SLOT_SIZE, 0);

                slot.recipeContext(this);
                widgets.add(slot);
            } else {
                widgets.add(new SlotWidget(EmiBaseLoot.toStack(block), i * SLOT_SIZE, 0).recipeContext(this));
            }
        }

        widgets.add(new TextWidget(page.blocks().get(0).getName().getVisualOrderText(), page.blocks().size() * SLOT_SIZE + 4, 5, LabelWidget.COLOR, false));
        widgets.add(new EmiWidgetWrapper(content));
        widgetHolder.add(new EmiScrollWidget(rect, EmiBaseLoot.CATEGORY_WIDTH, getContentHeight(), widgets));
    }

    @Override
    public final int getDisplayHeight() {
        return Math.min(getContentHeight(), EmiBaseLoot.getAvailableHeight(getCategory()));
    }

    @Override
    public Recipe<?> getBackingRecipe() {
        return null;
    }

    @Override
    public boolean supportsRecipeTree() {
        return false;
    }

    private int getContentHeight() {
        if (contentHeight < 0) {
            contentHeight = HEADER_HEIGHT + createContent().getRect().getHeight();
        }

        return contentHeight;
    }

    private BlockGenerationWidget createContent() {
        return new BlockGenerationWidget(new RelativeRect(0, HEADER_HEIGHT, 0, 0), EmiBaseLoot.CATEGORY_WIDTH, page.dimensions());
    }

    private IDataNode node(Block block) {
        for (BlockGeneration.Source source : page.dimensions().stream().flatMap((d) -> d.sources().stream()).toList()) {
            for (IBlockNode blockNode : source.blockNodes()) {
                if (blockNode instanceof IDataNode dataNode && blockNode.getBlocks().contains(block)) {
                    return dataNode;
                }
            }
        }

        return page.dimensions().get(0).sources().get(0).node();
    }

    private static ResourceLocation id(BlockGeneration.Page page) {
        ResourceLocation block = BuiltInRegistries.BLOCK.getKey(page.blocks().get(0));

        return new ResourceLocation("awi", "/block_generation/" + block.getNamespace() + "/" + block.getPath());
    }
}
