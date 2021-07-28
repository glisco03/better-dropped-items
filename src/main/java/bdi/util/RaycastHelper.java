package bdi.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.util.math.Vec3d;

public class RaycastHelper {

    public static ItemEntity raycastItem(Entity camera, float reach) {
        Vec3d normalizedFacing = camera.getRotationVec(1.0F);
        Vec3d denormalizedFacing = camera.getCameraPosVec(0).add(normalizedFacing.x * reach, normalizedFacing.y * reach, normalizedFacing.z * reach);

        final var result = ProjectileUtil.raycast(camera, camera.getCameraPosVec(0), denormalizedFacing, camera.getBoundingBox().stretch(normalizedFacing.multiply(reach)).expand(1, 1, 1), entity -> entity instanceof ItemEntity, reach*reach);
        return result == null ? null : (ItemEntity) result.getEntity();
    }

}
