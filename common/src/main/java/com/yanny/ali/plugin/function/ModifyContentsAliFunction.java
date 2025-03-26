package com.yanny.ali.plugin.function;

import com.mojang.serialization.JsonOps;
import com.yanny.ali.api.IContext;
import com.yanny.ali.api.ILootFunction;
import com.yanny.ali.mixin.MixinModifyContainerContents;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.storage.loot.ContainerComponentManipulator;
import net.minecraft.world.level.storage.loot.ContainerComponentManipulators;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;

import java.util.LinkedList;
import java.util.List;

public class ModifyContentsAliFunction extends LootConditionalAliFunction {
    public final ContainerComponentManipulator<?> component;
    public final ILootFunction modifier;

    public ModifyContentsAliFunction(IContext context, LootItemFunction function) {
        super(context, function);
        component = ((MixinModifyContainerContents) function).getComponent();
        modifier = context.utils().convertFunction(context, ((MixinModifyContainerContents) function).getModifier());
    }

    public ModifyContentsAliFunction(IContext context, FriendlyByteBuf buf) {
        super(context, buf);
        component = ContainerComponentManipulators.CODEC.decode(JsonOps.INSTANCE, buf.readJsonWithCodec(ExtraCodecs.JSON)).getOrThrow().getFirst();
        modifier = context.utils().decodeFunction(context, buf);
    }

    @Override
    public void encode(IContext context, FriendlyByteBuf buf) {
        super.encode(context, buf);
        buf.writeJsonWithCodec(ExtraCodecs.JSON, ContainerComponentManipulators.CODEC.encodeStart(JsonOps.INSTANCE, component).getOrThrow());
        context.utils().encodeFunction(context, buf, modifier);
    }

    @Override
    public List<Component> getTooltip(int pad) {
        List<Component> components = new LinkedList<>();
/* FIXME
        components.add(pad(pad, translatable("ali.type.function.modify_contents")));
        components.add(pad(pad + 1, translatable("ali.property.function.modify_contents.component", component)));
        components.add(pad(pad + 1, translatable("ali.property.function.modify_contents.modifier")));
        components.addAll(modifier.getTooltip(pad + 2));
*/
        return components;
    }
}
