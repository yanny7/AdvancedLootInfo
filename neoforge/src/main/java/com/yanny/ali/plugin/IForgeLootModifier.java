package com.yanny.ali.plugin;

import com.yanny.ali.api.ILootModifier;
import com.yanny.ali.api.IServerUtils;

import java.util.Optional;

public interface IForgeLootModifier {
    Optional<ILootModifier<?>> getLootModifier(IServerUtils utils);
}
