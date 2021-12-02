package interactic.mixin;

import interactic.InteracticInit;
import interactic.util.InteracticItemExtensions;
import interactic.util.InteracticPlayerExtension;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixin implements InteracticPlayerExtension {

    @Unique
    private float dropPower = 1;

    @Override
    public void setDropPower(float power) {
        this.dropPower = power;
    }

    @Inject(method = "dropItem(Lnet/minecraft/item/ItemStack;ZZ)Lnet/minecraft/entity/ItemEntity;", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/ItemEntity;setVelocity(DDD)V", shift = At.Shift.AFTER), locals = LocalCapture.CAPTURE_FAILHARD)
    private void applyDropPower(ItemStack stack, boolean throwRandomly, boolean retainOwnership, CallbackInfoReturnable<ItemEntity> cir, double d, ItemEntity item) {
        if (!InteracticInit.getConfig().itemThrowing) return;

        if (dropPower > 1) {
            var velocity = item.getVelocity();
            velocity = velocity.multiply(dropPower * 1.5f);

            double yV = MathHelper.sin((((PlayerEntity) (Object) this).getPitch() - 5) * MathHelper.RADIANS_PER_DEGREE);
            velocity = new Vec3d(velocity.x, -yV, velocity.z);

            item.setVelocity(velocity);

            item.updatePosition(item.getX(), ((PlayerEntity) (Object) this).getEyeY(), item.getZ());

            if (retainOwnership) {
                ((InteracticItemExtensions) item).markThrown();
                if (dropPower >= 5) ((InteracticItemExtensions) item).markFullPower();
            }

            dropPower = 1;
        }
    }
}
