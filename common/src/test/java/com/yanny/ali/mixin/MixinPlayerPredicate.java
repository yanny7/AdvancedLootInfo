package com.yanny.ali.mixin;

import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.advancements.critereon.PlayerPredicate;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.Stat;
import net.minecraft.world.level.GameType;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public interface MixinPlayerPredicate {
    interface AdvancementDonePredicate {
        boolean getState();
    }

    interface AdvancementCriterionsPredicate {
        Object2BooleanMap<String> getCriterions();
    }

    MinMaxBounds.Ints getLevel();

    @Nullable
    GameType getGameType();

    Map<Stat<?>, MinMaxBounds.Ints> getStats();
    Object2BooleanMap<ResourceLocation> getRecipes();
    Map<ResourceLocation, PlayerPredicate.AdvancementPredicate> getAdvancements();
    EntityPredicate getLookingAt();
}
