package com.yanny.advanced_loot_info.network.function;

import com.yanny.advanced_loot_info.mixin.MixinSetAttributesFunction;
import com.yanny.advanced_loot_info.network.RangeValue;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static com.yanny.advanced_loot_info.compatibility.EmiUtils.*;

public class SetAttributesFunction extends LootConditionalFunction {
    public final List<Modifier> modifiers;

    public SetAttributesFunction(LootContext lootContext, LootItemFunction function) {
        super(lootContext, function);
        modifiers = ((MixinSetAttributesFunction) function).getModifiers().stream().map((f) -> {
            MixinSetAttributesFunction.Modifier m = (MixinSetAttributesFunction.Modifier) f;
            return new Modifier(
                    m.getName(),
                    ForgeRegistries.ATTRIBUTES.getKey(m.getAttribute()),
                    m.getOperation().toValue(),
                    RangeValue.of(lootContext, m.getAmount()),
                    m.getId() != null ? m.getId().toString() : null,
                    Arrays.stream(m.getSlots()).map(EquipmentSlot::getName).toList()
            );
        }).toList();
    }

    public SetAttributesFunction(FunctionType type, FriendlyByteBuf buf) {
        super(type, buf);
        int count = buf.readInt();

        modifiers = new ArrayList<>(count);

        for (int i = 0; i < count; i++) {
            String name = buf.readUtf();
            ResourceLocation attribute = buf.readResourceLocation();
            int operation = buf.readInt();
            RangeValue amount = new RangeValue(buf);
            String id = buf.readOptional(FriendlyByteBuf::readUtf).orElse(null);

            int slotCount = buf.readInt();

            List<String> slots = new ArrayList<>(slotCount);

            for (int j = 0; j < slotCount; j++) {
                slots.add(buf.readUtf());
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
    public void encode(FriendlyByteBuf buf) {
        super.encode(buf);
        buf.writeInt(modifiers.size());

        for (Modifier modifier : modifiers) {
            buf.writeUtf(modifier.name);
            buf.writeResourceLocation(modifier.attribute);
            buf.writeInt(modifier.operation);
            modifier.amount.encode(buf);
            buf.writeOptional(Optional.ofNullable(modifier.id), FriendlyByteBuf::writeUtf);
            buf.writeInt(modifier.slots.size());

            for (String slot : modifier.slots) {
                buf.writeUtf(slot);
            }
        }
    }

    @Override
    public List<Component> getTooltip(int pad) {
        List<Component> components = super.getTooltip(pad);

        modifiers.forEach((modifier) -> {
            components.add(pad(pad + 1, translatable("emi.property.function.set_attributes.name", modifier.name())));
            components.add(pad(pad + 2, translatable("emi.property.function.set_attributes.attribute", value(translatable(ForgeRegistries.ATTRIBUTES.getValue(modifier.attribute()).getDescriptionId())))));
            components.add(pad(pad + 2, translatable("emi.property.function.set_attributes.operation", AttributeModifier.Operation.fromValue(modifier.operation()))));
            components.add(pad(pad + 2, translatable("emi.property.function.set_attributes.amount", modifier.amount())));

            if (modifier.id() != null) {
                components.add(pad(pad + 2, translatable("emi.property.function.set_attributes.id", modifier.id())));
            }

            if (!modifier.slots().isEmpty()) {
                components.add(pad(pad + 2, translatable("emi.property.function.set_attributes.slots")));
                modifier.slots().forEach((slot) -> components.add(pad(pad + 3, value(slot))));
            }
        });

        return components;
    }

    public record Modifier(
        String name,
        ResourceLocation attribute,
        int operation,
        RangeValue amount,
        @Nullable String id,
        List<String> slots
    ) {}
}
