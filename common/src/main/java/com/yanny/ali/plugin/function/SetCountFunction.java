package com.yanny.ali.plugin.function;

import com.yanny.ali.api.IContext;
import com.yanny.ali.api.RangeValue;
import com.yanny.ali.mixin.MixinSetItemCountFunction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;

import java.util.LinkedList;
import java.util.List;

import static com.yanny.ali.plugin.TooltipUtils.pad;
import static com.yanny.ali.plugin.TooltipUtils.translatable;

public class SetCountFunction extends LootConditionalFunction {
    public final RangeValue count;
    public final boolean add;

    public SetCountFunction(IContext context, LootItemFunction function) {
        super(context, function);
        count = context.utils().convertNumber(context, ((MixinSetItemCountFunction) function).getValue());
        add = ((MixinSetItemCountFunction) function).getAdd();
    }

    public SetCountFunction(IContext context, FriendlyByteBuf buf) {
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
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.function.set_count")));
        components.add(pad(pad + 1, translatable("ali.property.function.set_count.count", count)));
        components.add(pad(pad + 1, translatable("ali.property.function.set_count.add", add)));

        return components;
    }
}
