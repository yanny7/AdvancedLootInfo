package com.yanny.ali.plugin.function;

import com.mojang.serialization.JsonOps;
import com.yanny.ali.api.IContext;
import com.yanny.ali.mixin.MixinSetAttributesFunction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static com.yanny.ali.plugin.TooltipUtils.*;

public class SetAttributesFunction extends LootConditionalFunction {
    public final List<net.minecraft.world.level.storage.loot.functions.SetAttributesFunction.Modifier> modifiers;

    public SetAttributesFunction(IContext context, LootItemFunction function) {
        super(context, function);
        modifiers = ((MixinSetAttributesFunction) function).getModifiers();
    }

    public SetAttributesFunction(IContext context, FriendlyByteBuf buf) {
        super(context, buf);
        int count = buf.readInt();

        modifiers = new ArrayList<>(count);

        for (int i = 0; i < count; i++) {
            modifiers.add(net.minecraft.world.level.storage.loot.functions.SetAttributesFunction.Modifier.CODEC.decode(JsonOps.INSTANCE, buf.readJsonWithCodec(ExtraCodecs.JSON)).getOrThrow().getFirst());
        }
    }

    @Override
    public void encode(IContext context, FriendlyByteBuf buf) {
        super.encode(context, buf);
        buf.writeInt(modifiers.size());

        for (net.minecraft.world.level.storage.loot.functions.SetAttributesFunction.Modifier modifier : modifiers) {
            buf.writeJsonWithCodec(ExtraCodecs.JSON, net.minecraft.world.level.storage.loot.functions.SetAttributesFunction.Modifier.CODEC.encodeStart(JsonOps.INSTANCE, modifier).getOrThrow());
        }
    }

    @Override
    public List<Component> getTooltip(int pad) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.function.set_attributes")));

        modifiers.forEach((modifier) -> {
            components.add(pad(pad + 1, translatable("ali.property.function.set_attributes.name", modifier.name())));
            components.add(pad(pad + 2, translatable("ali.property.function.set_attributes.attribute", value(translatable(modifier.attribute().value().getDescriptionId())))));
            components.add(pad(pad + 2, translatable("ali.property.function.set_attributes.operation", modifier.operation().getSerializedName())));
            components.add(pad(pad + 2, translatable("ali.property.function.set_attributes.amount", modifier.amount())));

            modifier.id().ifPresent((i) -> components.add(pad(pad + 2, translatable("ali.property.function.set_attributes.id", i))));

            if (!modifier.slots().isEmpty()) {
                components.add(pad(pad + 2, translatable("ali.property.function.set_attributes.slots")));
                modifier.slots().forEach((slot) -> components.add(pad(pad + 3, value(slot.getSerializedName()))));
            }
        });

        return components;
    }
}
