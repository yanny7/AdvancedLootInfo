package com.yanny.ali.mixin;

import net.minecraft.world.item.component.FireworkExplosion;
import net.minecraft.world.level.storage.loot.functions.ListOperation;
import net.minecraft.world.level.storage.loot.functions.SetFireworksFunction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Optional;

@Mixin(SetFireworksFunction.class)
public interface MixinSetFireworksFunction {
    @Accessor
    Optional<ListOperation.StandAlone<FireworkExplosion>> getExplosions();

    @Accessor
    Optional<Integer> getFlightDuration();
}
