package com.yanny.ali.emi.compatibility.emi;

import com.yanny.aci.api.IWidget;
import com.yanny.aci.api.RelativeRect;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IWidgetUtils;
import com.yanny.ali.plugin.client.widget.LootTableWidget;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.SlotWidget;
import dev.emi.emi.api.widget.Widget;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BushBlock;

import java.util.Collections;
import java.util.List;

public class EmiBlockLoot extends EmiBaseLoot {
    private final Block block;
    private final boolean isSpecial;

    public EmiBlockLoot(EmiRecipeCategory category, Block block, IDataNode lootTable, List<ItemStack> outputs) {
        // id is derived from the block, not from its loot table - blocks sharing a table (vanilla's dropsLike, e.g.
        // wall banner -> banner) each get their own recipe and EMI requires recipe ids to be unique
        super(category, getRecipeId(block), lootTable, 0, (block instanceof BushBlock || block.asItem() == Items.AIR) ? 30 : 22, Collections.emptyList(), outputs);
        this.block = block;
        isSpecial = block instanceof BushBlock || block.asItem() == Items.AIR;
        inputs = List.of(EmiStack.of(block));
    }

    @Override
    protected int getHeaderHeight() {
        return isSpecial ? 30 : 22;
    }

    @Override
    protected List<Widget> getAdditionalWidgets(WidgetHolder widgetHolder) {
        if (isSpecial) {
            return List.of(new EmiBlockSlotWidget(block, CATEGORY_WIDTH / 2 - 13, 0));
        } else {
            return List.of(new SlotWidget(inputs.get(0), CATEGORY_WIDTH / 2 - 9, 0));
        }
    }

    @Override
    IWidget getRootWidget(IWidgetUtils utils, IDataNode entry, RelativeRect rect, int maxWidth) {
        return new LootTableWidget(utils, entry, rect, maxWidth);
    }

    private static ResourceLocation getRecipeId(Block block) {
        ResourceLocation location = BuiltInRegistries.BLOCK.getKey(block);

        return new ResourceLocation(location.getNamespace(), "blocks/" + location.getPath());
    }
}
