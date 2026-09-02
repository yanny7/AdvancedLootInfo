package com.yanny.alicompat.forge.platform;

import com.yanny.alicompat.platform.ICompatPlatform;
import net.minecraftforge.fml.ModList;

public class ForgeCompatPlatform implements ICompatPlatform {
    @Override
    public boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }
}
