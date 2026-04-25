package com.yanny.awi.manager;

import com.yanny.aci.manager.CoreClientRegistry;
import com.yanny.awi.api.*;
import org.jetbrains.annotations.NotNull;

public class AwiClientRegistry extends CoreClientRegistry<Object, ICommonUtils, IServerUtils, ITooltipNode, IDataNode, IWidgetUtils, IClientUtils> implements IClientRegistry, IClientUtils, ICommonUtils {
    public AwiClientRegistry(ICommonUtils registry) {
        super(registry);
    }

    @NotNull
    @Override
    public IWidgetFactory<IServerUtils, ITooltipNode, IDataNode, IWidgetUtils> getMissingWidgetFactory() {
        return null;
    }
}
