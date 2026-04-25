package com.yanny.awi.manager;

import com.yanny.aci.manager.CoreCommonRegistry;
import com.yanny.awi.api.ICommonRegistry;
import com.yanny.awi.api.ICommonUtils;
import org.jetbrains.annotations.NotNull;

public class AwiCommonRegistry extends CoreCommonRegistry<Object> implements ICommonRegistry, ICommonUtils {
    @NotNull
    @Override
    protected Object loadConfiguration() {
        return null;
    }
}
