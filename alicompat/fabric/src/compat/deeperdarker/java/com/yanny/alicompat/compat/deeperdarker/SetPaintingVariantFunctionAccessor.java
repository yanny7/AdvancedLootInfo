package com.yanny.alicompat.compat.deeperdarker;

import com.kyanite.deeperdarker.util.SetPaintingVariantFunction;
import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.language.Lang;
import com.yanny.alicompat.accessor.BaseAccessor;
import com.yanny.alicompat.accessor.FieldAccessor;
import com.yanny.alicompat.accessor.IFunctionTooltip;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.decoration.PaintingVariant;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class SetPaintingVariantFunctionAccessor extends BaseAccessor<SetPaintingVariantFunction> implements IFunctionTooltip {
    @FieldAccessor
    private Optional<TagKey<PaintingVariant>> validPaintings;

    public SetPaintingVariantFunctionAccessor(SetPaintingVariantFunction parent) {
        super(parent);
    }

    @NotNull
    @Override
    public TooltipBuilder getTooltip(IServerUtils utils) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, validPaintings).build(Lang.Value.TAG));
            b.add(utils.getValueTooltip(utils, parent.predicates).build(Lang.Branch.PREDICATES));
            b.showEmpty();
        }, DeeperDarkerLang.Functions.SET_PAINTING_VARIANT);
    }
}
