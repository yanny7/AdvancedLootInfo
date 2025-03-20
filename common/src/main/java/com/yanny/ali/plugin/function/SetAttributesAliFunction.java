package com.yanny.ali.plugin.function;

import com.yanny.ali.api.IContext;
import com.yanny.ali.api.RangeValue;
import com.yanny.ali.mixin.MixinSetAttributesFunction;
import com.yanny.ali.plugin.FunctionTooltipUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;

import javax.annotation.Nullable;
import java.util.*;

public class SetAttributesAliFunction extends LootConditionalAliFunction {
    public final List<Modifier> modifiers;

    public SetAttributesAliFunction(IContext context, LootItemFunction function) {
        super(context, function);
        modifiers = ((MixinSetAttributesFunction) function).getModifiers().stream().map((f) -> {
            MixinSetAttributesFunction.Modifier m = (MixinSetAttributesFunction.Modifier) f;
            return new Modifier(
                    m.getName(),
                    m.getAttribute(),
                    m.getOperation(),
                    context.utils().convertNumber(context, m.getAmount()),
                    m.getId(),
                    m.getSlots()
            );
        }).toList();
    }

    public SetAttributesAliFunction(IContext context, FriendlyByteBuf buf) {
        super(context, buf);
        int count = buf.readInt();

        modifiers = new ArrayList<>(count);

        for (int i = 0; i < count; i++) {
            String name = buf.readUtf();
            Attribute attribute = BuiltInRegistries.ATTRIBUTE.getOptional(buf.readResourceLocation()).orElseThrow();
            AttributeModifier.Operation operation = AttributeModifier.Operation.fromValue(buf.readInt());
            RangeValue amount = new RangeValue(buf);
            UUID id = buf.readOptional(FriendlyByteBuf::readUtf).map(UUID::fromString).orElse(null);

            int slotCount = buf.readInt();

            EquipmentSlot[] slots = new EquipmentSlot[slotCount];

            for (int j = 0; j < slotCount; j++) {
                slots[j] = EquipmentSlot.byName(buf.readUtf());
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
            buf.writeResourceLocation(Objects.requireNonNull(BuiltInRegistries.ATTRIBUTE.getKey(modifier.attribute)));
            buf.writeInt(modifier.operation.toValue());
            modifier.amount.encode(buf);
            buf.writeOptional(Optional.ofNullable(modifier.id != null ? modifier.id.toString() : null), FriendlyByteBuf::writeUtf);
            buf.writeInt(modifier.slots.length);

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
        Attribute attribute,
        AttributeModifier.Operation operation,
        RangeValue amount,
        @Nullable UUID id,
        EquipmentSlot[] slots
    ) {}
}
