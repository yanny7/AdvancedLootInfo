package com.yanny.alicompat.neoforge;

import com.yanny.alicompat.Utils;
import com.yanny.alicompat.neoforge.datagen.DataGeneration;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(Utils.MOD_ID)
public class AliCompatMod {
    public AliCompatMod(IEventBus modEventBus) {
        modEventBus.addListener(DataGeneration::generate);
    }
}
