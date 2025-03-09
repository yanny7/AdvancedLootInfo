package com.yanny.ali.plugin.function;

import com.yanny.ali.api.IContext;
import com.yanny.ali.mixin.MixinSetContainerContents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;

import java.util.LinkedList;
import java.util.List;

import static com.yanny.ali.plugin.TooltipUtils.pad;
import static com.yanny.ali.plugin.TooltipUtils.translatable;

public class SetContentsFunction extends LootConditionalFunction {
    public final ResourceLocation location;

    public SetContentsFunction(IContext context, LootItemFunction function) {
        super(context, function);
        location = BuiltInRegistries.DATA_COMPONENT_TYPE.getKey(((MixinSetContainerContents) function).getComponent().type());
    }

    public SetContentsFunction(IContext context, FriendlyByteBuf buf) {
        super(context, buf);
        location = buf.readResourceLocation();
    }

    @Override
    public void encode(IContext context, FriendlyByteBuf buf) {
        super.encode(context, buf);
        buf.writeResourceLocation(location);
    }

    @Override
    public List<Component> getTooltip(int pad) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.function.set_contents")));
        components.add(pad(pad + 1, translatable("ali.property.function.set_contents.type", location)));

        return components;
    }
}
