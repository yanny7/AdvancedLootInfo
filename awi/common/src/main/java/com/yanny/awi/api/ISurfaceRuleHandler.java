package com.yanny.awi.api;

import com.google.gson.JsonObject;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.level.levelgen.RandomState;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.NavigableSet;

public interface ISurfaceRuleHandler {
    @NotNull
    List<BlockInfo> expand(JsonObject definition, GhostObservation ghost);

    /** Must be false for a rule that can pass a position on to the next rule, or its ghost shadows every rule after it. */
    default boolean alwaysPlaces() {
        return true;
    }

    record Context(RandomState randomState, HolderLookup.Provider lookup) {}

    record GhostObservation(NavigableSet<Integer> absoluteY, BlockInfo.WaterConstraint water, BlockInfo.Placement placement) {}
}
