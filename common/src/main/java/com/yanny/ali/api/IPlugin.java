package com.yanny.ali.api;

public interface IPlugin {
    String getModId();

    default void registerClient(IClientRegistry registry) {

    }

    default void registerServer(IServerRegistry registry) {

    }

    default void registerCommon(ICommonRegistry registry) {

    }
}
