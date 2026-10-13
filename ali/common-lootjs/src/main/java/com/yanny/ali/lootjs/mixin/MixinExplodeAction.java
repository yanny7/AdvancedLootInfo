package com.yanny.ali.lootjs.mixin;

import com.almostreliable.lootjs.loot.modifier.handler.ExplodeAction;
import net.minecraft.world.level.Explosion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ExplodeAction.class)
public interface MixinExplodeAction {
    @Accessor
    float getRadius();

    @Accessor
    boolean getFire();

    @Accessor
    Explosion.BlockInteraction getMode();
}
