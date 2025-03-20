package com.yanny.ali.mixin;

import net.minecraft.advancements.critereon.*;
import org.jetbrains.annotations.Nullable;

public interface MixinEntityPredicate {
    EntityTypePredicate getEntityType();
    DistancePredicate getDistanceToPlayer();
    LocationPredicate getLocation();
    LocationPredicate getSteppingOnLocation();
    MobEffectsPredicate getEffects();
    NbtPredicate getNbt();
    EntityFlagsPredicate getFlags();
    EntityEquipmentPredicate getEquipment();
    EntitySubPredicate getSubPredicate();
    EntityPredicate getVehicle();
    EntityPredicate getPassenger();
    EntityPredicate getTargetedEntity();
    @Nullable
    String getTeam();
}
