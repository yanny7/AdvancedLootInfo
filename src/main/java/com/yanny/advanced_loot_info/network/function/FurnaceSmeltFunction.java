package com.yanny.advanced_loot_info.network.function;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;

import java.util.List;

public class FurnaceSmeltFunction extends LootConditionalFunction {
    public FurnaceSmeltFunction(LootContext lootContext, LootItemFunction function) {
        super(lootContext, function);
    }

    public FurnaceSmeltFunction(FunctionType type, FriendlyByteBuf buf) {
        super(type, buf);
    }

    @Override
    public List<Component> getTooltip(int pad) {
        return List.of();
    }
}
