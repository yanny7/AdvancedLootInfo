package com.yanny.ali.lootjs.mixin;

import com.almostreliable.lootjs.core.AbstractLootModification;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractLootModification.class)
public interface MixinAbstractLootModification {
    @Accessor
    String getName();
}
