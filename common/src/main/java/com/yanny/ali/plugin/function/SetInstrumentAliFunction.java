package com.yanny.ali.plugin.function;

import com.yanny.ali.api.IContext;
import com.yanny.ali.mixin.MixinSetInstrumentFunction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Instrument;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;

public class SetInstrumentAliFunction extends LootConditionalAliFunction {
    public final TagKey<Instrument> options;

    public SetInstrumentAliFunction(IContext context, LootItemFunction function) {
        super(context, function);
        options = ((MixinSetInstrumentFunction) function).getOptions();
    }

    public SetInstrumentAliFunction(IContext context, FriendlyByteBuf buf) {
        super(context, buf);
        options = TagKey.create(BuiltInRegistries.INSTRUMENT.key(), buf.readResourceLocation());
    }

    @Override
    public void encode(IContext context, FriendlyByteBuf buf) {
        super.encode(context, buf);
        buf.writeResourceLocation(options.location());
    }
}
