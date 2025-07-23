package com.yanny.ali.plugin.server;

import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.api.ITooltipNode;
import com.yanny.ali.api.TooltipNode;
import net.minecraft.world.item.consume_effects.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import static com.yanny.ali.plugin.server.GenericTooltipUtils.*;

public class ConsumeEffectTooltipUtils {
    @NotNull
    public static ITooltipNode getApplyEffectsTooltip(IServerUtils utils, ApplyStatusEffectsConsumeEffect effect) {
        ITooltipNode components = new TooltipNode(translatable("ali.type.consume_effect.apply_effects"));

        components.add(getCollectionTooltip(utils, "ali.property.branch.effects", "ali.property.value.null", effect.effects(), GenericTooltipUtils::getMobEffectInstanceTooltip));
        components.add(getFloatTooltip(utils, "ali.property.value.probability", effect.probability()));

        return components;
    }

    @NotNull
    public static ITooltipNode getRemoveEffectsTooltip(IServerUtils utils, RemoveStatusEffectsConsumeEffect effect) {
        return getHolderSetTooltip(utils, "ali.type.consume_effect.remove_effects", "ali.property.value.null", effect.effects(), RegistriesTooltipUtils::getMobEffectTooltip);
    }

    @Unmodifiable
    @NotNull
    public static ITooltipNode getClearAllEffectsTooltip(IServerUtils ignoredUtils, ConsumeEffect ignoredEEffect) {
        return new TooltipNode(translatable("ali.type.consume_effect.clear_all_effects"));
    }

    @Unmodifiable
    @NotNull
    public static ITooltipNode getTeleportRandomlyTooltip(IServerUtils utils, TeleportRandomlyConsumeEffect effect) {
        ITooltipNode components = new TooltipNode(translatable("ali.type.consume_effect.teleport_randomly"));

        components.add(getFloatTooltip(utils, "ali.property.value.diameter", effect.diameter()));

        return components;
    }

    @Unmodifiable
    @NotNull
    public static ITooltipNode getPlaySoundTooltip(IServerUtils utils, PlaySoundConsumeEffect effect) {
        ITooltipNode components = new TooltipNode(translatable("ali.type.consume_effect.play_sound"));

        components.add(getHolderTooltip(utils, "ali.property.value.sound", effect.sound(), RegistriesTooltipUtils::getSoundEventTooltip));

        return components;
    }
}
