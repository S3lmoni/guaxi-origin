package net.s3lmoni.guaxiorigin.power;

import com.mojang.datafixers.util.Pair;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.Prioritized;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.s3lmoni.guaxiorigin.GuaxiOrigin;

import java.util.function.Consumer;
import java.util.function.Predicate;

public class EdibleItemPower extends Power implements Prioritized<EdibleItemPower> {
    private final Consumer<Entity> entityAction;
    private final Consumer<Tuple<Level, ItemStack>> itemAction;
    private final ItemStack resultStack;
    private final Consumer<Pair<Level, ItemStack>> resultItemAction;
    private final Predicate<ItemStack> itemCondition;
    private final FoodProperties foodComponent;
    private final UseAnim useAction;
    private final SoundEvent useSound;
    private final int priority;

    public EdibleItemPower(PowerType<?> type, LivingEntity entity,
                           Consumer<Entity> entityAction,
                           Consumer<Tuple<Level, ItemStack>> itemAction,
                           Predicate<ItemStack> itemCondition,
                           ItemStack resultStack,
                           Consumer<Pair<Level, ItemStack>> resultItemAction,
                           FoodProperties foodComponent,
                           UseAnim useAction,
                           SoundEvent useSound,
                           int priority
    ) {
        super(type, entity);

        this.entityAction = entityAction;
        this.itemAction = itemAction;
        this.itemCondition = itemCondition;
        this.resultStack = resultStack;
        this.resultItemAction = resultItemAction;
        this.foodComponent = foodComponent;
        this.useAction = useAction;
        this.useSound = useSound;
        this.priority = priority;
    }

    @Override
    public int getPriority() {
        return priority;
    }

    public boolean doesApply(ItemStack itemStack) {
        return itemCondition == null || itemCondition.test(itemStack);
    }

    public FoodProperties getFoodComponent() {
        return foodComponent;
    }

    public UseAnim getUseAction() {
        return useAction;
    }

    public Integer getConsumeTime() {
        return this.getFoodComponent().isFastFood() ? 16 : 32;
    }

    public ItemStack getResultStack() {
        return resultStack;
    }

    public SoundEvent getUseSound() {
        return useSound;
    }

    public void executeEntityAction() {
        if(entityAction != null) {
            entityAction.accept(entity);
        }
    }

    public void executeItemAction(Level level, ItemStack stack) {
        if(itemAction != null && stack != null) {
            itemAction.accept(new Tuple<>(level, stack));
        }

        if(resultStack != null && resultItemAction != null) {
            resultItemAction.accept(new Pair<>(level, resultStack));
        }
    }

    public static PowerFactory<Power> createFactory() {
        return new PowerFactory<>(
                GuaxiOrigin.identifier("edible_item"),
                new SerializableData()
                        .add("entity_action", ApoliDataTypes.ENTITY_ACTION, null)
                        .add("item_action", ApoliDataTypes.ITEM_ACTION, null)
                        .add("item_condition", ApoliDataTypes.ITEM_CONDITION, null)
                        .add("return_stack", SerializableDataTypes.ITEM_STACK, null)
                        .add("result_item_action", ApoliDataTypes.ITEM_ACTION, null)
                        .add("food_component", SerializableDataTypes.FOOD_COMPONENT)
                        .add("use_action", SerializableDataType.enumValue(UseAnim.class), UseAnim.EAT)
                        .add("sound", SerializableDataTypes.SOUND_EVENT, SoundEvents.GENERIC_EAT)
                        .add("priority", SerializableDataTypes.INT, 0),
                data -> (powerType, livingEntity) -> new EdibleItemPower(
                        powerType,
                        livingEntity,
                        data.get("entity_action"),
                        data.get("item_action"),
                        data.get("item_condition"),
                        data.get("return_stack"),
                        data.get("result_item_action"),
                        data.get("food_component"),
                        data.get("use_action"),
                        data.get("sound"),
                        data.get("priority")
                )
        ).allowCondition();
    }
}
