package com.yanny.ali.mixin;

import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.world.item.component.FireworkExplosion;
import net.minecraft.world.level.storage.loot.functions.SetFireworkExplosionFunction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Optional;

@Mixin(SetFireworkExplosionFunction.class)
public interface MixinSetFireworkExplosionFunction {
    @Accessor
    Optional<FireworkExplosion.Shape> getShape();

    @Accessor
    Optional<IntList> getColors();

    @Accessor
    Optional<IntList> getFadeColors();

    @Accessor
    Optional<Boolean> getTrail();

    @Accessor
    Optional<Boolean> getTwinkle();
}
