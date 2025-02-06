package interactic.mixin;

import interactic.InteracticClientInit;
import interactic.InteracticInit;
import interactic.util.Helpers;
import interactic.util.InteracticNetworking;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {

    @Unique
    private float dropPower = 0.9f;

    @Shadow
    @Nullable
    public Entity cameraEntity;

    @Shadow
    @Nullable
    public ClientPlayerEntity player;

    @Shadow
    @Final
    public GameOptions options;

    @Inject(method = "doItemUse", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;isRiding()Z", shift = At.Shift.AFTER), cancellable = true)
    private void tryPickupItem(CallbackInfo ci) {
        if (!InteracticInit.getConfig().rightClickPickup()) return;
        if (KeyBindingHelper.getBoundKeyOf(InteracticClientInit.PICKUP_ITEM) != InputUtil.UNKNOWN_KEY) return;

        if (Helpers.raycastItem(cameraEntity, (float) this.player.getAttributeValue(EntityAttributes.BLOCK_INTERACTION_RANGE)) == null) return;
        InteracticNetworking.CHANNEL.clientHandle().send(new InteracticNetworking.Pickup());
        this.player.swingHand(Hand.MAIN_HAND);
        ci.cancel();
    }

    @Redirect(method = "handleInputEvents", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;dropSelectedItem(Z)Z"))
    private boolean handleDropPower(ClientPlayerEntity clientPlayerEntity, boolean dropEntireStack) {
        if (!InteracticInit.getConfig().itemThrowing()) return clientPlayerEntity.dropSelectedItem(dropEntireStack);

        if (!Screen.hasShiftDown()) {
            dropPower += 0.075f;
            if (dropPower > 5) dropPower = 5;
            if (dropPower >= 1.5)
                clientPlayerEntity.sendMessage(Text.of("Power: " + BigDecimal.valueOf(Math.max(dropPower, 1)).setScale(1, RoundingMode.HALF_UP)), true);
            return false;
        } else {
            return clientPlayerEntity.dropSelectedItem(dropEntireStack);
        }
    }

    @Redirect(method = "handleInputEvents", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;swingHand(Lnet/minecraft/util/Hand;)V"))
    private void dontSwingArms(ClientPlayerEntity player, Hand hand) {
        if (!InteracticInit.getConfig().swingArm()) return;
        player.swingHand(hand);
    }

    @Inject(method = "handleInputEvents", at = @At("RETURN"))
    private void afterDrop(CallbackInfo ci) {
        if (!InteracticInit.getConfig().itemThrowing()) return;

        if (dropPower > 0.9f && !options.dropKey.isPressed()) {
            final var dropAll = Screen.hasControlDown();

            if (dropPower >= 1.5) {
                InteracticNetworking.CHANNEL.clientHandle().send(new InteracticNetworking.DropWithPower(dropPower, dropAll));

                if (!this.player.getInventory().removeStack(this.player.getInventory().selectedSlot, dropAll && !this.player.getInventory().getMainHandStack().isEmpty() ? this.player.getInventory().getMainHandStack().getCount() : 1).isEmpty()) {
                    if (InteracticInit.getConfig().swingArm()) this.player.swingHand(Hand.MAIN_HAND);
                }
            } else if (this.player.dropSelectedItem(dropAll)) {
                if (InteracticInit.getConfig().swingArm()) this.player.swingHand(Hand.MAIN_HAND);
            }

            dropPower = 0.9f;
        }
    }

}
