package com.yanny.ali.compatibility.emi;

import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.SlotWidget;
import dev.emi.emi.api.widget.Widget;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.storage.loot.LootTable;

import java.util.List;

public class EmiBlockLoot extends EmiBaseLoot {
    private final Block block;
    private final boolean isSpecial;

    public EmiBlockLoot(EmiRecipeCategory category, ResourceLocation id, Block block, LootTable lootTable, List<Item> items) {
        super(category, id, lootTable, 0, (block instanceof BushBlock || block.asItem() == Items.AIR) ? 30 : 22, items);
        this.block = block;
        isSpecial = block instanceof BushBlock || block.asItem() == Items.AIR;
        inputs = List.of(EmiStack.of(block));
    }

    @Override
    public int getDisplayHeight() {
        return (isSpecial ? 30 : 22) + getItemsHeight();
    }

    @Override
    protected List<Widget> getAdditionalWidgets(WidgetHolder widgetHolder) {
        if (isSpecial) {
            return List.of(new EmiBlockSlotWidget(block, CATEGORY_WIDTH / 2 - 13, 0));
        } else {
            return List.of(new SlotWidget(inputs.get(0), CATEGORY_WIDTH / 2 - 9, 0));
        }
    }
}
