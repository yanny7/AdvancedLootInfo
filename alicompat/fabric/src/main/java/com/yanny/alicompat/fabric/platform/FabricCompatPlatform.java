package com.yanny.alicompat.fabric.platform;

import com.yanny.alicompat.platform.ICompatPlatform;
import net.fabricmc.loader.api.FabricLoader;

public class FabricCompatPlatform implements ICompatPlatform {
    @Override
    public boolean isModLoaded(String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
    }
}
