package com.yanny.alicompat.accessor;

import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.plugin.server.EnchantedRanges;

public interface IChanceModifier {
    void applyChanceModifier(IServerUtils utils, EnchantedRanges chance);
}
