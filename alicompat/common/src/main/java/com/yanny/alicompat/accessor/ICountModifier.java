package com.yanny.alicompat.accessor;

import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.plugin.server.EnchantedRanges;

public interface ICountModifier {
    void applyCountModifier(IServerUtils utils, EnchantedRanges count);
}
