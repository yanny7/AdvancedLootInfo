package com.yanny.ali.lootjs.server;

import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.language.Lang;
import com.yanny.ali.lootjs.modifier.CustomPlayerFunction;
import com.yanny.ali.lootjs.modifier.ModifiedItemFunction;
import org.jetbrains.annotations.NotNull;

public class LootJsFunctionTooltipUtils {
    @NotNull
    public static TooltipBuilder customPlayerTooltip(IServerUtils ignoredUtils, CustomPlayerFunction ignoredFunction) {
        return TooltipBuilder.array((b) -> b.add(TooltipBuilder.error(Lang.Error.DETAIL_NOT_AVAILABLE.singular())))
                .key(Lang.Functions.CUSTOM_PLAYER);
    }

    @NotNull
    public static TooltipBuilder modifiedItemTooltip(IServerUtils ignoredUtils, ModifiedItemFunction ignoredFunction) {
        return TooltipBuilder.error(Lang.Error.MODIFIED_ITEM.singular());
    }
}
