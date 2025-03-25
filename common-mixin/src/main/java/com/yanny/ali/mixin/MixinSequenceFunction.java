package com.yanny.ali.mixin;

import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.SequenceFunction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(SequenceFunction.class)
public interface MixinSequenceFunction {
    @Accessor
    List<LootItemFunction> getFunctions();
}
