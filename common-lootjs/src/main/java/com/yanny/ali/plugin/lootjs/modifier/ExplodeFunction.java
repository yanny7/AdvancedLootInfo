package com.yanny.ali.plugin.lootjs.modifier;

import com.almostreliable.lootjs.loot.action.ExplodeAction;
import com.yanny.ali.mixin.MixinExplodeAction;
import net.minecraft.world.level.Explosion;

public class ExplodeFunction extends BaseLootItemFunction {
    public final float radius;
    public final boolean fire;
    public final Explosion.BlockInteraction mode;

    public ExplodeFunction(ExplodeAction action) {
        radius = ((MixinExplodeAction) action).getRadius();
        fire = ((MixinExplodeAction) action).getFire();
        mode = ((MixinExplodeAction) action).getMode();
    }
}
