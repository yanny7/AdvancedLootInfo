package com.yanny.awi.api;

public interface IPlugin {
    String getModId();

    default void registerServer(IServerRegistry registry) {

    }
}
