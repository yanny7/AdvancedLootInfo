package com.yanny.ali.plugin.client;

import com.yanny.ali.api.IClientUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.consume_effects.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.LinkedList;
import java.util.List;

import static com.yanny.ali.plugin.client.GenericTooltipUtils.*;

public class ConsumeEffectTooltipUtils {
    @NotNull
    public static List<Component> getApplyEffectsTooltip(IClientUtils utils, int pad, ApplyStatusEffectsConsumeEffect effect) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.consume_effect.apply_effects")));
        components.addAll(getCollectionTooltip(utils, pad + 1, "ali.property.branch.effects", "ali.property.branch.effect", effect.effects(), GenericTooltipUtils::getMobEffectInstanceTooltip));
        components.addAll(getFloatTooltip(utils, pad + 1, "ali.property.value.seed", effect.probability()));

        return components;
    }

    @NotNull
    public static List<Component> getRemoveEffectsTooltip(IClientUtils utils, int pad, RemoveStatusEffectsConsumeEffect effect) {
        return getHolderSetTooltip(utils, pad, "ali.type.consume_effect.remove_effects", "ali.property.value.null", effect.effects(), GenericTooltipUtils::getMobEffectTooltip);
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getClearAllEffectsTooltip(IClientUtils ignoredUtils, int pad, ConsumeEffect ignoredEEffect) {
        return List.of(pad(pad, translatable("ali.type.consume_effect.clear_all_effects")));
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getTeleportRandomlyTooltip(IClientUtils utils, int pad, TeleportRandomlyConsumeEffect effect) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.consume_effect.teleport_randomly")));
        components.addAll(getFloatTooltip(utils, pad + 1, "ali.property.value.diameter", effect.diameter()));

        return components;
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getPlaySoundTooltip(IClientUtils utils, int pad, PlaySoundConsumeEffect effect) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.consume_effect.play_sound")));
        components.addAll(getRegistryTooltip(utils, pad + 1, "ali.property.value.sound", Registries.SOUND_EVENT, effect.sound().value()));

        return components;
    }
}
