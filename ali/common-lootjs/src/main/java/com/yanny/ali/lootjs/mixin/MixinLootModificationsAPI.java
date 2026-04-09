package com.yanny.ali.lootjs.mixin;

import com.almostreliable.lootjs.LootModificationsAPI;
import com.almostreliable.lootjs.loot.modifier.LootModifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(LootModificationsAPI.class)
public interface MixinLootModificationsAPI {
    @Accessor
    static List<LootModifier> getModifiers() {
        throw new IllegalStateException();
    }
}
