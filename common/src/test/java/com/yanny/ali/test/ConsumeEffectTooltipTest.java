package com.yanny.ali.test;

import com.yanny.ali.plugin.client.ConsumeEffectTooltipUtils;
import net.minecraft.core.HolderSet;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.consume_effects.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.yanny.ali.test.TooltipTestSuite.UTILS;
import static com.yanny.ali.test.utils.TestUtils.assertTooltip;

public class ConsumeEffectTooltipTest {
    @Test
    public void testApplyEffectTooltip() {
        assertTooltip(ConsumeEffectTooltipUtils.getApplyEffectsTooltip(UTILS, 0, new ApplyStatusEffectsConsumeEffect(
                List.of(
                        new MobEffectInstance(MobEffects.BAD_OMEN),
                        new MobEffectInstance(MobEffects.CONFUSION)
                )
        )), List.of(
                "Apply Effects:",
                "  -> Effects:",
                "    -> minecraft:bad_omen",
                "      -> Duration: 0",
                "      -> Amplifier: 0",
                "      -> Ambient: false",
                "      -> Is Visible: true",
                "      -> Show Icon: true",
                "    -> minecraft:nausea",
                "      -> Duration: 0",
                "      -> Amplifier: 0",
                "      -> Ambient: false",
                "      -> Is Visible: true",
                "      -> Show Icon: true",
                "  -> Probability: 1.0"
        ));
    }

    @Test
    public void testRemoveEffectTooltip() {
        assertTooltip(ConsumeEffectTooltipUtils.getRemoveEffectsTooltip(UTILS, 0, new RemoveStatusEffectsConsumeEffect(
                HolderSet.direct(MobEffects.BLINDNESS, MobEffects.DIG_SPEED)
        )), List.of(
                "Remove Effects:",
                "  -> minecraft:blindness",
                "  -> minecraft:haste"
        ));
    }

    @Test
    public void testClearAllEffectsTooltip() {
        assertTooltip(ConsumeEffectTooltipUtils.getClearAllEffectsTooltip(UTILS, 0, new ClearAllStatusEffectsConsumeEffect()), List.of("Clear All Effects:"));
    }

    @Test
    public void testTeleportRandomlyTooltip() {
        assertTooltip(ConsumeEffectTooltipUtils.getTeleportRandomlyTooltip(UTILS, 0, new TeleportRandomlyConsumeEffect(30)), List.of(
                "Teleport Randomly:",
                "  -> Diameter: 30.0"
        ));
    }

    @Test
    public void testPlaySoundTooltip() {
        assertTooltip(ConsumeEffectTooltipUtils.getPlaySoundTooltip(UTILS, 0, new PlaySoundConsumeEffect(SoundEvents.ARMOR_EQUIP_CHAIN)), List.of(
                "Play Sound:",
                "  -> Sound: minecraft:item.armor.equip_chain"
        ));
    }
}
