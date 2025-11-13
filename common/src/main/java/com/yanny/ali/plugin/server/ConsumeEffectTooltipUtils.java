package com.yanny.ali.plugin.server;

import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.api.ITooltipNode;
import com.yanny.ali.plugin.common.tooltip.BranchTooltipNode;
import com.yanny.ali.plugin.common.tooltip.LiteralTooltipNode;
import net.minecraft.world.item.consume_effects.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

public class ConsumeEffectTooltipUtils {
    @NotNull
    public static ITooltipNode getApplyEffectsTooltip(IServerUtils utils, ApplyStatusEffectsConsumeEffect effect) {
        return BranchTooltipNode.branch()
                .add(utils.getValueTooltip(utils, effect.effects()).build("ali.property.branch.effects"))
                .add(utils.getValueTooltip(utils, effect.probability()).build("ali.property.value.probability"))
                .build("ali.type.consume_effect.apply_effects");
    }

    @NotNull
    public static ITooltipNode getRemoveEffectsTooltip(IServerUtils utils, RemoveStatusEffectsConsumeEffect effect) {
        return utils.getValueTooltip(utils, effect.effects()).build("ali.type.consume_effect.remove_effects");
    }

    @Unmodifiable
    @NotNull
    public static ITooltipNode getClearAllEffectsTooltip(IServerUtils ignoredUtils, ConsumeEffect ignoredEEffect) {
        return LiteralTooltipNode.translatable("ali.type.consume_effect.clear_all_effects");
    }

    @Unmodifiable
    @NotNull
    public static ITooltipNode getTeleportRandomlyTooltip(IServerUtils utils, TeleportRandomlyConsumeEffect effect) {
        return BranchTooltipNode.branch()
                .add(utils.getValueTooltip(utils, effect.diameter()).build("ali.property.value.diameter"))
                .build("ali.type.consume_effect.teleport_randomly");
    }

    @Unmodifiable
    @NotNull
    public static ITooltipNode getPlaySoundTooltip(IServerUtils utils, PlaySoundConsumeEffect effect) {
        return BranchTooltipNode.branch()
                .add(utils.getValueTooltip(utils, effect.sound()).build("ali.property.value.sound"))
                .build("ali.type.consume_effect.play_sound");
    }
}
