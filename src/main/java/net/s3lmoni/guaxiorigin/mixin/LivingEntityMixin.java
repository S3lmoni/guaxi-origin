package net.s3lmoni.guaxiorigin.mixin;


import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.apace100.apoli.component.PowerHolderComponent;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.s3lmoni.guaxiorigin.power.ActionOnDeathPower;
import net.s3lmoni.guaxiorigin.power.EdibleItemPower;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Comparator;
import java.util.Optional;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {

    @Shadow
    protected ItemStack useItem;

    @Unique
    private final FoodProperties FOODPROP_FALLBACK = new FoodProperties.Builder().build(); /* to avoid returning null */

    @Unique
    private LivingEntity getSelf() {
        return (LivingEntity) (Object) this;
    }

    @Unique
    private Optional<EdibleItemPower> getEdibleItemPower(ItemStack itemStack) {
        return PowerHolderComponent.getPowers(getSelf(), EdibleItemPower.class)
                .stream()
                .filter(p -> p.doesApply(itemStack))
                .max(Comparator.comparing(EdibleItemPower::getPriority));
    }

    @Unique
    private UseAnim getCustomUseAction(ItemStack itemStack) {
        return getEdibleItemPower(itemStack)
                .filter(p -> p.doesApply(itemStack))
                .map(EdibleItemPower::getUseAction)
                .orElse(UseAnim.NONE);
    }

    @Unique
    private int getCustomConsumeTime(int original, ItemStack itemStack) {
        return getEdibleItemPower(itemStack).map(EdibleItemPower::getConsumeTime).orElse(original);
    }

    // [EdibleItemPower] triggerItemUseEffects() -> Custom Use Action
    @ModifyExpressionValue(method = "triggerItemUseEffects", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getUseAnimation()Lnet/minecraft/world/item/UseAnim;", ordinal = 0))
    private UseAnim guaxiorigin$useActionDrink(UseAnim original, @Local(argsOnly = true) ItemStack itemStack) {
        return getCustomUseAction(itemStack);
    }

    @ModifyExpressionValue(method = "triggerItemUseEffects", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getUseAnimation()Lnet/minecraft/world/item/UseAnim;", ordinal = 1))
    private UseAnim guaxiorigin$useActionEat(UseAnim original, @Local(argsOnly = true) ItemStack itemStack) {
        return getCustomUseAction(itemStack);
    }

    // [EdibleItemPower] eat() -> IsEdible
    @ModifyExpressionValue(method = "eat", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;isEdible()Z"))
    private boolean guaxiorigin$modifyEatIsEdible(boolean original, @Local(argsOnly = true) ItemStack itemStack) {
        return original || PowerHolderComponent.hasPower(getSelf(), EdibleItemPower.class, p-> p.doesApply(itemStack));
    }

    // [EdibleItemPower] eat() -> IsTheSameItem
    @WrapOperation(method = "eat", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;shrink(I)V"))
    private void guaxiorigin$preventShrink(ItemStack itemStack, int i, Operation<Void> instance) {
        ItemStack resultStack = getEdibleItemPower(itemStack)
                .map(EdibleItemPower::getResultStack)
                .filter(rstack -> ItemStack.isSameItem(rstack, itemStack))
                .orElse(null);

        if(resultStack == null || resultStack.isEmpty()) {
            instance.call(itemStack, i);
        }
    }

    // [EdibleItemPower] addEatEffect() -> IsEdible
    @ModifyExpressionValue(method = "eat", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;isEdible()Z"))
    private boolean guaxiorigin$modifyEatEffectsIsEdible(boolean original, @Local(argsOnly = true) ItemStack itemStack) {
        return original || PowerHolderComponent.hasPower(getSelf(), EdibleItemPower.class, p-> p.doesApply(itemStack));
    }

    // [EdibleItemPower] addEatEffect() -> getFoodProperties()
    @ModifyExpressionValue(method = "addEatEffect", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/Item;getFoodProperties()Lnet/minecraft/world/food/FoodProperties;"))
    private FoodProperties guaxiorigin$customFoodProperties(FoodProperties original, @Local(argsOnly = true) ItemStack itemStack)  {
        return getEdibleItemPower(itemStack)
                .map(EdibleItemPower::getFoodComponent)
                .orElse(original != null ? original : FOODPROP_FALLBACK);
    }

    // [EdibleItemPower] customUseDuration
    @ModifyExpressionValue(method = "shouldTriggerItemUseEffects", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/Item;getFoodProperties()Lnet/minecraft/world/food/FoodProperties;"))
    private FoodProperties guaxiorigin$replaceFoodProperties(FoodProperties original) {
        return getEdibleItemPower(this.useItem)
                .map(EdibleItemPower::getFoodComponent)
                .orElse(original != null ? original : FOODPROP_FALLBACK);
    }

    @ModifyExpressionValue(method = "shouldTriggerItemUseEffects", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getUseDuration()I"))
    private int guaxiorigin$useDurationTriggerItem(int original) {
        return getCustomConsumeTime(original, this.useItem);
    }

    @WrapOperation(method = "startUsingItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getUseDuration()I"))
    private int guaxiorigin$useDurationStartUsing(ItemStack itemStack, Operation<Integer> instance) {
        return getCustomConsumeTime(instance.call(itemStack), itemStack);
    }

    @WrapOperation(method = "onSyncedDataUpdated", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getUseDuration()I"))
    private int guaxiorigin$useDurationSyncedData(ItemStack itemStack, Operation<Integer> instance) {
        return getCustomConsumeTime(instance.call(itemStack), itemStack);
    }

    // [EdibleItemPower] Custom Eating Sound
    @WrapOperation(method = "getEatingSound", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getEatingSound()Lnet/minecraft/sounds/SoundEvent;"))
    private SoundEvent guaxiorigin$customEatingSound(ItemStack itemStack, Operation<SoundEvent> original) {
        return getEdibleItemPower(itemStack)
                .map(EdibleItemPower::getUseSound)
                .orElse(original.call(itemStack));
    }

    // [EdibleItemPower] Custom Drinking Sound
    @WrapOperation(method = "getDrinkingSound", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getDrinkingSound()Lnet/minecraft/sounds/SoundEvent;"))
    private SoundEvent guaxiorigin$customDrinkingSound(ItemStack itemStack, Operation<SoundEvent> original) {
        return getEdibleItemPower(itemStack)
                .map(EdibleItemPower::getUseSound)
                .orElse(original.call(itemStack));
    }

    /*
    * Method logic adapted from apoli
    * Licensed under the MIT License
    * Copyright (c) 2021 apace100
    */
    @Inject(method = "hurt", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;die(Lnet/minecraft/world/damagesource/DamageSource;)V"))
    private void guaxiorigin$executeDeathAction(DamageSource damageSource, float amount, CallbackInfoReturnable<Boolean> cir) {
        PowerHolderComponent.withPowers(getSelf(), ActionOnDeathPower.class,
                p -> p.doesApply(damageSource.getEntity(), damageSource, amount),
                p -> p.executeOnDeath(damageSource.getEntity()));
    }
}
