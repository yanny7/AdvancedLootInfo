package com.yanny.ali.mixin;

import com.almostreliable.lootjs.loot.condition.MatchStructure;
import net.minecraft.core.HolderSet;
import net.minecraft.world.level.levelgen.structure.Structure;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MatchStructure.class)
public interface MixinMatchStructure {
    @Accessor
    HolderSet<Structure> getStructures();

    @Accessor
    boolean getExact();
}
