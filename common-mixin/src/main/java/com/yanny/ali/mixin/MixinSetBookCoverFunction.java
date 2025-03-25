package com.yanny.ali.mixin;

import net.minecraft.server.network.Filterable;
import net.minecraft.world.level.storage.loot.functions.SetBookCoverFunction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Optional;

@Mixin(SetBookCoverFunction.class)
public interface MixinSetBookCoverFunction {
    @Accessor
    Optional<String> getAuthor();

    @Accessor
    Optional<Filterable<String>> getTitle();

    @Accessor
    Optional<Integer> getGeneration();
}
