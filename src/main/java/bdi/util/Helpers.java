package bdi.util;

import bdi.BdiInit;
import bdi.ItemFilterItem;
import bdi.mixin.PlayerInventoryAccessor;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;

import java.util.Collection;

public class Helpers {

    public static ItemEntity raycastItem(Entity camera, float reach) {
        Vec3d normalizedFacing = camera.getRotationVec(1.0F);
        Vec3d denormalizedFacing = camera.getCameraPosVec(0).add(normalizedFacing.x * reach, normalizedFacing.y * reach, normalizedFacing.z * reach);

        final var result = ProjectileUtil.raycast(camera, camera.getCameraPosVec(0), denormalizedFacing, camera.getBoundingBox().stretch(normalizedFacing.multiply(reach)).expand(1, 1, 1), entity -> entity instanceof ItemEntity, reach * reach);
        return result == null ? null : (ItemEntity) result.getEntity();
    }

    public static boolean canPlayerPickUpItem(PlayerEntity player, ItemStack stack) {
        if (player.isSneaking()) return true;

        if (!BdiInit.getConfig().autoPickup) return false;
        if (!BdiInit.getConfig().itemFilterEnabled) return true;

        var filterOptional = ((PlayerInventoryAccessor) player.getInventory()).getCombinedInventory().stream().flatMap(Collection::stream).filter(itemStack -> itemStack.isOf(BdiInit.ITEM_FILTER)).findFirst();
        if (filterOptional.isEmpty()) return true;

        final var filterStack = filterOptional.get();

        if (!filterStack.getOrCreateTag().getBoolean("Enabled")) return true;

        return ItemFilterItem.getWhitelist(filterStack).contains(stack.getItem());
    }

}
