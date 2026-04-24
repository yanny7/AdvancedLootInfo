package com.yanny.ali.plugin.mods;

import com.yanny.aci.api.RangeValue;
import com.yanny.ali.api.IServerUtils;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.Map;

public interface ICountModifier {
    void applyCountModifier(IServerUtils utils, Map<Enchantment, Map<Integer, RangeValue>> count);
}
