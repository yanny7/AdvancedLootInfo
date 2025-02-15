package com.yanny.ali.mixin;

import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Instrument;
import net.minecraft.world.level.storage.loot.functions.SetInstrumentFunction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SetInstrumentFunction.class)
public interface MixinSetInstrumentFunction {
    @Accessor
    TagKey<Instrument> getOptions();
}
