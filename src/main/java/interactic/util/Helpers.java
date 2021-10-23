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
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Vec3d;

import java.util.Collection;
import java.util.Optional;

public class Helpers {

    public static ItemEntity raycastItem(Entity camera, float reach) {
        Vec3d normalizedFacing = camera.getRotationVec(1.0F);
        Vec3d denormalizedFacing = camera.getCameraPosVec(0).add(normalizedFacing.x * reach, normalizedFacing.y * reach, normalizedFacing.z * reach);

        final EntityHitResult result = ProjectileUtil.raycast(camera, camera.getCameraPosVec(0), denormalizedFacing, camera.getBoundingBox().stretch(normalizedFacing.multiply(reach)).expand(1, 1, 1), entity -> entity instanceof ItemEntity, reach * reach);
        return result == null ? null : (ItemEntity) result.getEntity();
    }

    public static boolean canPlayerPickUpItem(PlayerEntity player, ItemStack stack) {
        if (player.isSneaking()) return true;

        if (!InteracticInit.getConfig().autoPickup) return false;
        if (!InteracticInit.getConfig().itemFilterEnabled) return true;

        var filterOptional = ((PlayerInventoryAccessor) player.getInventory()).getCombinedInventory().stream().flatMap(Collection::stream).filter(itemStack -> itemStack.isOf(InteracticInit.ITEM_FILTER)).findFirst();
        if (filterOptional.isEmpty()) return true;

        final ItemStack filterStack = filterOptional.get();
        final NbtCompound filterNbt = filterStack.getOrCreateNbt();

        if (!filterNbt.getBoolean("Enabled")) return true;

        return filterNbt.getBoolean("BlockMode") != ItemFilterItem.getItemsInFilter(filterStack).contains(stack.getItem());
    }

}
