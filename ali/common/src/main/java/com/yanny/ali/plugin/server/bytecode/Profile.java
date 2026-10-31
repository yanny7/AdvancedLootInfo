package com.yanny.ali.plugin.server.bytecode;

import java.util.List;
import java.util.Map;

import static com.yanny.ali.plugin.server.bytecode.Dispatcher.*;

record Profile(Map<Phase, List<MethodModel>> models) {
    private static final MethodModel STACKS = new ItemStackModel();
    private static final MethodModel LOGGER = new LoggerModel();
    private static final MethodModel OPTIONAL = new OptionalModel();
    private static final MethodModel RANDOM = new RandomModel();
    private static final MethodModel NUMBERS = new NumberModel();
    private static final MethodModel SELF = new SelfModel();
    private static final MethodModel WORLD = new WorldModel();
    private static final MethodModel MAPS = new MapModel();
    private static final MethodModel COLLECTIONS = new CollectionsModel();

    static final Profile TRADE = new Profile(Map.of(
            Phase.CONSTRUCTOR, List.of(
                    OffersModel.NEW,
                    STACKS,
                    new MerchantOfferModel(),
                    NEW_PER_CHOICE,
                    NEW_UNKNOWN
            ),
            Phase.OWNER, List.of(
                    LOGGER,
                    new MapItemModel(),
                    EQUALS_UNKNOWN
            ),
            Phase.UNDISTRIBUTED, List.of(
                    new PickModel(),
                    INT_VALUE,
                    OPTIONAL,
                    CollectionsModel.REMOVE_CHOICE,
                    OffersModel.COLLECTED
            ),
            Phase.RECEIVER, List.of(
                    RANDOM,
                    STACKS,
                    NUMBERS,
                    CLOSURE,
                    new TraderModel(),
                    SELF,
                    new OffersModel(),
                    DRAWS,
                    WORLD,
                    UNKNOWN,
                    UNSUPPORTED,
                    MAPS,
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
                    COLLECTIONS,
                    UNCERTAIN,
                    STATIC_UNKNOWN
            )
    ));

    static final Profile LOOT = new Profile(Map.of(
            Phase.CONSTRUCTOR, List.of(
                    NEW_PER_CHOICE,
                    NEW_UNKNOWN
            ),
            Phase.OWNER, List.of(
                    LOGGER,
                    EQUALS_UNKNOWN
            ),
            Phase.UNDISTRIBUTED, List.of(
                    INT_VALUE,
                    OPTIONAL,
                    CollectionsModel.REMOVE_CHOICE
            ),
            Phase.RECEIVER, List.of(
                    RANDOM,
                    NUMBERS,
                    CLOSURE,
                    new EntityModel(),
                    new LootTableModel(),
                    new ForeignReceiverModel(),
                    SELF,
                    DRAWS,
                    WORLD,
                    UNKNOWN,
                    UNSUPPORTED,
                    MAPS,
                    NULL_RECEIVER,
                    LAMBDA
            ),
            Phase.JDK, List.of(
                    REQUIRE_NON_NULL,
                    IS_NULL,
                    SELECTION_GUARD,
                    THREAD_LOCAL_RANDOM,
                    NumberModel.STATICS,
                    COLLECTIONS,
                    UNCERTAIN,
                    STATIC_UNKNOWN
            )
    ));

    List<MethodModel> models(Phase phase) {
        return models.get(phase);
    }
}
