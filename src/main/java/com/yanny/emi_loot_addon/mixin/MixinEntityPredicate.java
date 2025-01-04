package com.yanny.emi_loot_addon.mixin;

import net.minecraft.advancements.critereon.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(EntityPredicate.class)
public interface MixinEntityPredicate {
    @Accessor
    EntityTypePredicate getEntityType();

    @Accessor
    DistancePredicate getDistanceToPlayer();

    @Accessor
    LocationPredicate getLocation();

    @Accessor
    LocationPredicate getSteppingOnLocation();

    @Accessor
    MobEffectsPredicate getEffects();

    @Accessor
    NbtPredicate getNbt();

    @Accessor
    EntityFlagsPredicate getFlags();

    @Accessor
    EntityEquipmentPredicate getEquipment();

    @Accessor
    EntitySubPredicate getSubPredicate();

    @Accessor
    EntityPredicate getVehicle();

    @Accessor
    EntityPredicate getPassenger();

    @Accessor
    EntityPredicate getTargetedEntity();

    @Accessor
    String getTeam();
}
