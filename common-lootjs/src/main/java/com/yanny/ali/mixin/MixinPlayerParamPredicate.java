package com.yanny.ali.mixin;

import com.almostreliable.lootjs.loot.condition.PlayerParamPredicate;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.function.Predicate;

@Mixin(PlayerParamPredicate.class)
public interface MixinPlayerParamPredicate {
    @Accessor
    Predicate<ServerPlayer> getPredicate();
}
