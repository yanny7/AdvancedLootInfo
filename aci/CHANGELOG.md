## []

- Added `ManagedRegistry.entries`, so a registry's whole content can be read back

## [1.1.0]

- Added `TooltipStyle` and `TooltipColors`, so tooltip text, value, error and branch colors can be supplied by the calling mod
- Enum values are no longer rendered by `CommonValueTooltip` - a mod has to register its own handler for `Enum.class`, otherwise an enum falls through to the JSON dump fallback

## [1.0.0]

- First release
