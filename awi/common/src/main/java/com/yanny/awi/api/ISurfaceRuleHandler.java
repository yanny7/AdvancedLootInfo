package com.yanny.awi.api;

import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.NavigableSet;

public interface ISurfaceRuleHandler {
    @NotNull
    List<BlockInfo> expand(JsonObject definition, GhostObservation ghost);

    record GhostObservation(NavigableSet<Integer> absoluteY, BlockInfo.WaterConstraint water, BlockInfo.Placement placement) {}
}
