package com.yanny.ali.lootjs.mixin;

import com.almostreliable.lootjs.loot.action.DropExperienceAction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(DropExperienceAction.class)
public interface MixinDropExperienceAction {
    @Accessor
    int getAmount();
}
