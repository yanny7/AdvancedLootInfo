package com.yanny.ali.plugin.client.widget.trades;

import com.yanny.aci.api.IWidget;
import com.yanny.aci.api.RelativeRect;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IWidgetUtils;
import com.yanny.ali.api.ListWidget;
import com.yanny.ali.plugin.client.WidgetUtils;
import com.yanny.ali.plugin.common.trades.TradeLevelNode;
import net.minecraft.core.HolderLookup;
import org.jetbrains.annotations.Nullable;

public class TradeLevelWidget extends ListWidget {
    private final HolderLookup.Provider provider;

    public TradeLevelWidget(IWidgetUtils utils, IDataNode entry, RelativeRect rect, int maxWidth) {
        super(utils, entry, rect, maxWidth);
        provider = utils.lookupProvider();
    }

    @Nullable
    @Override
    public IWidget getLootGroupWidget(RelativeRect rect, IDataNode entry) {
        return WidgetUtils.getLevelWidget(provider, rect, entry, ((TradeLevelNode) entry).level);
    }
}
