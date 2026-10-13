package com.yanny.ali.lootjs.modifier;

import net.minecraft.world.level.Explosion;

public class ExplodeFunction extends BaseLootItemFunction {
    public final float radius;
    public final Explosion.BlockInteraction mode;
    public final boolean fire;

    public ExplodeFunction(float radius, Explosion.BlockInteraction mode, boolean fire) {
        this.radius = radius;
        this.mode = mode;
        this.fire = fire;
    }
}
