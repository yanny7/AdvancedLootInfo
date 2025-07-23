package com.yanny.ali.api;

public interface IPlugin {
    default void registerClient(IClientRegistry registry) {

    }

    default void registerServer(IServerRegistry registry) {

    }

    default void registerCommon(ICommonRegistry registry) {

    }
}
