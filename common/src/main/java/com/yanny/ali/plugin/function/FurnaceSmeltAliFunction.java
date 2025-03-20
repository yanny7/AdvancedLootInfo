package com.yanny.ali.plugin.function;

import com.yanny.ali.api.IContext;
import com.yanny.ali.plugin.FunctionTooltipUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;

import java.util.List;

public class FurnaceSmeltAliFunction extends LootConditionalAliFunction {
    public FurnaceSmeltAliFunction(IContext context, LootItemFunction function) {
        super(context, function);
    }

    public FurnaceSmeltAliFunction(IContext context, FriendlyByteBuf buf) {
        super(context, buf);
    }

    @Override
    public List<Component> getTooltip(int pad) {
        return FunctionTooltipUtils.getFurnaceSmeltTooltip(pad);
    }
}
