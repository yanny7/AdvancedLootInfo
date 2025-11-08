package com.yanny.ali.plugin.server;

import com.yanny.ali.api.IKeyTooltipNode;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.plugin.common.tooltip.BranchTooltipNode;
import com.yanny.ali.plugin.common.tooltip.ErrorTooltipNode;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.NotNull;

public class IngredientTooltipUtils {
    @NotNull
    public static IKeyTooltipNode getIngredientTooltip(IServerUtils utils, Ingredient ingredient) {
        IKeyTooltipNode tooltip = BranchTooltipNode.branch();

        for (Ingredient.Value value : ingredient.values) {
            if (value instanceof Ingredient.ItemValue itemValue) {
                tooltip.add(utils.getValueTooltip(utils, itemValue.item()).key("ali.property.branch.item"));
            } else if (value instanceof Ingredient.TagValue tagValue) {
                tooltip.add(utils.getValueTooltip(utils, tagValue.tag()).key("ali.property.value.tag"));
            } else {
                tooltip.add(ErrorTooltipNode.error(value.getClass().getSimpleName()));
            }
        }

        return tooltip;
    }
}
