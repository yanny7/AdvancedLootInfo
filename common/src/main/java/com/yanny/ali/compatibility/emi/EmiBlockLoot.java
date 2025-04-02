package com.yanny.ali.compatibility.emi;

import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.storage.loot.LootTable;

import java.util.List;

public class EmiBlockLoot extends EmiBaseLoot {
    private final Block block;
    private final boolean isSpecial;

    public EmiBlockLoot(EmiRecipeCategory category, ResourceLocation id, Block block, LootTable lootTable) {
        super(category, id, lootTable, 0, (block instanceof BushBlock || block.asItem() == Items.AIR) ? 30 : 22);
        this.block = block;
        isSpecial = block instanceof BushBlock || block.asItem() == Items.AIR;
        inputs = List.of(EmiStack.of(block));
    }

    @Override
    public void addWidgets(WidgetHolder widgetHolder) {
        if (isSpecial) {
            widgetHolder.add(new EmiBlockSlotWidget(block, 4 * 18 - 4, 0));
        } else {
            widgetHolder.addSlot(inputs.get(0), 4 * 18, 0);
        }

        super.addWidgets(widgetHolder);
    }

    @Override
    public int getDisplayHeight() {
        return (isSpecial ? 30 : 22) + getItemsHeight();
    }
}
