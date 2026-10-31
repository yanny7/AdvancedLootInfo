package com.yanny.ali.plugin.server.bytecode;

import java.util.List;
import java.util.Map;

import static com.yanny.ali.plugin.server.bytecode.Dispatcher.*;

record Profile(Map<Phase, List<MethodModel>> models) {
    private static final MethodModel STACKS = new ItemStackModel();
    private static final MethodModel COSTS = new ItemCostModel();

    static final Profile TRADE = new Profile(Map.of(
            Phase.CONSTRUCTOR, List.of(
                    OffersModel.NEW,
                    STACKS,
                    COSTS,
                    new MerchantOfferModel(),
                    NEW_PER_CHOICE,
                    NEW_UNKNOWN
            ),
            Phase.OWNER, List.of(
                    new LoggerModel(),
                    new MapItemModel(),
                    EQUALS_UNKNOWN
            ),
            Phase.UNDISTRIBUTED, List.of(
                    new PickModel(),
                    INT_VALUE,
                    new OptionalModel(),
                    CollectionsModel.REMOVE_CHOICE,
                    OffersModel.COLLECTED
            ),
            Phase.RECEIVER, List.of(
                    new RandomModel(),
                    COSTS,
                    STACKS,
                    new NumberModel(),
                    CLOSURE,
                    new TraderModel(),
                    new SelfModel(),
                    new OffersModel(),
                    DRAWS,
                    new WorldModel(),
                    UNKNOWN,
                    UNSUPPORTED,
                    new MapModel(),
                    NULL_RECEIVER,
                    LAMBDA
            ),
            Phase.JDK, List.of(
                    REQUIRE_NON_NULL,
                    IS_NULL,
                    SELECTION_GUARD,
                    THREAD_LOCAL_RANDOM,
                    NumberModel.STATICS,
                    OffersModel.ORDER,
                    new CollectionsModel(),
                    UNCERTAIN,
                    STATIC_UNKNOWN
            )
    ));

    List<MethodModel> models(Phase phase) {
        return models.get(phase);
    }
}
