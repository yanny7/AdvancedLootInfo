package com.yanny.ali.plugin.server;

import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.language.Lang;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.NotNull;

public class IngredientTooltipUtils {
    @NotNull
    public static TooltipBuilder getIngredientTooltip(IServerUtils utils, Ingredient ingredient) {
        return TooltipBuilder.array((b) -> {
            for (Ingredient.Value value : ingredient.values) {
                if (value instanceof Ingredient.ItemValue itemValue) {
                    b.add(utils.getValueTooltip(utils, itemValue.item()).build(Lang.Branch.ITEMS));
                } else if (value instanceof Ingredient.TagValue tagValue) {
                    b.add(utils.getValueTooltip(utils, tagValue.tag()).build(Lang.Value.TAG));
                } else {
                    b.add(TooltipBuilder.error(value.getClass().getSimpleName()).build());
                }
            }
        }).key(Lang.Branch.ITEMS);
    }
}
