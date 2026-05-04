package net.s3lmoni.guaxiorigin.mixin;


import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.apace100.apoli.component.PowerHolderComponent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.s3lmoni.guaxiorigin.power.EdibleItemPower;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Comparator;

@Mixin(ItemInHandRenderer.class)
public class ItemInHandRendererMixin {

    @Shadow
    @Final
    private Minecraft minecraft;

    @ModifyExpressionValue(method = "applyEatTransform", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getUseDuration()I"))
    private int guaxiorigin$customUseDuration(int original, @Local(argsOnly = true) ItemStack itemStack) {
        return PowerHolderComponent.getPowers(this.minecraft.player, EdibleItemPower.class)
                .stream()
                .filter(p -> p.doesApply(itemStack))
                .max(Comparator.comparing(EdibleItemPower::getPriority))
                .map(EdibleItemPower::getConsumeTime)
                .orElse(original);
    }

    @ModifyExpressionValue(method = "renderArmWithItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getUseAnimation()Lnet/minecraft/world/item/UseAnim;"))
    private UseAnim guaxiorigin$customUseAnimation(UseAnim original, @Local(argsOnly = true) AbstractClientPlayer player, @Local(argsOnly = true) ItemStack itemStack) {
        return PowerHolderComponent.getPowers(player, EdibleItemPower.class)
                .stream()
                .filter(p-> p.doesApply(itemStack))
                .max(Comparator.comparing(EdibleItemPower::getPriority))
                .map(EdibleItemPower::getUseAction)
                .orElse(original);
    }
}
