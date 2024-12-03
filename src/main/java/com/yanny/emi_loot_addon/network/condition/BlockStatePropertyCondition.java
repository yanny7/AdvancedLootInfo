package com.yanny.emi_loot_addon.network.condition;

import com.yanny.emi_loot_addon.mixin.MixinLootItemBlockStatePropertyCondition;
import com.yanny.emi_loot_addon.mixin.MixinStatePropertiesPredicate;
import com.yanny.emi_loot_addon.network.LootCondition;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public class BlockStatePropertyCondition extends LootCondition {
    public final ResourceLocation block;
    public final List<Property> properties;

    public BlockStatePropertyCondition(LootContext lootContext, LootItemCondition condition) {
        super(ConditionType.of(condition.getType()));
        block = ForgeRegistries.BLOCKS.getKey(((MixinLootItemBlockStatePropertyCondition) condition).getBlock());
        properties = ((MixinStatePropertiesPredicate)((MixinLootItemBlockStatePropertyCondition) condition).getProperties()).getProperties().stream()
                .map(Property::of).collect(Collectors.toList());
    }

    public BlockStatePropertyCondition(ConditionType type, FriendlyByteBuf buf) {
        super(type);
        block = buf.readResourceLocation();
        properties = new LinkedList<>();

        int count = buf.readInt();

        for (int i = 0; i < count; i++) {
            properties.add(Property.decode(buf));
        }
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeResourceLocation(block);
        buf.writeInt(properties.size());
        properties.forEach((p) -> {
            buf.writeEnum(p.type);
            p.encode(buf);
        });
    }

    public enum PropertyType {
        EXACT,
        RANGE,
        UNKNOWN
        ;

        public static PropertyType of(StatePropertiesPredicate.PropertyMatcher matcher) {
            if (matcher instanceof StatePropertiesPredicate.ExactPropertyMatcher) {
                return PropertyType.EXACT;
            } else if (matcher instanceof StatePropertiesPredicate.RangedPropertyMatcher) {
                return PropertyType.RANGE;
            } else {
                return UNKNOWN;
            }
        }
    }

    private static final Map<PropertyType, BiFunction<PropertyType, StatePropertiesPredicate.PropertyMatcher, Property>> PROPERTY_MAP = new HashMap<>();
    private static final Map<PropertyType, BiFunction<PropertyType, FriendlyByteBuf, Property>> PROPERTY_DECODE_MAP = new HashMap<>();

    static {
        PROPERTY_MAP.put(PropertyType.EXACT, ExactProperty::new);
        PROPERTY_MAP.put(PropertyType.RANGE, RangeProperty::new);
        PROPERTY_MAP.put(PropertyType.UNKNOWN, UnknownProperty::new);

        PROPERTY_DECODE_MAP.put(PropertyType.EXACT, ExactProperty::new);
        PROPERTY_DECODE_MAP.put(PropertyType.RANGE, RangeProperty::new);
        PROPERTY_DECODE_MAP.put(PropertyType.UNKNOWN, UnknownProperty::new);
    }

    public abstract static class Property {
        public PropertyType type;

        public Property(PropertyType type) {
            this.type = type;
        }

        public abstract void encode(FriendlyByteBuf buf);

        public static Property of(StatePropertiesPredicate.PropertyMatcher property) {
            PropertyType type = PropertyType.of(property);
            return PROPERTY_MAP.get(type).apply(type, property);
        }

        public static Property decode(FriendlyByteBuf buf) {
            PropertyType type = buf.readEnum(PropertyType.class);
            return PROPERTY_DECODE_MAP.get(type).apply(type, buf);
        }

        public static void encode(FriendlyByteBuf buf, Property property) {
            buf.writeEnum(property.type);
            property.encode(buf);
        }
    }

    public static class ExactProperty extends Property {
        public ExactProperty(PropertyType type, StatePropertiesPredicate.PropertyMatcher property) {
            super(type);
        }

        public ExactProperty(PropertyType type, FriendlyByteBuf buf) {
            super(type);
        }

        @Override
        public void encode(FriendlyByteBuf buf) {

        }
    }

    public static class RangeProperty extends Property {
        public RangeProperty(PropertyType type, StatePropertiesPredicate.PropertyMatcher property) {
            super(type);
        }

        public RangeProperty(PropertyType type, FriendlyByteBuf buf) {
            super(type);
        }

        @Override
        public void encode(FriendlyByteBuf buf) {

        }
    }

    public static class UnknownProperty extends Property {
        public UnknownProperty(PropertyType type, StatePropertiesPredicate.PropertyMatcher property) {
            super(type);
        }

        public UnknownProperty(PropertyType type, FriendlyByteBuf buf) {
            super(type);
        }

        @Override
        public void encode(FriendlyByteBuf buf) {

        }
    }
}
