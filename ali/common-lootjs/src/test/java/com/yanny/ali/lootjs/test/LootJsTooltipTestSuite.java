package com.yanny.ali.lootjs.test;

import com.yanny.ali.lootjs.LootJsPlugin;
import com.yanny.ali.manager.PluginManager;
import com.yanny.ali.test.TooltipTestSuite;
import org.junit.platform.suite.api.BeforeSuite;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectClasses({
        LootJsConditionTooltipTest.class,
        LootJsFunctionTooltipTest.class,
        LootJsGenericTooltipTest.class,
        LootJsNodeTest.class,
        LootJsUtilsTest.class,
        LootModifierTest.class,
        LootModifierScopeTest.class
})
public class LootJsTooltipTestSuite {
    @BeforeSuite
    static void beforeAllTests() throws NoSuchFieldException, IllegalAccessException {
        TooltipTestSuite.beforeAllTests();
        new LootJsPlugin().registerServer(PluginManager.getInstance().serverRegistry);
    }
}
