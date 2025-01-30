package com.yanny.advanced_loot_info.plugin.function;

import com.yanny.advanced_loot_info.api.IContext;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;

import java.util.List;

public class FurnaceSmeltFunction extends LootConditionalFunction {
    public FurnaceSmeltFunction(IContext context, LootItemFunction function) {
        super(context, function);
    }

    public FurnaceSmeltFunction(IContext context, FriendlyByteBuf buf) {
        super(context, buf);
    }

    @Override
    public List<Component> getTooltip(int pad) {
        return List.of();
    }
}
