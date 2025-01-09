package com.yanny.advanced_loot_info.network.function;

import com.yanny.advanced_loot_info.mixin.MixinSetNbtFunction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;

import java.util.List;

import static com.yanny.advanced_loot_info.compatibility.EmiUtils.pad;
import static com.yanny.advanced_loot_info.compatibility.EmiUtils.value;

public class SetNbtFunction extends LootConditionalFunction {
    public final String tag;

    public SetNbtFunction(LootContext lootContext, LootItemFunction function) {
        super(lootContext, function);
        tag = ((MixinSetNbtFunction) function).getTag().toString();
    }

    public SetNbtFunction(FunctionType type, FriendlyByteBuf buf) {
        super(type, buf);
        tag = buf.readUtf();
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        super.encode(buf);
        buf.writeUtf(tag);
    }

    @Override
    public List<Component> getTooltip(int pad) {
        List<Component> components = super.getTooltip(pad);

        components.add(pad(pad + 1, value(tag)));

        return components;
    }
}
