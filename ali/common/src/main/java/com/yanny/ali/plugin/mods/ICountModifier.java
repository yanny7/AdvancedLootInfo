package com.yanny.ali.plugin.mods;

import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.plugin.server.EnchantedRanges;

public interface ICountModifier {
    void applyCountModifier(IServerUtils utils, EnchantedRanges count);
}
