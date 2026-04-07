package com.yanny.ali.lootjs.server;

import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.api.ITooltipNode;
import com.yanny.ali.plugin.common.tooltip.BranchTooltipNode;
import com.yanny.ali.plugin.common.tooltip.LiteralTooltipNode;
import com.yanny.ali.lootjs.modifier.CustomPlayerFunction;
import com.yanny.ali.lootjs.modifier.ModifiedItemFunction;
import org.jetbrains.annotations.NotNull;

public class LootJsFunctionTooltipUtils {
    @NotNull
    public static ITooltipNode customPlayerTooltip(IServerUtils utils, CustomPlayerFunction function) {
        return BranchTooltipNode.branch()
                .add(LiteralTooltipNode.translatable("ali.property.value.detail_not_available"))
                .build("ali.type.function.custom_player");
    }

    @NotNull
    public static ITooltipNode modifiedItemTooltip(IServerUtils utils, ModifiedItemFunction function) {
        return LiteralTooltipNode.translatable("ali.type.function.modified_item"); //FIXME
    }
}
