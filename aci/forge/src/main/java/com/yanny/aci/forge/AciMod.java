package com.yanny.aci.forge;

import com.yanny.aci.Utils;
import com.yanny.aci.forge.datagen.DataGeneration;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(Utils.MOD_ID)
public class AciMod {
    public AciMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        modEventBus.addListener(DataGeneration::generate);
    }
}
