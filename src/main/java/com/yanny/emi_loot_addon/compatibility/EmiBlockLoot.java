package com.yanny.emi_loot_addon.compatibility;

import com.yanny.emi_loot_addon.Utils;
import com.yanny.emi_loot_addon.network.LootGroup;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@ParametersAreNonnullByDefault
public class EmiBlockLoot extends EmiBaseLoot {
    public static final EmiRecipeCategory CATEGORY = new EmiRecipeCategory(Utils.modLoc("block_loot"), EmiStack.of(Items.DIAMOND_PICKAXE));

    public EmiBlockLoot(ResourceLocation id, Block block, LootGroup message) {
        super(CATEGORY, id, message);

        inputs = List.of(EmiStack.of(block.asItem()));
    }

    @Override
    public void addWidgets(WidgetHolder widgetHolder) {
        widgetHolder.addSlot(inputs.get(0), 4 * 18, 0);
        addWidgets(widgetHolder, new int[]{0, 22});
    }
}
