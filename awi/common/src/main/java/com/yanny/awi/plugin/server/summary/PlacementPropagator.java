package com.yanny.awi.plugin.server.summary;

import com.yanny.awi.api.IServerUtils;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;

/**
 * Computes a {@link PlacementContribution} for a concrete {@link PlacementModifier} type. This is the main
 * extensibility point: a modded count/chance/height placement modifier registers one of these to feed the
 * summary. Delegates to the height / int span registries where it needs to resolve a sub-provider.
 */
@FunctionalInterface
public interface PlacementPropagator<T extends PlacementModifier> {
    PlacementContribution apply(IServerUtils utils, T modifier, ColumnContext ctx);
}
