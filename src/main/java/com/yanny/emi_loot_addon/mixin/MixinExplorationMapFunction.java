package com.yanny.emi_loot_addon.mixin;

import net.minecraft.tags.TagKey;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraft.world.level.storage.loot.functions.ExplorationMapFunction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ExplorationMapFunction.class)
public interface MixinExplorationMapFunction {
    @Accessor
    TagKey<Structure> getDestination();

    @Accessor
    MapDecoration.Type getMapDecoration();

    @Accessor
    byte getZoom();

    @Accessor
    int getSearchRadius();

    @Accessor
    boolean getSkipKnownStructures();
}
