package com.yanny.awi.plugin.server.summary;

import com.yanny.aci.api.NumberExpr;
import com.yanny.awi.api.IServerUtils;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;

@FunctionalInterface
public interface HeightConverter<T extends HeightProvider> {
    NumberExpr apply(IServerUtils utils, T provider, ColumnContext ctx);
}
