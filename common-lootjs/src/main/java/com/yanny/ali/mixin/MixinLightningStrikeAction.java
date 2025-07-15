package com.yanny.ali.mixin;

import com.almostreliable.lootjs.loot.action.LightningStrikeAction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LightningStrikeAction.class)
public interface MixinLightningStrikeAction {
    @Accessor
    boolean getShouldDamageEntity();
}
