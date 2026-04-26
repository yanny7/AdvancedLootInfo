package com.yanny.awi.api;

import com.yanny.aci.api.ICoreServerUtils;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import org.jetbrains.annotations.NotNull;

public interface IServerUtils extends ICoreServerUtils {
    @NotNull
    <T extends PlacedFeature> FeatureHolder getPlacedFeature(IServerUtils utils, T entry);
}
