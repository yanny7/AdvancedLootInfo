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
                    b.add(TooltipBuilder.asElement(utils.getValueTooltip(utils, itemValue.item()), ingredient.values.length));
                } else if (value instanceof Ingredient.TagValue tagValue) {
                    b.add(TooltipBuilder.asElement(utils.getValueTooltip(utils, tagValue.tag()).key(Lang.Value.TAG), ingredient.values.length));
                } else {
                    b.add(TooltipBuilder.asElement(TooltipBuilder.error(value.getClass().getSimpleName()), ingredient.values.length));
                }
            }
        });
    }
}
