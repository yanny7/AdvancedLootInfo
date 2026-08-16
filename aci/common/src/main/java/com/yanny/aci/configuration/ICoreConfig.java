package com.yanny.aci.configuration;

public interface ICoreConfig {
    int getConfigVersion();

    void setConfigVersion(int configVersion);

    int getCurrentVersion();

    default void normalize() {
    }
}
