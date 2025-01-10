package com.yanny.advanced_loot_info.compatibility;

import com.yanny.advanced_loot_info.Utils;
import com.yanny.advanced_loot_info.network.LootGroup;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.IPlantable;

import java.util.List;

public class EmiBlockLoot extends EmiBaseLoot {
    public static final EmiRecipeCategory PLANT_CATEGORY = new EmiRecipeCategory(Utils.modLoc("plant_loot"), EmiStack.of(Items.DIAMOND_HOE));
    public static final EmiRecipeCategory BLOCK_CATEGORY = new EmiRecipeCategory(Utils.modLoc("block_loot"), EmiStack.of(Items.DIAMOND_PICKAXE));

    private final Block block;
    private final boolean isSpecial;

    public EmiBlockLoot(EmiRecipeCategory category, ResourceLocation id, Block block, LootGroup message) {
        super(category, id, message);
        this.block = block;
        isSpecial = block instanceof IPlantable || block.asItem() == Items.AIR;
        inputs = List.of(EmiStack.of(block));
    }

    @Override
    public void addWidgets(WidgetHolder widgetHolder) {
        if (isSpecial) {
            widgetHolder.add(new BlockSlotWidget(block, 4 * 18 - 4, 0));
        } else {
            widgetHolder.addSlot(inputs.get(0), 4 * 18, 0);
        }

        addWidgets(widgetHolder, new int[]{0, isSpecial ? 30 : 22});
    }

    @Override
    public int getDisplayHeight() {
        return (isSpecial ? 30 : 22) + (outputs.size() / 9 + 1) * 18;
    }
}
