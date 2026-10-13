package com.yanny.aci.gametest.mixin;

import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(value = MinecraftServer.class, priority = 400)
public class MixinMinecraftServer {
    private static final int SPAWN_RADIUS = 2;

    @ModifyConstant(method = "prepareLevels", constant = @Constant(intValue = 11), require = 0)
    private int aci$spawnRadius(int radius) {
        return SPAWN_RADIUS;
    }

    @ModifyConstant(method = "prepareLevels", constant = @Constant(intValue = 441), require = 0)
    private int aci$spawnChunks(int chunks) {
        return (SPAWN_RADIUS * 2 - 1) * (SPAWN_RADIUS * 2 - 1);
    }
}
