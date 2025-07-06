package com.yanny.ali.plugin.lootjs.modifier;

import com.almostreliable.lootjs.loot.action.LightningStrikeAction;
import com.yanny.ali.mixin.MixinLightningStrikeAction;

public class LightningStrikeFunction extends BaseLootItemFunction {
    public final boolean shouldDamageEntity;

    public LightningStrikeFunction(LightningStrikeAction action) {
        shouldDamageEntity = ((MixinLightningStrikeAction) action).getShouldDamageEntity();
    }
}
