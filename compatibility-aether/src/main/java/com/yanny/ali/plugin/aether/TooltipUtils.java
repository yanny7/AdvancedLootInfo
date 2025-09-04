package com.yanny.ali.plugin.aether;

import com.aetherteam.aether.loot.conditions.ConfigEnabled;
import com.aetherteam.aether.loot.functions.DoubleDrops;
import com.aetherteam.aether.loot.functions.SpawnTNT;
import com.aetherteam.aether.loot.functions.SpawnXP;
import com.aetherteam.aether.loot.functions.WhirlwindSpawnEntity;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.api.ITooltipNode;
import com.yanny.ali.api.TooltipNode;
import com.yanny.ali.mixin.MixinWhirlwindSpawnEntity;
import com.yanny.ali.plugin.server.RegistriesTooltipUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

import static com.yanny.ali.plugin.server.GenericTooltipUtils.*;

public class TooltipUtils {
    @NotNull
    public static ITooltipNode getSpawnXpTooltip(IServerUtils utils, SpawnXP fun) {
        ITooltipNode tooltip = new TooltipNode(translatable("ali.type.function.spawn_xp"));

        tooltip.add(getSubConditionsTooltip(utils, Arrays.asList(fun.predicates)));

        return tooltip;
    }

    @NotNull
    public static ITooltipNode getSpawnTntTooltip(IServerUtils utils, SpawnTNT fun) {
        ITooltipNode tooltip = new TooltipNode(translatable("ali.type.function.spawn_tnt"));

        tooltip.add(getSubConditionsTooltip(utils, Arrays.asList(fun.predicates)));

        return tooltip;
    }

    @NotNull
    public static ITooltipNode getDoubleDropsTooltip(IServerUtils utils, DoubleDrops fun) {
        ITooltipNode tooltip = new TooltipNode(translatable("ali.type.function.double_drops"));

        tooltip.add(getSubConditionsTooltip(utils, Arrays.asList(fun.predicates)));

        return tooltip;
    }

    @NotNull
    public static ITooltipNode getWhirlwindSpawnEntityTooltip(IServerUtils utils, WhirlwindSpawnEntity fun) {
        MixinWhirlwindSpawnEntity mixin = (MixinWhirlwindSpawnEntity) fun;
        ITooltipNode tooltip = new TooltipNode(translatable("ali.type.function.whirlwind_spawn_entity"));

        tooltip.add(RegistriesTooltipUtils.getEntityTypeTooltip(utils, "ali.property.value.entity_type", mixin.getEntityType()));
        tooltip.add(getIntegerTooltip(utils, "ali.property.value.count", mixin.getCount()));
        tooltip.add(getSubConditionsTooltip(utils, Arrays.asList(fun.predicates)));

        return tooltip;
    }

    @NotNull
    public static ITooltipNode getConfigEnabledTooltip(IServerUtils ignoredUtils, ConfigEnabled ignoredCond) {
        return new TooltipNode(translatable("ali.type.function.config_enabled"));
    }
}
