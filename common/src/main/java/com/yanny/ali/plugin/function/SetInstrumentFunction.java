package com.yanny.ali.plugin.function;

import com.yanny.ali.api.IContext;
import com.yanny.ali.mixin.MixinSetInstrumentFunction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Instrument;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;

import java.util.LinkedList;
import java.util.List;

import static com.yanny.ali.plugin.TooltipUtils.pad;
import static com.yanny.ali.plugin.TooltipUtils.translatable;

public class SetInstrumentFunction extends LootConditionalFunction {
    public final TagKey<Instrument> options;

    public SetInstrumentFunction(IContext context, LootItemFunction function) {
        super(context, function);
        options = ((MixinSetInstrumentFunction) function).getOptions();
    }

    public SetInstrumentFunction(IContext context, FriendlyByteBuf buf) {
        super(context, buf);
        options = TagKey.create(BuiltInRegistries.INSTRUMENT.key(), buf.readResourceLocation());
    }

    @Override
    public void encode(IContext context, FriendlyByteBuf buf) {
        super.encode(context, buf);
        buf.writeResourceLocation(options.location());
    }

    @Override
    public List<Component> getTooltip(int pad) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.function.set_instrument")));
        components.add(pad(pad + 1, translatable("ali.property.function.set_instrument.options", options.location())));

        return components;
    }
}
