package com.yanny.awi.manager;

import com.yanny.aci.manager.CoreClientRegistry;
import com.yanny.awi.api.*;
import net.minecraft.core.HolderLookup;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AwiClientRegistry extends CoreClientRegistry<Object, ICommonUtils, IDataNode, IWidgetUtils, IClientUtils> implements IClientRegistry, IClientUtils, ICommonUtils {
    private HolderLookup.Provider provider;

    public AwiClientRegistry(ICommonUtils registry) {
        super(registry);
    }

    public void setLookupProvider(HolderLookup.Provider provider) {
        this.provider = provider;
    }

    @NotNull
    @Override
    public IWidgetFactory<IDataNode, IWidgetUtils> getMissingWidgetFactory() {
        return null;
    }

    @Nullable
    @Override
    public HolderLookup.Provider lookupProvider() {
        return provider;
    }
}
