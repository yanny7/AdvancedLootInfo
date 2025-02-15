package com.yanny.ali.api;

public interface IPlugin {
    default void registerCommon(ICommonRegistry registry) {

    }

    default void registerClient(IClientRegistry registry) {

    }
}
