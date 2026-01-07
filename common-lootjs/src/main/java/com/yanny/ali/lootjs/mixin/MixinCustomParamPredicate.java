package com.yanny.ali.lootjs.mixin;

import com.almostreliable.lootjs.loot.condition.CustomParamPredicate;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(CustomParamPredicate.class)
public interface MixinCustomParamPredicate<T> {
    @Accessor
    LootContextParam<T> getParam();
}
