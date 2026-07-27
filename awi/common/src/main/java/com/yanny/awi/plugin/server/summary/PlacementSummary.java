package com.yanny.awi.plugin.server.summary;

import com.yanny.aci.api.RangeValue;
import org.jetbrains.annotations.Nullable;

/**
 * The three per-feature values derived from a {@code PlacedFeature}'s placement modifiers:
 * how many per chunk, with what chance, and in which vertical band.
 *
 * @param count         count per chunk (or {@code null} if no count modifier ⇒ effectively 1)
 * @param chancePercent chance the feature runs per chunk attempt, in percent (or {@code null} ⇒ 100%)
 * @param height        vertical span (or {@code null} if no height modifier)
 */
public record PlacementSummary(@Nullable CountSpan count, @Nullable RangeValue chancePercent, @Nullable HeightSpan height) {
}
