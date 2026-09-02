package com.yanny.alicompat.neoforge.platform;

import com.yanny.alicompat.platform.ICompatPlatform;
import net.neoforged.fml.ModList;

public class NeoForgeCompatPlatform implements ICompatPlatform {
    @Override
    public boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }
}
