package com.yanny.ali.plugin.server;

import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.language.Lang;
import net.minecraft.core.Holder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.NotNull;

public class IngredientTooltipUtils {
    @NotNull
    public static TooltipBuilder getIngredientTooltip(IServerUtils utils, Ingredient ingredient) {
        return TooltipBuilder.array((b) -> {
            for (Holder<Item> value : ingredient.values) {
                b.add(utils.getValueTooltip(utils, value).build(Lang.Value.ITEM));
            }
        }).key(Lang.Branch.ITEMS);
    }
}
