package com.yanny.awi.manager;

import com.yanny.aci.manager.CoreClientRegistry;
import com.yanny.awi.api.*;
import org.jetbrains.annotations.NotNull;

public class AwiClientRegistry extends CoreClientRegistry<Object, ICommonUtils, IDataNode, IWidgetUtils, IClientUtils> implements IClientRegistry, IClientUtils, ICommonUtils {
    public AwiClientRegistry(ICommonUtils registry) {
        super(registry);
    }

    @NotNull
    @Override
    public IWidgetFactory<IDataNode, IWidgetUtils> getMissingWidgetFactory() {
        return null;
    }
}
