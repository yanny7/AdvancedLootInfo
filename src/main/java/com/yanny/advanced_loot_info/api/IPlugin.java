package com.yanny.advanced_loot_info.api;

public interface IPlugin {
    default void registerCommon(ICommonRegistry registry) {

    }

    default void registerClient(IClientRegistry registry) {

    }
}
