package com.yanny.ali.plugin.function;

import com.yanny.ali.api.IContext;
import com.yanny.ali.api.ILootFunction;
import com.yanny.ali.mixin.MixinSequenceFunction;
import com.yanny.ali.plugin.FunctionTooltipUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;

import java.util.List;

public class SequenceAliFunction implements ILootFunction {
    public final List<ILootFunction> functions;

    public SequenceAliFunction(IContext context, LootItemFunction function) {
        functions = context.utils().convertFunctions(context, ((MixinSequenceFunction) function).getFunctions());
    }

    public SequenceAliFunction(IContext context, FriendlyByteBuf buf) {
        functions = context.utils().decodeFunctions(context, buf);
    }

    @Override
    public void encode(IContext context, FriendlyByteBuf buf) {
        context.utils().encodeFunctions(context, buf, functions);
    }

    @Override
    public List<Component> getTooltip(int pad) {
        return FunctionTooltipUtils.getSequenceTooltip(pad, functions);
    }
}
