package interactic.data;

import com.mojang.serialization.MapCodec;
import interactic.itemfilter.ItemFilterItem;
import net.minecraft.client.render.item.property.bool.BooleanProperty;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ModelTransformationMode;
import org.jetbrains.annotations.Nullable;

public record EnabledProperty() implements BooleanProperty {
    public static final MapCodec<EnabledProperty> CODEC = MapCodec.unit(new EnabledProperty());

    @Override
    public boolean getValue(ItemStack stack, @Nullable ClientWorld world, @Nullable LivingEntity user, int seed, ModelTransformationMode modelTransformationMode) {
        return stack.getOrDefault(ItemFilterItem.ENABLED, false);
    }

    @Override
    public MapCodec<EnabledProperty> getCodec() {
        return CODEC;
    }
}
