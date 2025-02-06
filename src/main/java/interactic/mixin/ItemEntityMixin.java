package interactic.mixin;

import interactic.InteracticInit;
import interactic.util.Helpers;
import interactic.util.InteracticItemExtensions;
import interactic.util.ItemDamageSource;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.entity.*;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.apache.commons.lang3.mutable.MutableDouble;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin extends Entity implements InteracticItemExtensions {

    @Shadow
    public abstract ItemStack getStack();

    @Shadow
    private int itemAge;

    @Shadow
    @Nullable
    public abstract Entity getOwner();

    @Unique
    private float rotation = -1;

    @Unique
    private boolean wasThrown;

    @Unique
    private boolean wasFullPower;

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
    public void markThrown() {
        this.wasThrown = true;
    }

    @Override
    public void markFullPower() {
        this.wasFullPower = true;
    }

    @Inject(method = "onPlayerCollision", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/ItemEntity;getStack()Lnet/minecraft/item/ItemStack;", ordinal = 0), cancellable = true)
    private void controlPickup(PlayerEntity player, CallbackInfo ci) {
        if (Helpers.canPlayerPickUpItem(player, (ItemEntity) (Object) this)) return;
        ci.cancel();
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void dealThrowingDamage(CallbackInfo ci) {
        if (!InteracticInit.getConfig().itemsActAsProjectiles()) return;
        if (itemAge < 2) return;

        var world = this.getWorld();
        if (world.isClient) return;

        if (this.isOnGround()) this.wasThrown = false;
        if (!this.wasThrown) return;

        final var hasDamageModifiers = this.getStack().getOrDefault(DataComponentTypes.ATTRIBUTE_MODIFIERS, AttributeModifiersComponent.DEFAULT)
                .modifiers().stream().anyMatch(entry -> entry.attribute().value() == EntityAttributes.ATTACK_DAMAGE);
        if (!(this.wasFullPower || hasDamageModifiers)) return;

        var damage = new MutableDouble(2d);
        if (hasDamageModifiers) {
            this.getStack().applyAttributeModifiers(EquipmentSlot.MAINHAND, (attribEntry, modifier) -> {
                if (attribEntry.value() != EntityAttributes.ATTACK_DAMAGE || modifier.operation() != EntityAttributeModifier.Operation.ADD_VALUE) return;
                damage.add(modifier.value());
            });
        }

        final var entities = world.getNonSpectatingEntities(LivingEntity.class, this.getBoundingBox().expand(0.15));
        if (entities.isEmpty()) return;

        final var target = entities.get(0);
        final var damageSource = new ItemDamageSource((ItemEntity) (Object) this, this.getOwner());

        if (target.hurtTime != 0 || target.isInvulnerableTo((ServerWorld) world, damageSource)) return;

        target.damage((ServerWorld) world, damageSource, damage.floatValue());
        this.getStack().damage(1, (ServerWorld) world, null, item -> this.discard());
    }

    @Override
    public float getTargetingMargin() {
        return .2f;
    }
}
