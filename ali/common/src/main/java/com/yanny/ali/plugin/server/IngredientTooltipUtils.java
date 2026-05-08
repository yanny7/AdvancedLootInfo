package com.yanny.ali.plugin.server;

import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.ali.api.IServerUtils;
import net.minecraft.core.Holder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.NotNull;

public class IngredientTooltipUtils {
    @NotNull
    public static TooltipNode getIngredientTooltip(IServerUtils utils, Ingredient ingredient) {
        return TooltipBuilder.array((b) -> {
            for (Holder<Item> value : ingredient.values) {
                b.add(utils.getValueTooltip(utils, value).build("ali.property.value.item"));
            }
        }).build("ali.property.branch.items");
    }
}
