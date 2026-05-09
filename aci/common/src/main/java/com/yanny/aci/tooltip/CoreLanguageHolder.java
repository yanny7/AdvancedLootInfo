package com.yanny.aci.tooltip;

import java.util.HashMap;
import java.util.Map;

public class CoreLanguageHolder {
    public static final Map<String, String> TRANSLATION_MAP = new HashMap<>();

    static {
        TRANSLATION_MAP.put("aci.util.auto_detected", "Auto-detected: %s");
        TRANSLATION_MAP.put("aci.util.missing", "Not implemented: %s");
    }
}
