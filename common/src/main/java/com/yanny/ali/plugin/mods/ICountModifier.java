package com.yanny.ali.plugin.mods;

import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.api.RangeValue;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.Map;

public interface ICountModifier {
    void applyCountModifier(IServerUtils utils, Map<Enchantment, Map<Integer, RangeValue>> count);
}
