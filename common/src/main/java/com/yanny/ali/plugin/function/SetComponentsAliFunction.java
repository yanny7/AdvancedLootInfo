package com.yanny.ali.plugin.function;

import com.mojang.serialization.JsonOps;
import com.yanny.ali.api.IContext;
import com.yanny.ali.mixin.MixinSetComponentsFunction;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;

import java.util.LinkedList;
import java.util.List;

public class SetComponentsAliFunction extends LootConditionalAliFunction {
    public final DataComponentPatch components;

    public SetComponentsAliFunction(IContext context, LootItemFunction function) {
        super(context, function);
        components = ((MixinSetComponentsFunction) function).getComponents();
    }

    public SetComponentsAliFunction(IContext context, FriendlyByteBuf buf) {
        super(context, buf);
        components = DataComponentPatch.CODEC.decode(JsonOps.INSTANCE, buf.readJsonWithCodec(ExtraCodecs.JSON)).getOrThrow().getFirst();
    }

    @Override
    public void encode(IContext context, FriendlyByteBuf buf) {
        super.encode(context, buf);
        buf.writeJsonWithCodec(ExtraCodecs.JSON, DataComponentPatch.CODEC.encodeStart(JsonOps.INSTANCE, components).getOrThrow());
    }

    @Override
    public List<Component> getTooltip(int pad) {
        List<Component> components = new LinkedList<>();
/* FIXME
        components.add(pad(pad, translatable("ali.type.function.set_components")));
        components.add(pad(pad + 1, value(this.components.toString())));
*/
        return components;
    }
}
