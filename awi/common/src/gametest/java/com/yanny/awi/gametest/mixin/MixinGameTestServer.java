package com.yanny.awi.gametest.mixin;

import net.minecraft.gametest.framework.GameTestServer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.presets.WorldPreset;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = GameTestServer.class, remap = false)
public class MixinGameTestServer {
    @Redirect(method = "lambda$create$1", at = @At(value = "FIELD", target = "Lnet/minecraft/world/level/levelgen/presets/WorldPresets;FLAT:Lnet/minecraft/resources/ResourceKey;", opcode = Opcodes.GETSTATIC), require = 1)
    private static ResourceKey<WorldPreset> awi$normalPreset() {
        return WorldPresets.NORMAL;
    }
}
