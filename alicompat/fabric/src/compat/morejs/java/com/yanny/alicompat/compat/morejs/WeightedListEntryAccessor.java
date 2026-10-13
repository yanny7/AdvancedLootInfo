package com.yanny.alicompat.compat.morejs;

import com.almostreliable.morejs.util.ResourceOrTag;
import com.yanny.aci.language.ITooltipKey;
import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.language.Lang;
import com.yanny.alicompat.accessor.BaseAccessor;
import com.yanny.alicompat.accessor.ClassAccessor;
import com.yanny.alicompat.accessor.FieldAccessor;
import com.yanny.alicompat.accessor.IValueTooltip;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

@ClassAccessor("com.almostreliable.morejs.util.WeightedList$Entry")
public class WeightedListEntryAccessor extends BaseAccessor<Object> implements IValueTooltip {
    @FieldAccessor
    private int weight;

    @FieldAccessor
    private Object value;

    public WeightedListEntryAccessor(Object parent) {
        super(parent);
    }

    @NotNull
    @Override
    public TooltipBuilder getTooltip(IServerUtils utils) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, weight).build(Lang.Value.WEIGHT));
            b.add(utils.getValueTooltip(utils, value).build(getValueKey()));
        });
    }

    @NotNull
    private ITooltipKey getValueKey() {
        if (value instanceof ResourceOrTag<?> resourceOrTag) {
            ResourceLocation registry = ResourceOrTagAccessor.of(resourceOrTag).getRegistry();

            if (Registries.STRUCTURE.location().equals(registry)) {
                return Lang.Value.STRUCTURE;
            }

            if (Registries.BIOME.location().equals(registry)) {
                return Lang.Value.BIOME;
            }
        }

        return Lang.Value.DESTINATION;
    }
}
