package com.yanny.ali.lootjs.server;

import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.lootjs.modifier.CustomPlayerFunction;
import com.yanny.ali.lootjs.modifier.ModifiedItemFunction;
import org.jetbrains.annotations.NotNull;

public class LootJsFunctionTooltipUtils {
    @NotNull
    public static TooltipNode customPlayerTooltip(IServerUtils utils, CustomPlayerFunction function) {
        return TooltipBuilder.array((b) -> b
                        .add(TooltipBuilder.error("ali.property.value.detail_not_available"))
                )
                .build("ali.type.function.player_action");
    }

    @NotNull
    public static TooltipNode modifiedItemTooltip(IServerUtils utils, ModifiedItemFunction function) {
        return TooltipBuilder.error("ali.type.function.modified_item").build(); //FIXME
    }
}
