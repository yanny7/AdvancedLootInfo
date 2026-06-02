package com.yanny.awi.emi.compatibility;

import com.mojang.logging.LogUtils;
import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import org.slf4j.Logger;

@EmiEntrypoint
public class EmiCompatibility implements EmiPlugin {
    private static final Logger LOGGER = LogUtils.getLogger();

    @Override
    public void register(EmiRegistry emiRegistry) {
        System.out.println("EMI REGISTERED");
//        GenericUtils.register(emiRegistry, this::registerData);
    }

    private void registerData(EmiRegistry registry, byte[] fullCompressedData) {
    }
}
