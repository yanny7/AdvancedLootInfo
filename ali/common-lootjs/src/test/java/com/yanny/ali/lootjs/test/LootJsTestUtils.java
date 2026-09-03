package com.yanny.ali.lootjs.test;

import org.mockito.Mockito;

import static org.mockito.Mockito.withSettings;

public class LootJsTestUtils {
    public static <T> T mock(Class<T> type, Class<?>... accessors) {
        return Mockito.mock(type, withSettings().extraInterfaces(accessors));
    }
}
