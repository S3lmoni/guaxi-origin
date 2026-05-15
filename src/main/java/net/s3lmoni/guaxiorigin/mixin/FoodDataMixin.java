package net.s3lmoni.guaxiorigin.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.apace100.apoli.component.PowerHolderComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.s3lmoni.guaxiorigin.power.EdibleItemPower;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Comparator;

@Mixin(FoodData.class)
public abstract class FoodDataMixin {

    @Unique
    private Player guaxiorigin$cachedPlayer;

    @Inject(method = "tick", at = @At(value = "HEAD"))
    private void guaxiorigin$getPlayer(Player player, CallbackInfo ci) {
        this.guaxiorigin$cachedPlayer = player;
    }

    @ModifyExpressionValue(method = "eat(Lnet/minecraft/world/item/Item;Lnet/minecraft/world/item/ItemStack;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/Item;isEdible()Z"))
    private boolean guaxiorigin$customIsEdible(boolean original, @Local(argsOnly = true) ItemStack itemStack) {
        return original
                || PowerHolderComponent.hasPower(guaxiorigin$cachedPlayer, EdibleItemPower.class, power -> power.doesApply(itemStack));
    }

    @ModifyExpressionValue(method = "eat(Lnet/minecraft/world/item/Item;Lnet/minecraft/world/item/ItemStack;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/Item;getFoodProperties()Lnet/minecraft/world/food/FoodProperties;"))
    private FoodProperties guaxiorigin$customFoodProperties(FoodProperties original, @Local(argsOnly = true) ItemStack item) {
        return PowerHolderComponent.getPowers(guaxiorigin$cachedPlayer, EdibleItemPower.class)
                .stream()
                .filter(p -> p.doesApply(item))
                .max(Comparator.comparing(EdibleItemPower::getPriority))
                .map(EdibleItemPower::getFoodComponent)
                .orElse(original);
    }
}
