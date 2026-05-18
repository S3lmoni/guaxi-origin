package net.s3lmoni.guaxiorigin.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.apace100.apoli.component.PowerHolderComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.s3lmoni.guaxiorigin.power.EdibleItemPower;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Comparator;

@Mixin(value = Item.class)
abstract public class ItemMixin {

    @ModifyExpressionValue(
            method = "use",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/Item;isEdible()Z"
            ),
            require = 0
    )
    private boolean guaxiorigin$fabricIsEdible(boolean original, @Local(argsOnly = true) Player player, @Local(argsOnly = true) InteractionHand hand) {
        return original ||
                PowerHolderComponent.hasPower(player, EdibleItemPower.class, p -> p.doesApply(player.getItemInHand(hand)));
    }

    @ModifyExpressionValue(
            method = "use",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemStack;isEdible()Z"
            ),
            require = 0
    )
    private boolean guaxiorigin$forgeIsEdible(boolean original, @Local(argsOnly = true) Player player, @Local(argsOnly = true) InteractionHand hand) {
        return original ||
                PowerHolderComponent.hasPower(player, EdibleItemPower.class, p -> p.doesApply(player.getItemInHand(hand)));
    }

    @ModifyExpressionValue(method = "use", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/Item;getFoodProperties()Lnet/minecraft/world/food/FoodProperties;"))
    private FoodProperties guaxiorigin$customFoodProperties(FoodProperties original, @Local(argsOnly = true) Player player, @Local(argsOnly = true) InteractionHand interactionHand)  {
        return PowerHolderComponent.getPowers(player, EdibleItemPower.class)
                                    .stream()
                                    .filter(p -> p.doesApply(player.getItemInHand(interactionHand)))
                                    .max(Comparator.comparing(EdibleItemPower::getPriority))
                                    .map(EdibleItemPower::getFoodComponent)
                                    .orElse(original);
    }

    @ModifyExpressionValue(method = "finishUsingItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/Item;isEdible()Z"))
    private boolean guaxiorigin$customFinishUsingIsEdible(boolean original, @Local(argsOnly = true) LivingEntity entity, @Local(argsOnly = true) ItemStack itemStack) {
        if(entity instanceof Player player && !original) {
            return PowerHolderComponent.hasPower(player, EdibleItemPower.class, p-> p.doesApply(itemStack));
        }

        return original;
    }
}
