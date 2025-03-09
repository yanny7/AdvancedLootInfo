package com.yanny.ali.plugin.function;

import com.mojang.serialization.JsonOps;
import com.yanny.ali.api.IContext;
import com.yanny.ali.mixin.MixinSetCustomDataFunction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;

import java.util.LinkedList;
import java.util.List;

import static com.yanny.ali.plugin.TooltipUtils.*;

public class SetCustomDataFunction extends LootConditionalFunction {
    public final CompoundTag tag;

    public SetCustomDataFunction(IContext context, LootItemFunction function) {
        super(context, function);
        tag = ((MixinSetCustomDataFunction) function).getTag();
    }

    public SetCustomDataFunction(IContext context, FriendlyByteBuf buf) {
        super(context, buf);
        tag = CompoundTag.CODEC.decode(JsonOps.INSTANCE, buf.readJsonWithCodec(ExtraCodecs.JSON)).getOrThrow().getFirst();
    }

    @Override
    public void encode(IContext context, FriendlyByteBuf buf) {
        super.encode(context, buf);
        buf.writeJsonWithCodec(ExtraCodecs.JSON, CompoundTag.CODEC.encodeStart(JsonOps.INSTANCE, tag).getOrThrow());
    }

    @Override
    public List<Component> getTooltip(int pad) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.function.set_custom_data")));
        components.add(pad(pad + 1, value(tag.toString())));

        return components;
    }
}
