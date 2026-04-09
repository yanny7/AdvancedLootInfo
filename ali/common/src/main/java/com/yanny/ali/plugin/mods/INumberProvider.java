package com.yanny.ali.plugin.mods;

import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.api.RangeValue;

public interface INumberProvider {
    RangeValue convertNumber(IServerUtils utils);
}
