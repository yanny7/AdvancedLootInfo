package com.yanny.alicompat.compat.enderio;

import com.enderio.enderio.content.paint.CopyPaintFunction;
import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.language.Lang;
import com.yanny.alicompat.accessor.BaseAccessor;
import com.yanny.alicompat.accessor.FieldAccessor;
import com.yanny.alicompat.accessor.IFunctionTooltip;
import org.jetbrains.annotations.NotNull;

public class CopyPaintFunctionAccessor extends BaseAccessor<CopyPaintFunction> implements IFunctionTooltip {
    @FieldAccessor
    private boolean shouldCopyPrimary;

    public CopyPaintFunctionAccessor(CopyPaintFunction parent) {
        super(parent);
    }

    @NotNull
    @Override
    public TooltipBuilder getTooltip(IServerUtils utils) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, shouldCopyPrimary).build(EnderIoLang.Value.COPY_PRIMARY));
            b.add(utils.getValueTooltip(utils, parent.predicates).build(Lang.Branch.PREDICATES));
        }, EnderIoLang.Functions.COPY_PAINT);
    }
}
