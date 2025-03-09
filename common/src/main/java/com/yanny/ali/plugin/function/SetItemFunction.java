package com.yanny.ali.plugin.function;

import com.yanny.ali.api.IContext;
import com.yanny.ali.mixin.MixinSetItemFunction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;

import java.util.LinkedList;
import java.util.List;

import static com.yanny.ali.plugin.TooltipUtils.*;

public class SetItemFunction extends LootConditionalFunction {
    public final Holder<Item> item;

    public SetItemFunction(IContext context, LootItemFunction function) {
        super(context, function);
        item = ((MixinSetItemFunction) function).getItem();
    }

    public SetItemFunction(IContext context, FriendlyByteBuf buf) {
        super(context, buf);
        item = BuiltInRegistries.ITEM.getHolderOrThrow(buf.readResourceKey(Registries.ITEM));
    }

    @Override
    public void encode(IContext context, FriendlyByteBuf buf) {
        super.encode(context, buf);
        buf.writeResourceKey(item.unwrap().orThrow());
    }

    @Override
    public List<Component> getTooltip(int pad) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.function.set_item")));
        components.add(pad(pad + 1, value(item.value().getDescriptionId())));

        return components;
    }
}
