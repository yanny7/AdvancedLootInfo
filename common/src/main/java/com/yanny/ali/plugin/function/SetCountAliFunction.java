package com.yanny.ali.plugin.function;

import com.yanny.ali.api.IContext;
import com.yanny.ali.api.RangeValue;
import com.yanny.ali.mixin.MixinSetItemCountFunction;
import com.yanny.ali.plugin.FunctionTooltipUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;

import java.util.List;

public class SetCountAliFunction extends LootConditionalAliFunction {
    public final RangeValue count;
    public final boolean add;

    public SetCountAliFunction(IContext context, LootItemFunction function) {
        super(context, function);
        count = context.utils().convertNumber(context, ((MixinSetItemCountFunction) function).getValue());
        add = ((MixinSetItemCountFunction) function).getAdd();
    }

    public SetCountAliFunction(IContext context, FriendlyByteBuf buf) {
        super(context, buf);
        count = new RangeValue(buf);
        add = buf.readBoolean();
    }

    @Override
    public void encode(IContext context, FriendlyByteBuf buf) {
        super.encode(context, buf);
        count.encode(buf);
        buf.writeBoolean(add);
    }

    @Override
    public List<Component> getTooltip(int pad) {
        return FunctionTooltipUtils.getSetCountTooltip(pad, count, add);
    }
}
