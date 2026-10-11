package com.yanny.ali.lootjs.mixin;

import com.almostreliable.lootjs.loot.modifier.handler.DropExperienceAction;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(DropExperienceAction.class)
public interface MixinDropExperienceAction {
    @Accessor
    NumberProvider getAmount();
}
