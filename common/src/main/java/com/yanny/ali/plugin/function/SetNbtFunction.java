package com.yanny.ali.plugin.function;

import com.yanny.ali.api.IContext;
import com.yanny.ali.mixin.MixinSetNbtFunction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;

import java.util.LinkedList;
import java.util.List;

import static com.yanny.ali.plugin.TooltipUtils.*;

public class SetNbtFunction extends LootConditionalFunction {
    public final String tag;

    public SetNbtFunction(IContext context, LootItemFunction function) {
        super(context, function);
        tag = ((MixinSetNbtFunction) function).getTag().toString();
    }

    public SetNbtFunction(IContext context, FriendlyByteBuf buf) {
        super(context, buf);
        tag = buf.readUtf();
    }

    @Override
    public void encode(IContext context, FriendlyByteBuf buf) {
        super.encode(context, buf);
        buf.writeUtf(tag);
    }

    @Override
    public List<Component> getTooltip(int pad) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.function.set_nbt")));
        components.add(pad(pad + 1, value(tag)));

        return components;
    }
}
