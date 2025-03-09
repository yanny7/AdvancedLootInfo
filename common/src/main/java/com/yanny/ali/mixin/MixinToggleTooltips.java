package com.yanny.ali.mixin;

import com.mojang.serialization.Codec;
import net.minecraft.world.level.storage.loot.functions.ToggleTooltips;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(ToggleTooltips.class)
public interface MixinToggleTooltips {
    @Accessor
    Map<ToggleTooltips.ComponentToggle<?>, Boolean> getValues();

    @Accessor("TOGGLE_CODEC")
    static Codec<ToggleTooltips.ComponentToggle<?>> getToggleCodec() {
        throw new AssertionError();
    }
}
