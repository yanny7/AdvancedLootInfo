package com.yanny.alicompat.compat.goblintraders;

import com.mrcrayfish.goblintraders.entity.AbstractGoblinEntity;
import com.mrcrayfish.goblintraders.trades.type.BaseTrade;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.trading.MerchantOffer;
import org.jetbrains.annotations.Nullable;

public record GoblinTradeListing(BaseTrade trade) implements VillagerTrades.ItemListing {
    // ALI probes an unregistered listing with getOffer(null, null), so this must not assume a trader is present
    @Nullable
    @Override
    public MerchantOffer getOffer(Entity trader, RandomSource random) {
        if (trader instanceof AbstractGoblinEntity goblin) {
            return trade.createVanillaOffer(goblin, random);
        }

        return null;
    }
}
