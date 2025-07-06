package com.yanny.ali.mixin;

import com.almostreliable.lootjs.LootModificationsAPI;
import com.almostreliable.lootjs.core.ILootAction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(LootModificationsAPI.class)
public interface MixinLootModificationsAPI {
    @Accessor
    static List<ILootAction> getActions() {
        throw new IllegalStateException();
    }
}
