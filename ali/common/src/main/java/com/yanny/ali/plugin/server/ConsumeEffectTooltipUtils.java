package com.yanny.ali.plugin.server;

import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.ali.api.IServerUtils;
import net.minecraft.world.item.consume_effects.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

public class ConsumeEffectTooltipUtils {
    @NotNull
    public static TooltipNode getApplyEffectsTooltip(IServerUtils utils, ApplyStatusEffectsConsumeEffect effect) {
        return TooltipBuilder.array((b) -> b
                .add(utils.getValueTooltip(utils, effect.effects()).build("ali.property.branch.effects"))
                .add(utils.getValueTooltip(utils, effect.probability()).build("ali.property.value.probability"))
                )
                .build("ali.type.consume_effect.apply_effects");
    }

    @NotNull
    public static TooltipNode getRemoveEffectsTooltip(IServerUtils utils, RemoveStatusEffectsConsumeEffect effect) {
        return utils.getValueTooltip(utils, effect.effects()).build("ali.type.consume_effect.remove_effects");
    }

    @Unmodifiable
    @NotNull
    public static TooltipNode getClearAllEffectsTooltip(IServerUtils ignoredUtils, ConsumeEffect ignoredEEffect) {
        return TooltipBuilder.keyOnly("ali.type.consume_effect.clear_all_effects").build();
    }

    @Unmodifiable
    @NotNull
    public static TooltipNode getTeleportRandomlyTooltip(IServerUtils utils, TeleportRandomlyConsumeEffect effect) {
        return TooltipBuilder.array((b) -> b
                .add(utils.getValueTooltip(utils, effect.diameter()).build("ali.property.value.diameter"))
                )
                .build("ali.type.consume_effect.teleport_randomly");
    }

    @Unmodifiable
    @NotNull
    public static TooltipNode getPlaySoundTooltip(IServerUtils utils, PlaySoundConsumeEffect effect) {
        return TooltipBuilder.array((b) -> b
                .add(utils.getValueTooltip(utils, effect.sound()).build("ali.property.value.sound"))
                )
                .build("ali.type.consume_effect.play_sound");
    }
}
