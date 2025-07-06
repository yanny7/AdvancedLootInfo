package com.yanny.ali.mixin;

import com.almostreliable.lootjs.loot.condition.WrappedDamageSourceCondition;
import net.minecraft.advancements.critereon.DamageSourcePredicate;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(WrappedDamageSourceCondition.class)
public interface MixinWrappedDamageSourceCondition {
    @Accessor
    DamageSourcePredicate getPredicate();

    @Nullable
    @Accessor
    String[] getSourceNames();
}
