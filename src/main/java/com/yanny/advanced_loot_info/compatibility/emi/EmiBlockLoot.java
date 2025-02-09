package com.yanny.advanced_loot_info.compatibility.emi;

import com.yanny.advanced_loot_info.loot.LootTableEntry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.IPlantable;

import java.util.List;

public class EmiBlockLoot extends EmiBaseLoot {
    private final Block block;
    private final boolean isSpecial;

    public EmiBlockLoot(EmiRecipeCategory category, ResourceLocation id, Block block, LootTableEntry message) {
        super(category, id, message, 0, (block instanceof IPlantable || block.asItem() == Items.AIR) ? 30 : 22);
        this.block = block;
        isSpecial = block instanceof IPlantable || block.asItem() == Items.AIR;
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
