package com.yanny.ali.lootjs.mixin;

import com.almostreliable.lootjs.loot.action.LootItemFunctionWrapperAction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LootItemFunctionWrapperAction.class)
public interface MixinLootItemFunctionWrapperAction {
    @Accessor
    LootItemFunction getLootItemFunction();
}
