package com.yanny.ali.plugin.kubejs.modifier;

import com.almostreliable.lootjs.loot.action.DropExperienceAction;
import com.yanny.ali.mixin.MixinDropExperienceAction;

public class DropExperienceFunction extends BaseLootItemFunction {
    public final int amount;

    public DropExperienceFunction(DropExperienceAction action) {
        amount = ((MixinDropExperienceAction) action).getAmount();
    }
}
