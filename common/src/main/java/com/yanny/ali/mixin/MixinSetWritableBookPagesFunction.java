package com.yanny.ali.mixin;

import net.minecraft.network.chat.Component;
import net.minecraft.server.network.Filterable;
import net.minecraft.world.level.storage.loot.functions.ListOperation;
import net.minecraft.world.level.storage.loot.functions.SetWritableBookPagesFunction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(SetWritableBookPagesFunction.class)
public interface MixinSetWritableBookPagesFunction {
    @Accessor
    List<Filterable<Component>> getPages();

    @Accessor
    ListOperation getPageOperation();
}
