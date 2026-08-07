package com.yanny.awi.manager;

import com.yanny.aci.manager.CoreClientRegistry;
import com.yanny.awi.api.*;
import com.yanny.awi.configuration.AwiConfig;
import org.jetbrains.annotations.NotNull;

public class AwiClientRegistry extends CoreClientRegistry<AwiConfig, AwiCommonRegistry, IDataNode, IWidgetUtils, IClientUtils> implements IClientRegistry, IClientUtils, ICommonUtils {
    public AwiClientRegistry(AwiCommonRegistry registry) {
        super(registry);
    }

    @NotNull
    @Override
    public IWidgetFactory<IDataNode, IWidgetUtils> getMissingWidgetFactory() {
        return null; //TODO
    }
}
