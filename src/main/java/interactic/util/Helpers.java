package interactic.util;

import interactic.InteracticInit;
import interactic.itemfilter.ItemFilterItem;
import interactic.mixin.PlayerInventoryAccessor;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Vec3d;

import java.util.Collection;
import java.util.List;

public class Helpers {

    public static ItemEntity raycastItem(Entity camera, float reach) {
        Vec3d normalizedFacing = camera.getRotationVec(1.0F);
        Vec3d denormalizedFacing = camera.getCameraPosVec(0).add(normalizedFacing.x * reach, normalizedFacing.y * reach, normalizedFacing.z * reach);

        final EntityHitResult result = ProjectileUtil.raycast(camera, camera.getCameraPosVec(0), denormalizedFacing,
                camera.getBoundingBox().stretch(normalizedFacing.multiply(reach)).expand(1), entity -> entity instanceof ItemEntity, reach * reach);

        if (result != null) {
            var distance = camera.getPos().distanceTo(result.getPos()) - .3;
            if (camera.raycast(distance, 1f, false) instanceof BlockHitResult blockResult) {
                if (!camera.getWorld().getBlockState(blockResult.getBlockPos()).getCollisionShape(camera.getWorld(), blockResult.getBlockPos()).isEmpty()) {
                    return null;
                }
            }
        }

        return result == null ? null : (ItemEntity) result.getEntity();
    }

    public static boolean canPlayerPickUpItem(PlayerEntity player, ItemEntity item) {
        if (!InteracticInit.getConfig().autoPickup() && player.isSneaking() && !item.getCommandTags().contains("interactic.ignore_auto_pickup_rule")) {
            return true;
        }

        if (!InteracticInit.getConfig().itemFilterEnabled()) return true;
        var filters = ((PlayerInventoryAccessor) player.getInventory()).getCombinedInventory().stream()
                .flatMap(Collection::stream)
                .filter(stack -> stack.isOf(InteracticInit.getItemFilter()))
                .filter(stack -> stack.getOrDefault(ItemFilterItem.ENABLED, false))
                .map(stack -> new FilterEntry(stack, ItemFilterItem.getItemsInFilter(stack), stack.getOrDefault(ItemFilterItem.BLOCK_MODE, false)))
                .toList();

        if (filters.isEmpty()) return true;

        var allowed = filters.stream().allMatch(FilterEntry::blockMode);
        for (var entry : filters) {
            if (entry.blockMode) continue;

            if (entry.filterItems.contains(item.getStack().getItem())) {
                return true;
            }
        }

        if (!allowed) return false;

        for (var entry : filters) {
            if (!entry.blockMode) continue;

            if (entry.filterItems.contains(item.getStack().getItem())) {
                return false;
            }
        }

        return true;
    }

    private record FilterEntry(ItemStack filter, List<Item> filterItems, boolean blockMode) {}
}
