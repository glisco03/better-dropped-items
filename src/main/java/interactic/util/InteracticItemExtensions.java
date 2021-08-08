package interactic.util;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.math.Vec3d;

public interface InteracticItemExtensions {
    float getRotation();
    void setRotation(float rotation);

    void markThrown();
    void markFullPower();
}
