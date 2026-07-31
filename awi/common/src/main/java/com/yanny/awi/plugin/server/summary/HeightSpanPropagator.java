package com.yanny.awi.plugin.server.summary;

import com.yanny.awi.api.IServerUtils;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;

/**
 * Computes a {@link HeightSpan} for a concrete {@link HeightProvider} type. Registered per class on the
 * server registry (mirrors the tooltip dispatch tiers) so mods can add propagators for their own providers.
 * Takes a {@link ColumnContext} as a third argument (a plain {@code BiFunction} can't) to resolve anchors.
 */
@FunctionalInterface
public interface HeightSpanPropagator<T extends HeightProvider> {
    HeightSpan apply(IServerUtils utils, T provider, ColumnContext ctx);
}
