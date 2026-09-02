package com.yanny.alicompat.forge;

import com.yanny.alicompat.Utils;
import com.yanny.alicompat.forge.datagen.DataGeneration;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(Utils.MOD_ID)
public class AliCompatMod {
    public AliCompatMod() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(DataGeneration::generate);
    }
}
