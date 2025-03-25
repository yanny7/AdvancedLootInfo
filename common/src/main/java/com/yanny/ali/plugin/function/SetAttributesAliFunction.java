package com.yanny.ali.plugin.function;

import com.yanny.ali.api.IContext;
import com.yanny.ali.api.RangeValue;
import com.yanny.ali.mixin.MixinSetAttributesFunction;
import com.yanny.ali.plugin.FunctionTooltipUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;

import java.util.*;

public class SetAttributesAliFunction extends LootConditionalAliFunction {
    public final List<Modifier> modifiers;

    public SetAttributesAliFunction(IContext context, LootItemFunction function) {
        super(context, function);
        modifiers = ((MixinSetAttributesFunction) function).getModifiers().stream().map((f) -> {
            return new Modifier(
                    f.name(),
                    f.attribute(),
                    f.operation(),
                    context.utils().convertNumber(context, f.amount()),
                    f.id(),
                    f.slots()
            );
        }).toList();
    }

    public SetAttributesAliFunction(IContext context, FriendlyByteBuf buf) {
        super(context, buf);
        int count = buf.readInt();

        modifiers = new ArrayList<>(count);

        for (int i = 0; i < count; i++) {
            String name = buf.readUtf();
            Holder<Attribute> attribute = BuiltInRegistries.ATTRIBUTE.getHolderOrThrow(buf.readResourceKey(Registries.ATTRIBUTE));
            AttributeModifier.Operation operation = AttributeModifier.Operation.fromValue(buf.readInt());
            RangeValue amount = new RangeValue(buf);
            Optional<UUID> id = buf.readOptional(FriendlyByteBuf::readUtf).map(UUID::fromString);

            int slotCount = buf.readInt();

            List<EquipmentSlot> slots = new LinkedList<>();

            for (int j = 0; j < slotCount; j++) {
                slots.add(EquipmentSlot.byName(buf.readUtf()));
            }

            modifiers.add(new Modifier(
                    name,
                    attribute,
                    operation,
                    amount,
                    id,
                    slots
            ));
        }
    }

    @Override
    public void encode(IContext context, FriendlyByteBuf buf) {
        super.encode(context, buf);
        buf.writeInt(modifiers.size());

        for (Modifier modifier : modifiers) {
            buf.writeUtf(modifier.name);
            buf.writeResourceKey(modifier.attribute.unwrap().orThrow());
            buf.writeInt(modifier.operation.toValue());
            modifier.amount.encode(buf);
            buf.writeOptional(modifier.id.map(UUID::toString), FriendlyByteBuf::writeUtf);
            buf.writeInt(modifier.slots.size());

            for (EquipmentSlot slot : modifier.slots) {
                buf.writeUtf(slot.getName());
            }
        }
    }

    @Override
    public List<Component> getTooltip(int pad) {
        return FunctionTooltipUtils.getSetAttributesTooltip(pad, modifiers);
    }

    public record Modifier(
            String name,
            Holder<Attribute> attribute,
            AttributeModifier.Operation operation,
            RangeValue amount,
            Optional<UUID> id,
            List<EquipmentSlot> slots
    ) {}
}
