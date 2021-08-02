package interactic.mixin;

import interactic.InteracticInit;
import interactic.util.Helpers;
import interactic.util.InteracticItemExtensions;
import net.minecraft.entity.*;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin extends Entity implements InteracticItemExtensions {

    @Shadow
    public abstract ItemStack getStack();

    @Shadow
    @Nullable
    public abstract UUID getOwner();

    @Unique
    private float rotation;

    @Unique
    private boolean wasThrown;

    private ItemEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Override
    public float getRotation() {
        return rotation;
    }

    @Override
    public void setRotation(float rotation) {
        this.rotation = rotation;
    }

    @Override
    public boolean getWasThrown() {
        return wasThrown;
    }

    @Override
    public void markThrown() {
        this.wasThrown = true;
    }

    @Inject(method = "onPlayerCollision", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/ItemEntity;getStack()Lnet/minecraft/item/ItemStack;", ordinal = 0), cancellable = true)
    private void controlPickup(PlayerEntity player, CallbackInfo ci) {
        if (Helpers.canPlayerPickUpItem(player, getStack())) return;
        ci.cancel();
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void dealThrowingDamage(CallbackInfo ci) {
        if(!InteracticInit.getConfig().itemsActAsProjectiles) return;

        if (world.isClient) return;

        if (this.isOnGround()) this.wasThrown = false;
        if (!this.wasThrown) return;

        if (!this.getStack().getAttributeModifiers(EquipmentSlot.MAINHAND).containsKey(EntityAttributes.GENERIC_ATTACK_DAMAGE)) return;
        final double damage = this.getStack().getAttributeModifiers(EquipmentSlot.MAINHAND).get(EntityAttributes.GENERIC_ATTACK_DAMAGE)
                .stream().filter(modifier -> modifier.getOperation() == EntityAttributeModifier.Operation.ADDITION)
                .mapToDouble(EntityAttributeModifier::getValue).sum();

        final var entities = world.getNonSpectatingEntities(LivingEntity.class, this.getBoundingBox().expand(0.15));
        if (entities.size() < 1) return;

        final var target = entities.get(0);
        target.damage(DamageSource.thrownProjectile(this, ((ServerWorld) world).getEntity(this.getOwner())), (float) damage);
    }
}
