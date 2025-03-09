package com.yanny.ali.plugin.function;

import com.yanny.ali.api.IContext;
import com.yanny.ali.api.ILootFunction;
import com.yanny.ali.mixin.MixinModifyContainerContents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;

import java.util.LinkedList;
import java.util.List;

import static com.yanny.ali.plugin.TooltipUtils.pad;
import static com.yanny.ali.plugin.TooltipUtils.translatable;

public class ModifyContentsFunction extends LootConditionalFunction {
    public final ResourceLocation component;
    public final ILootFunction modifier;

    public ModifyContentsFunction(IContext context, LootItemFunction function) {
        super(context, function);
        component = BuiltInRegistries.DATA_COMPONENT_TYPE.getKey(((MixinModifyContainerContents) function).getComponent().type());
        modifier = context.utils().convertFunction(context, ((MixinModifyContainerContents) function).getModifier());
    }

    public ModifyContentsFunction(IContext context, FriendlyByteBuf buf) {
        super(context, buf);
        component = buf.readResourceLocation();
        modifier = context.utils().decodeFunction(context, buf);
    }

    @Override
    public void encode(IContext context, FriendlyByteBuf buf) {
        super.encode(context, buf);
        buf.writeResourceLocation(component);
        context.utils().encodeFunction(context, buf, modifier);
    }

    @Override
    public List<Component> getTooltip(int pad) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.function.modify_contents")));
        components.add(pad(pad + 1, translatable("ali.property.function.modify_contents.component", component)));
        components.add(pad(pad + 1, translatable("ali.property.function.modify_contents.modifier")));
        components.addAll(modifier.getTooltip(pad + 2));

        return components;
    }
}
