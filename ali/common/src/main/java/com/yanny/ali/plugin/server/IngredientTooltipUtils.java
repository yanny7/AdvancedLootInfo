package com.yanny.ali.plugin.server;

import com.yanny.ali.api.IKeyTooltipNode;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.api.ITooltipNode;
import com.yanny.ali.plugin.common.tooltip.BranchTooltipNode;
import net.minecraft.core.Holder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.NotNull;

public class IngredientTooltipUtils {
    @NotNull
    public static ITooltipNode getIngredientTooltip(IServerUtils utils, Ingredient ingredient) {
        IKeyTooltipNode tooltip = BranchTooltipNode.branch();

        for (Holder<Item> value : ingredient.values) {
            tooltip.add(utils.getValueTooltip(utils, value).build("ali.property.value.item"));
        }

        return tooltip.build("ali.property.branch.items");
    }
}
