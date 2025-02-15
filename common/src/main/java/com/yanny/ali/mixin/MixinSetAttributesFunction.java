package com.yanny.ali.mixin;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.level.storage.loot.functions.SetAttributesFunction;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

@Mixin(SetAttributesFunction.class)
public interface MixinSetAttributesFunction {
    @Mixin(SetAttributesFunction.Modifier.class)
    interface Modifier {
        @Accessor
        String getName();
        
        @Accessor
        Attribute getAttribute();

        @Accessor
        AttributeModifier.Operation getOperation();

        @Accessor
        NumberProvider getAmount();
        
        @Accessor
        @Nullable
        UUID getId();
        
        @Accessor
        EquipmentSlot[] getSlots();
    }
    
    @Accessor
    List<SetAttributesFunction.Modifier> getModifiers();
}
