package com.yanny.aci.neoforge;

import com.yanny.aci.Utils;
import com.yanny.aci.neoforge.datagen.DataGeneration;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(Utils.MOD_ID)
public class AciMod {
    public AciMod(IEventBus modEventBus) {
        modEventBus.addListener(DataGeneration::generate);
    }
}
