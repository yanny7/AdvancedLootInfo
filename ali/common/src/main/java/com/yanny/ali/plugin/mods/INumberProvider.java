package com.yanny.ali.plugin.mods;

import com.yanny.aci.api.RangeValue;
import com.yanny.ali.api.IServerUtils;

public interface INumberProvider {
    RangeValue convertNumber(IServerUtils utils);
}
