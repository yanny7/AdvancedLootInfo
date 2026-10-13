package com.yanny.ali.lootjs.mixin;

import com.almostreliable.lootjs.loot.modifier.handler.LightningStrikeAction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LightningStrikeAction.class)
public interface MixinLightningStrikeAction {
    @Accessor
    boolean getShouldDamageEntity();
}
