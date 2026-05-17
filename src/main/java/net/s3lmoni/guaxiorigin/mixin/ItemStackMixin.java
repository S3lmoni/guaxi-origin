package net.s3lmoni.guaxiorigin.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.apace100.apoli.component.PowerHolderComponent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.s3lmoni.guaxiorigin.power.EdibleItemPower;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Comparator;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {

    @WrapOperation(method = "finishUsingItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/Item;finishUsingItem(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;)Lnet/minecraft/world/item/ItemStack;"))
    private ItemStack guaxiorigin$customFinishEating(Item instance, ItemStack itemStack, Level world, LivingEntity livingEntity, Operation<ItemStack> original) {
        EdibleItemPower power = PowerHolderComponent.getPowers(livingEntity, EdibleItemPower.class)
                .stream()
                .filter(p -> p.doesApply(itemStack))
                .max(Comparator.comparing(EdibleItemPower::getPriority))
                .orElse(null);

        if(power == null) {
            return original.call(instance, itemStack, world, livingEntity);
        }

        // Entity Action
        power.executeEntityAction();

        // Item Action
        ItemStack reference = livingEntity.eat(world, itemStack);
        power.executeItemAction(world, reference);

        // Result Stack
        ItemStack resultStack = power.getResultStack();
        if(resultStack != null && !resultStack.isEmpty()) {
            if (livingEntity instanceof Player player) {
                player.getInventory().placeItemBackInInventory(resultStack.copy());
            }
        }

        return reference;
    }
}
