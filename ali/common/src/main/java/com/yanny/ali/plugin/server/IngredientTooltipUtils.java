package com.yanny.ali.plugin.server;

import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.ali.api.IServerUtils;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.NotNull;

public class IngredientTooltipUtils {
    @NotNull
    public static TooltipNode getIngredientTooltip(IServerUtils utils, Ingredient ingredient) {
        return TooltipBuilder.array((b) -> {
            for (Ingredient.Value value : ingredient.values) {
                if (value instanceof Ingredient.ItemValue itemValue) {
                    b.add(utils.getValueTooltip(utils, itemValue.item()).build("ali.property.branch.item"));
                } else if (value instanceof Ingredient.TagValue tagValue) {
                    b.add(utils.getValueTooltip(utils, tagValue.tag()).build("ali.property.value.tag"));
                } else {
                    b.add(TooltipBuilder.error(value.getClass().getSimpleName()).build());
                }
            }
        }).build("ali.property.branch.items");
    }
}
