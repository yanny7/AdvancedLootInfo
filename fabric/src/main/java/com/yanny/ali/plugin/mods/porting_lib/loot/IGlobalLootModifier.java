package com.yanny.ali.plugin.mods.porting_lib.loot;

import com.yanny.ali.api.ILootModifier;
import com.yanny.ali.api.IServerUtils;

import java.util.Optional;

public interface IGlobalLootModifier {
    Optional<ILootModifier<?>> getLootModifier(IServerUtils utils);
}
