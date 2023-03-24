package interactic.util;

import interactic.InteracticInit;
import interactic.ItemFilterItem;
import interactic.mixin.PlayerInventoryAccessor;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Vec3d;

import java.util.Collection;

public class Helpers {

    public static ItemEntity raycastItem(Entity camera, float reach) {
        Vec3d normalizedFacing = camera.getRotationVec(1.0F);
        Vec3d denormalizedFacing = camera.getCameraPosVec(0).add(normalizedFacing.x * reach, normalizedFacing.y * reach, normalizedFacing.z * reach);

        final EntityHitResult result = ProjectileUtil.raycast(camera, camera.getCameraPosVec(0), denormalizedFacing,
                camera.getBoundingBox().stretch(normalizedFacing.multiply(reach)).expand(1), entity -> entity instanceof ItemEntity, reach * reach);

        if (result != null) {
            var distance = camera.getPos().distanceTo(result.getPos()) - .3;
            if (camera.raycast(distance, 1f, false) instanceof BlockHitResult blockResult) {
                if (!camera.world.getBlockState(blockResult.getBlockPos()).getCollisionShape(camera.world, blockResult.getBlockPos()).isEmpty()) {
                    return null;
                }
            }
        }

        return result == null ? null : (ItemEntity) result.getEntity();
    }

    public static boolean canPlayerPickUpItem(PlayerEntity player, ItemEntity item) {
        if (player.isSneaking()) return true;

        if (!InteracticInit.getConfig().autoPickup() && !item.getCommandTags().contains("interactic.ignore_auto_pickup_rule")) return false;
        if (!InteracticInit.getConfig().itemFilterEnabled()) return true;

        var filterOptional = ((PlayerInventoryAccessor) player.getInventory()).getCombinedInventory().stream().flatMap(Collection::stream).filter(itemStack -> itemStack.isOf(InteracticInit.getItemFilter())).findFirst();
        if (filterOptional.isEmpty()) return true;

        final ItemStack filterStack = filterOptional.get();
        final NbtCompound filterNbt = filterStack.getOrCreateNbt();

        if (!filterNbt.getBoolean("Enabled")) return true;

        return filterNbt.getBoolean("BlockMode") != ItemFilterItem.getItemsInFilter(filterStack).contains(item.getStack().getItem());
    }

}
