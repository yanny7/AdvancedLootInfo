package com.yanny.ali.plugin.server;

import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.language.Lang;
import net.minecraft.world.item.consume_effects.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

public class ConsumeEffectTooltipUtils {
    @NotNull
    public static TooltipBuilder getApplyEffectsTooltip(IServerUtils utils, ApplyStatusEffectsConsumeEffect effect) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, effect.effects()).build(Lang.Branch.EFFECTS));
            b.add(utils.getValueTooltip(utils, effect.probability()).build(Lang.Value.PROBABILITY));
        }).key(Lang.ConsumeEffects.APPLY_EFFECTS);
    }

    @NotNull
    public static TooltipBuilder getRemoveEffectsTooltip(IServerUtils utils, RemoveStatusEffectsConsumeEffect effect) {
        return utils.getValueTooltip(utils, effect.effects()).key(Lang.ConsumeEffects.REMOVE_EFFECTS);
    }

    @Unmodifiable
    @NotNull
    public static TooltipBuilder getClearAllEffectsTooltip(IServerUtils ignoredUtils, ConsumeEffect ignoredEEffect) {
        return TooltipBuilder.keyOnly(Lang.ConsumeEffects.CLEAR_ALL_EFFECTS);
    }

    @Unmodifiable
    @NotNull
    public static TooltipBuilder getTeleportRandomlyTooltip(IServerUtils utils, TeleportRandomlyConsumeEffect effect) {
        return TooltipBuilder.array((b) -> b.add(utils.getValueTooltip(utils, effect.diameter()).build(Lang.Value.DIAMETER)))
                .key(Lang.ConsumeEffects.TELEPORT_RANDOMLY);
    }

    @Unmodifiable
    @NotNull
    public static TooltipBuilder getPlaySoundTooltip(IServerUtils utils, PlaySoundConsumeEffect effect) {
        return TooltipBuilder.array((b) -> b.add(utils.getValueTooltip(utils, effect.sound()).build(Lang.Value.SOUND)))
                .key(Lang.ConsumeEffects.PLAY_SOUND);
    }
}
