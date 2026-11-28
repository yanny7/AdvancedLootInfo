package com.yanny.awi.plugin.client.widget;

import com.yanny.aci.api.IWidget;
import com.yanny.aci.api.RelativeRect;
import com.yanny.aci.api.WidgetDirection;
import com.yanny.aci.tooltip.TooltipLine;
import com.yanny.aci.tooltip.TooltipStyle;
import com.yanny.awi.api.IBlockNode;
import com.yanny.awi.api.IDataNode;
import com.yanny.awi.compatibility.BlockGeneration;
import com.yanny.awi.compatibility.BlockGenerationChart;
import com.yanny.awi.compatibility.GenericUtils;
import com.yanny.awi.language.Lang;
import com.yanny.awi.plugin.EnumTypes;
import com.yanny.awi.plugin.client.TooltipUtils;
import com.yanny.awi.plugin.client.WidgetUtils;
import com.yanny.awi.plugin.common.nodes.PlacedFeatureNode;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.GenerationStep;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class BlockGenerationWidget implements IWidget {
    private static final int ICON_SIZE = 18;
    private static final int INDENT = 4;
    private static final int GAP = 4;
    private static final int DIMENSION_GAP = 6;

    private final RelativeRect rect;
    private final List<IWidget> widgets = new ArrayList<>();

    public BlockGenerationWidget(RelativeRect rect, int width, List<BlockGeneration.Dimension> dimensions) {
        int y = 0;

        this.rect = rect;

        for (BlockGeneration.Dimension dimension : dimensions) {
            if (y > 0) {
                y += DIMENSION_GAP;
            }

            Component title = GenericUtils.getFormattedCategoryTitle(dimension.id()).copy().withStyle(ChatFormatting.UNDERLINE);
            BlockGenerationChart chart = BlockGenerationChart.of(dimension);
            Map<BlockGeneration.Source, Integer> colors = new IdentityHashMap<>();

            widgets.add(new LabelWidget(new RelativeRect(0, y, 0, 0, rect), title, width, LabelWidget.COLOR, List.of()));
            y += LabelWidget.HEIGHT + 2;

            for (int i = 0; i < chart.series().size(); i++) {
                colors.put(chart.series().get(i).source(), TooltipUtils.getStyle().level(i));
            }

            if (!chart.columns().isEmpty()) {
                ChartWidget chartWidget = new ChartWidget(new RelativeRect(0, y, 0, 0, rect), width, chart, colors);

                widgets.add(chartWidget);
                y += chartWidget.getRect().getHeight() + GAP;
            }

            for (BlockGeneration.BiomeGroup group : dimension.rows()) {
                y = addGroup(group, width, y, colors, chart.exclusions());
            }

            if (!dimension.merged().isEmpty()) {
                y = addMerged(dimension.merged(), width, y, colors, chart.exclusions());
            }
        }

        rect.setDimensions(width, y);
    }

    @NotNull
    @Override
    public RelativeRect getRect() {
        return rect;
    }

    @NotNull
    @Override
    public WidgetDirection getDirection() {
        return WidgetDirection.VERTICAL;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        for (IWidget widget : widgets) {
            widget.render(guiGraphics, mouseX, mouseY);
        }
    }

    @NotNull
    @Override
    public List<TooltipLine> getTooltipLines(int mouseX, int mouseY) {
        List<TooltipLine> lines = new LinkedList<>();

        for (IWidget widget : widgets) {
            if (widget.getRect().contains(mouseX, mouseY)) {
                lines.addAll(widget.getTooltipLines(mouseX, mouseY));
            }
        }

        return lines;
    }

    @NotNull
    static Component biomesLabel(List<ResourceLocation> biomes, boolean allBiomes) {
        if (allBiomes) {
            return Component.translatable(Lang.BlockGeneration.ALL_BIOMES.singular());
        } else if (biomes.size() == 1) {
            return GenericUtils.getBiomeTitle(biomes.get(0));
        }

        return Component.translatable(Lang.BlockGeneration.BIOMES.singular(), biomes.size());
    }

    private int addGroup(BlockGeneration.BiomeGroup group, int width, int y, Map<BlockGeneration.Source, Integer> colors,
                         Map<BlockGeneration.Source, BlockGenerationChart.Exclusion> exclusions) {
        Component text = biomesLabel(group.biomes(), group.allBiomes());
        boolean ownRow = INDENT + Minecraft.getInstance().font.width(text) + GAP + ICON_SIZE > width;
        int labelY = ownRow ? y + 1 : y + (ICON_SIZE - LabelWidget.HEIGHT) / 2 + 1;
        LabelWidget label = new LabelWidget(new RelativeRect(INDENT, labelY, 0, 0, rect), text, width - INDENT, LabelWidget.COLOR, biomesTooltip(group.biomes()));
        int iconStart = ownRow ? INDENT : INDENT + label.getRect().getWidth() + GAP;
        int x = iconStart;

        widgets.add(label);

        if (ownRow) {
            y += LabelWidget.HEIGHT + 1;
        }

        for (BlockGeneration.Source source : group.sources()) {
            if (x + ICON_SIZE > width && x > iconStart) {
                x = iconStart;
                y += ICON_SIZE;
            }

            widgets.add(new SourceWidget(new RelativeRect(x, y, ICON_SIZE, ICON_SIZE, rect), source, colors.getOrDefault(source, 0), exclusions.get(source), null));
            x += ICON_SIZE;
        }

        return y + ICON_SIZE;
    }

    private int addMerged(List<BlockGeneration.BiomeGroup> groups, int width, int y, Map<BlockGeneration.Source, Integer> colors,
                          Map<BlockGeneration.Source, BlockGenerationChart.Exclusion> exclusions) {
        int x = INDENT;

        widgets.add(new LabelWidget(new RelativeRect(INDENT, y + 1, 0, 0, rect), Component.translatable(Lang.BlockGeneration.VARIOUS_BIOMES.singular()),
                width - INDENT, LabelWidget.COLOR, List.of()));
        y += LabelWidget.HEIGHT + 1;

        for (BlockGeneration.BiomeGroup group : groups) {
            BlockGeneration.Source source = group.sources().get(0);

            if (x + ICON_SIZE > width && x > INDENT) {
                x = INDENT;
                y += ICON_SIZE;
            }

            widgets.add(new SourceWidget(new RelativeRect(x, y, ICON_SIZE, ICON_SIZE, rect), source, colors.getOrDefault(source, 0), exclusions.get(source),
                    biomesLine(biomeNames(group.biomes()))));
            x += ICON_SIZE;
        }

        return y + ICON_SIZE;
    }

    @NotNull
    private static Component stepName(int step) {
        GenerationStep.Decoration[] steps = GenerationStep.Decoration.values();

        if (step == BlockGeneration.BASE_TERRAIN_STEP) {
            return Component.translatable(Lang.GenerationStep.BASE_TERRAIN.singular());
        } else if (step < steps.length) {
            return Component.translatable(EnumTypes.key(steps[step]));
        }

        return Component.translatable(Lang.Value.EXTRA_GENERATION_STEP.singular(), step - steps.length + 1);
    }

    @NotNull
    private static Component biomesLine(Component biomes) {
        TooltipStyle style = TooltipUtils.getStyle();

        return Component.translatable(Lang.BlockGeneration.BIOMES_LIST.singular()).withStyle(style.text()).append(": ").append(biomes.copy().withStyle(style.value()));
    }

    @NotNull
    private static Component biomeNames(List<ResourceLocation> biomes) {
        MutableComponent names = Component.empty();

        for (int i = 0; i < biomes.size(); i++) {
            names.append(i == 0 ? Component.empty() : Component.literal(", ")).append(GenericUtils.getBiomeTitle(biomes.get(i)));
        }

        return names;
    }

    @NotNull
    private static List<TooltipLine> biomesTooltip(List<ResourceLocation> biomes) {
        return biomes.stream().map((b) -> TooltipLine.text(GenericUtils.getBiomeTitle(b))).toList();
    }

    @NotNull
    private static Lang.BlockGeneration exclusionKey(BlockGenerationChart.Exclusion exclusion) {
        return switch (exclusion) {
            case BASE_TERRAIN -> Lang.BlockGeneration.EXCLUDED_BASE_TERRAIN;
            case NO_PLACEMENT -> Lang.BlockGeneration.EXCLUDED_NO_PLACEMENT;
            case SURFACE -> Lang.BlockGeneration.EXCLUDED_SURFACE;
            case UNKNOWN_HEIGHT -> Lang.BlockGeneration.EXCLUDED_UNKNOWN_HEIGHT;
            case UNKNOWN_COUNT -> Lang.BlockGeneration.EXCLUDED_UNKNOWN_COUNT;
            case OUTSIDE_WORLD -> Lang.BlockGeneration.EXCLUDED_OUTSIDE_WORLD;
        };
    }

    private static class SourceWidget extends TextureWidget {
        private final int color;

        SourceWidget(RelativeRect rect, BlockGeneration.Source source, int color, @Nullable BlockGenerationChart.Exclusion exclusion,
                     @Nullable Component biomes) {
            super(WidgetUtils.TEXTURE_LOC, rect, iconU(source.step()), source.step() == BlockGeneration.BASE_TERRAIN_STEP ? 18 : 36);
            this.color = color;

            PlacedFeatureNode feature = source.feature();

            tooltipComponent(stepName(source.step()).copy().withStyle(ChatFormatting.WHITE));

            if (feature != null && feature.getFeatureId() != null) {
                tooltipComponent(Component.literal(feature.getFeatureId().toString()).withStyle(ChatFormatting.GRAY));
            }

            if (biomes != null) {
                tooltipComponent(biomes);
            }

            if (exclusion != null) {
                tooltipComponent(Component.translatable(exclusionKey(exclusion).singular()).withStyle(ChatFormatting.GRAY));
            }

            if (feature != null) {
                tooltipText(feature.getTooltip());
            }

            for (IBlockNode blockNode : source.blockNodes()) {
                if (blockNode instanceof IDataNode dataNode) {
                    tooltipText(dataNode.getTooltip());
                }
            }
        }

        @Override
        public void render(GuiGraphics draw, int mouseX, int mouseY) {
            super.render(draw, mouseX, mouseY);

            if (color != 0) {
                draw.fill(rect.getX() + 1, rect.getBottom() - 3, rect.getRight() - 1, rect.getBottom() - 1, color);
            }
        }

        private static int iconU(int step) {
            if (step == BlockGeneration.BASE_TERRAIN_STEP) {
                return 7;
            }

            return ICON_SIZE * Math.min(step, GenerationStep.Decoration.values().length);
        }
    }
}
