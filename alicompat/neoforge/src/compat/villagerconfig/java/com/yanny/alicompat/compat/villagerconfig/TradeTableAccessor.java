package com.yanny.alicompat.compat.villagerconfig;

import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.api.TradeLevel;
import com.yanny.alicompat.accessor.BaseAccessor;
import com.yanny.alicompat.accessor.FieldAccessor;
import com.yanny.alicompat.accessor.ReflectionUtils;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import me.drex.villagerconfig.common.data.TradeTable;
import me.drex.villagerconfig.common.data.TradeTier;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class TradeTableAccessor extends BaseAccessor<TradeTable> {
    @FieldAccessor
    private List<TradeTier> tiers;

    public TradeTableAccessor(TradeTable parent) {
        super(parent);
    }

    @NotNull
    public Int2ObjectMap<TradeLevel> getLevels(IServerUtils utils) {
        Int2ObjectMap<TradeLevel> levels = new Int2ObjectOpenHashMap<>();

        for (int i = 0; i < tiers.size(); i++) {
            levels.put(i + 1, ReflectionUtils.copyClassData(TradeTierAccessor.class, tiers.get(i), TradeTier.class).getLevel(utils));
        }

        return levels;
    }
}
