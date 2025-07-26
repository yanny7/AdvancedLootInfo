package com.yanny.ali.plugin.server;

import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.api.ITooltipNode;
import com.yanny.ali.api.TooltipNode;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.NotNull;

import static com.yanny.ali.plugin.server.GenericTooltipUtils.*;

public class IngredientTooltipUtils {
    @NotNull
    public static ITooltipNode getIngredientTooltip(IServerUtils utils, Ingredient ingredient) {
        ITooltipNode tooltip = new TooltipNode();

        for (Ingredient.Value value : ingredient.values) {
            if (value instanceof Ingredient.ItemValue itemValue) {
                tooltip.add(getItemStackTooltip(utils, "ali.property.branch.items", itemValue.item()));
            } else if (value instanceof Ingredient.TagValue tagValue) {
                tooltip.add(getTagKeyTooltip(utils, "ali.property.value.tag", tagValue.tag()));
            } else {
                tooltip.add(new TooltipNode(translatable("ali.util.advanced_loot_info.missing", value(value.getClass().getSimpleName()))));
            }
        }

        return tooltip;
    }
}
