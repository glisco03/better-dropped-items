package interactic.util;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.math.Vec3d;

@Environment(EnvType.CLIENT)
public interface InteracticItemExtensions {
    float getRotation();
    void setRotation(float rotation);

    boolean getWasThrown();
    void markThrown();

}
