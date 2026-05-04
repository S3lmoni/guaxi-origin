/*
* Class logic adapted from apoli
* Licensed under the MIT License
* Copyright (c) 2021 apace100
*/

package net.s3lmoni.guaxiorigin.power;

import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.util.Tuple;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.s3lmoni.guaxiorigin.GuaxiOrigin;

import java.util.function.Consumer;
import java.util.function.Predicate;

public class ActionOnDeathPower extends Power {
    private final Consumer<Tuple<Entity, Entity>> biEntityAction;
    private final Predicate<Tuple<Entity, Entity>> biEntityCondition;
    private final Predicate<Tuple<DamageSource, Float>> damageCondition;

    public ActionOnDeathPower(PowerType<?> type, LivingEntity entity,
                              Consumer<Tuple<Entity, Entity>> biEntityAction,
                              Predicate<Tuple<Entity, Entity>> biEntityCondition,
                              Predicate<Tuple<DamageSource, Float>> damageCondition) {
        super(type, entity);

        this.biEntityAction = biEntityAction;
        this.biEntityCondition = biEntityCondition;
        this.damageCondition = damageCondition;
    }

    public boolean doesApply(Entity actor, DamageSource damageSource, Float amountDamage) {
        return (biEntityCondition == null || biEntityCondition.test(new Tuple<>(actor, entity)))
                && (damageCondition == null || damageCondition.test(new Tuple<>(damageSource, amountDamage)));
    }

    public void executeOnDeath(Entity actor) {
        biEntityAction.accept(new Tuple<>(actor, entity));
    }

    public static PowerFactory<Power> createFactory() {
        return new PowerFactory<>(
                GuaxiOrigin.identifier("action_on_death"),
                new SerializableData()
                        .add("bientity_action", ApoliDataTypes.BIENTITY_ACTION)
                        .add("bientity_condition", ApoliDataTypes.BIENTITY_CONDITION, null)
                        .add("damage_condition", ApoliDataTypes.DAMAGE_CONDITION, null),
                data -> ((powerPowerType, livingEntity) -> new ActionOnDeathPower(
                        powerPowerType,
                        livingEntity,
                        data.get("bientity_action"),
                        data.get("bientity_condition"),
                        data.get("damage_condition")
                ))
        ).allowCondition();
    }
}
