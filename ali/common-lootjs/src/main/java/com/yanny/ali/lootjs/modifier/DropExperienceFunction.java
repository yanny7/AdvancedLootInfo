package com.yanny.ali.lootjs.modifier;

import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;

public class DropExperienceFunction extends BaseLootItemFunction {
    public final NumberProvider amount;

    public DropExperienceFunction(NumberProvider amount) {
        this.amount = amount;
    }
}
