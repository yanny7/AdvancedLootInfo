package com.yanny.awi.plugin;

import com.yanny.awi.api.AwiEntrypoint;
import com.yanny.awi.api.IPlugin;
import org.jetbrains.annotations.NotNull;

@AwiEntrypoint
public class Plugin implements IPlugin {
    @NotNull
    @Override
    public String getModId() {
        return "awi";
    }
}
