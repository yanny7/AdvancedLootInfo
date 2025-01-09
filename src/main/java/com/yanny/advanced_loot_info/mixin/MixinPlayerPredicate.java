package com.yanny.advanced_loot_info.mixin;

import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.advancements.critereon.PlayerPredicate;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.Stat;
import net.minecraft.world.level.GameType;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(PlayerPredicate.class)
public interface MixinPlayerPredicate {
    @Mixin(PlayerPredicate.AdvancementDonePredicate.class)
    interface AdvancementDonePredicate {
        @Accessor
        boolean getState();
    }

    @Mixin(PlayerPredicate.AdvancementCriterionsPredicate.class)
    interface AdvancementCriterionsPredicate {
        @Accessor
        Object2BooleanMap<String> getCriterions();
    }

    @Accessor
    MinMaxBounds.Ints getLevel();

    @Nullable
    @Accessor
    GameType getGameType();

    @Accessor
    Map<Stat<?>, MinMaxBounds.Ints> getStats();

    @Accessor
    Object2BooleanMap<ResourceLocation> getRecipes();

    @Accessor
    Map<ResourceLocation, PlayerPredicate.AdvancementPredicate> getAdvancements();

    @Accessor
    EntityPredicate getLookingAt();
}
