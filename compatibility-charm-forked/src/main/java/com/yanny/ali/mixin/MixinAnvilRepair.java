package com.yanny.ali.mixin;

import net.minecraft.world.entity.npc.VillagerTrades;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(targets = "svenhjol.charm.feature.extra_trades.ExtraTrades$AnvilRepair")
public interface MixinAnvilRepair extends VillagerTrades.ItemListing {
    @Accessor()
    int getVillagerXp();

    @Accessor()
    int getMaxUses();
}
