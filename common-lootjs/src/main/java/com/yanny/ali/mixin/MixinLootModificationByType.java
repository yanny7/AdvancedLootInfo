package com.yanny.ali.mixin;

import com.almostreliable.lootjs.core.LootContextType;
import com.almostreliable.lootjs.core.LootModificationByType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(LootModificationByType.class)
public interface MixinLootModificationByType {
    @Accessor
    List<LootContextType> getTypes();
}
