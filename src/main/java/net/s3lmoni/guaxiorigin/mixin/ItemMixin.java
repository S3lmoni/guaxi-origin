package net.s3lmoni.guaxiorigin.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.apace100.apoli.component.PowerHolderComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.s3lmoni.guaxiorigin.power.EdibleItemPower;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Comparator;

@Mixin(Item.class)
public class ItemMixin {

    @ModifyExpressionValue(method = "use", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/Item;isEdible()Z"))
    private boolean guaxiorigin$customIsEdible(boolean original, @Local(argsOnly = true) Player player, @Local(argsOnly = true) InteractionHand hand) {
        return original || PowerHolderComponent.hasPower(player, EdibleItemPower.class, p -> p.doesApply(player.getItemInHand(hand)));
    }

    @ModifyExpressionValue(method = "use", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/Item;getFoodProperties()Lnet/minecraft/world/food/FoodProperties;"))
    private FoodProperties guaxiorigin$customFoodProperties(FoodProperties original, @Local(argsOnly = true) Player player, @Local(argsOnly = true) InteractionHand interactionHand)  {
        return PowerHolderComponent.getPowers(player, EdibleItemPower.class)
                                    .stream()
                                    .filter(p -> p.doesApply(player.getItemInHand(interactionHand)))
                                    .max(Comparator.comparing(EdibleItemPower::getPriority))
                                    .map(EdibleItemPower::getFoodComponent)
                                    .orElse(original != null ? original : new FoodProperties.Builder().build());
    }

    @ModifyExpressionValue(method = "finishUsingItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/Item;isEdible()Z"))
    private boolean guaxiorigin$customFinishUsingIsEdible(boolean original, @Local(argsOnly = true) LivingEntity entity, @Local(argsOnly = true) ItemStack itemStack) {
        if(entity instanceof Player player && !original) {
            return PowerHolderComponent.hasPower(player, EdibleItemPower.class, p-> p.doesApply(itemStack));
        }

        return original;
    }
}
