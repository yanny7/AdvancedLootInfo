package com.yanny.awi.plugin.server.summary;

import com.yanny.aci.api.NumberExpr;
import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.awi.api.IServerUtils;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;

import java.util.List;

@FunctionalInterface
public interface HeightConverter<T extends HeightProvider> {
    NumberExpr apply(IServerUtils utils, T provider, ColumnContext ctx, List<TooltipNode> conditions);
}
