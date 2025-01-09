package com.yanny.emi_loot_addon.network.function;

import com.yanny.emi_loot_addon.mixin.MixinSetInstrumentFunction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Instrument;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;

import java.util.List;

import static com.yanny.emi_loot_addon.compatibility.EmiUtils.pad;
import static com.yanny.emi_loot_addon.compatibility.EmiUtils.translatable;

public class SetInstrumentFunction extends LootConditionalFunction {
    public final TagKey<Instrument> options;

    public SetInstrumentFunction(LootContext lootContext, LootItemFunction function) {
        super(lootContext, function);
        options = ((MixinSetInstrumentFunction) function).getOptions();
    }

    public SetInstrumentFunction(FunctionType type, FriendlyByteBuf buf) {
        super(type, buf);
        options = TagKey.create(BuiltInRegistries.INSTRUMENT.key(), buf.readResourceLocation());
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        super.encode(buf);
        buf.writeResourceLocation(options.location());
    }

    @Override
    public List<Component> getTooltip(int pad) {
        List<Component> components = super.getTooltip(pad);

        components.add(pad(pad + 1, translatable("emi.property.function.set_instrument.options", options.location())));

        return components;
    }
}
