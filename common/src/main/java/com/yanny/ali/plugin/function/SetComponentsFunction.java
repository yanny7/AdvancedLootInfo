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

import static com.yanny.ali.plugin.TooltipUtils.*;

public class SetComponentsFunction extends LootConditionalFunction {
    public final DataComponentPatch components;

    public SetComponentsFunction(IContext context, LootItemFunction function) {
        super(context, function);
        components = ((MixinSetComponentsFunction) function).getComponents();
    }

    public SetComponentsFunction(IContext context, FriendlyByteBuf buf) {
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

        components.add(pad(pad, translatable("ali.type.function.set_components")));
        components.add(pad(pad + 1, value(this.components.toString())));

        return components;
    }
}
