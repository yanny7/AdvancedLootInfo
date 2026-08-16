package com.yanny.aci.configuration;

public interface ICoreConfig {
    int getConfigVersion();

    void setConfigVersion(int configVersion);

    int getCurrentVersion();

    /**
     * Called on a freshly deserialized config. An explicit {@code null} in the file is treated the same way as a
     * missing key - implementations fill such fields back in from a default instance.
     */
    default void normalize() {
    }
}
