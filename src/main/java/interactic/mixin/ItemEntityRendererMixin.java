package interactic.mixin;

import interactic.InteracticInit;
import interactic.util.InteracticItemExtensions;
import net.minecraft.block.ShapeContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.ItemEntityRenderer;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Quaternion;
import net.minecraft.util.math.Vec3f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

@Mixin(ItemEntityRenderer.class)
public abstract class ItemEntityRendererMixin extends EntityRenderer<ItemEntity> {

    private static final double TWO_PI = Math.PI * 2;
    private static final double HALF_PI = Math.PI * 0.5;
    private static final double THREE_HALF_PI = Math.PI * 1.5;

    @Shadow
    @Final
    private Random random;

    @Shadow
    @Final
    private ItemRenderer itemRenderer;

    @Shadow
    protected abstract int getRenderedAmount(ItemStack stack);

    private ItemEntityRendererMixin(EntityRendererFactory.Context dispatcher) {
        super(dispatcher);
    }

    @Inject(at = @At("RETURN"), method = "<init>")
    private void onConstructor(EntityRendererFactory.Context context, CallbackInfo ci) {
        this.shadowRadius = 0;
    }

    @Inject(at = @At("HEAD"), method = "render", cancellable = true)
    private void render(ItemEntity entity, float f, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumerProvider, int light, CallbackInfo callback) {
        if (!InteracticInit.getConfig().fancyItemRendering) return;

        ItemStack itemStack = entity.getStack();

        //Calculate the random seed for this specific item so that we can use random values
        //This works differently to the vanilla one to create differences between item entities of the same type
        int seed = itemStack.isEmpty() ? 187 : Item.getRawId(itemStack.getItem()) * entity.getId();
        this.random.setSeed(seed);

        matrices.push();

        BakedModel bakedModel = this.itemRenderer.getModel(itemStack, entity.world, null, seed);
        final int renderCount = this.getRenderedAmount(itemStack);
        InteracticItemExtensions rotator = (InteracticItemExtensions) entity;

        final var item = entity.getStack().getItem();
        final boolean treatAsDepthModel = item instanceof BlockItem && bakedModel.hasDepth();

        final var transform = bakedModel.getTransformation().ground;

        final float scaleX = bakedModel.getTransformation().ground.scale.getX();
        final float scaleY = bakedModel.getTransformation().ground.scale.getY();
        final float scaleZ = bakedModel.getTransformation().ground.scale.getZ();

        //Calculate the distance the model's center is from the item entity's center using the block outline shape
        final double blockHeight = !treatAsDepthModel ? 0 : ((BlockItem) item).getBlock().getOutlineShape(((BlockItem) item).getBlock().getDefaultState(), entity.world, entity.getBlockPos(), ShapeContext.absent()).getMax(Direction.Axis.Y);
        final boolean isFlatBlock = treatAsDepthModel && blockHeight <= 0.75;
        final double distanceToCenter = (0.5 - blockHeight + blockHeight / 2) * 0.25;

        //Translate so that everything happens in the middle of the item hitbox
        matrices.translate(0, 0.125f, 0);

        //Move the model, so it's center is at the base of the item entity
        if (treatAsDepthModel) matrices.translate(0, distanceToCenter, 0);

        //Calculate ground distance from either the amount of items or block height
        float groundDistance = treatAsDepthModel ? (float) distanceToCenter : (float) (0.125 - 0.0625 * scaleZ);
        if (!treatAsDepthModel) groundDistance -= (renderCount - 1) * 0.05 * scaleZ;
        matrices.translate(0, -groundDistance, 0);

        //Translate randomly to avoid Z-Fighting
        matrices.translate(0, (random.nextDouble() - 0.5) * 0.005, 0);
        if (treatAsDepthModel && !isFlatBlock) matrices.translate(0, -.1, 0);

        //Rotate the item by its yaw to get some randomness for the spinning axis
        matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(entity.getYaw()));

        //Calculate rotation based on velocity or get the one the item had
        //before it hit the ground
        if (rotator.getRotation() == -1) rotator.setRotation((random.nextInt(20) - 10) * 0.15f);
        float angle = entity.isOnGround() ? rotator.getRotation() : (float) (rotator.getRotation() + ((MathHelper.clamp(entity.getVelocity().y * 0.25, 0.075, 0.3))) * (entity.isSubmergedInWater() ? 0.25f : 1) * (MinecraftClient.getInstance().getLastFrameDuration() * 5) * InteracticInit.getItemRotationSpeedMultiplier());

        //Make sure the angle never exceeds two pi
        if (angle >= TWO_PI) angle -= TWO_PI;

        //Clusterfuck our way back to either 0 or 180 degrees. There has to be a better way to do this
        if (entity.isOnGround() && !(angle == 0 || angle == (float) Math.PI)) {
            if (angle > Math.PI) {
                if (angle > THREE_HALF_PI) angle += tickDelta * 0.5;
                else {
                    angle -= tickDelta * 0.5;
                }
            } else {
                if (angle > HALF_PI) {
                    angle += tickDelta * 0.5;
                    if (angle > Math.PI) angle = (float) Math.PI;
                } else angle -= tickDelta * 0.5;
            }

            if (angle < 0) angle = 0;
            if (angle > TWO_PI) angle = 0;
        }

        //Move the matrix back so the rotation happens around the model's center
        if (treatAsDepthModel) matrices.translate(0, -distanceToCenter, 0);

        //Spin the item and store the value inside it should it hit the ground next tick
        matrices.multiply(Vec3f.POSITIVE_X.getRadialQuaternion((float) (angle + (isFlatBlock ? 0 : HALF_PI))));
        rotator.setRotation(angle);

        // If the block is chonky, rotate it randomly
        if (treatAsDepthModel && !isFlatBlock) {
            matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(this.random.nextFloat(45)));
            matrices.multiply(Vec3f.POSITIVE_Z.getDegreesQuaternion(this.random.nextFloat(45)));
        }

        //Undo the translation from before
        if (treatAsDepthModel) {
            matrices.translate(0, distanceToCenter, 0);
        }

        //Translate so that the origin gets moved back for stacks with multiple items rendered
        matrices.translate(0, 0, ((0.09375 - (renderCount * 0.1)) * 0.5) * scaleZ);

        float x;
        float y;

        for (int i = 0; i < renderCount; ++i) {

            //Only apply random transformation to the current item
            matrices.push();

            //Only apply transformations to items from the second one onward
            if (i > 0) {

                //Decide whether to use random rotation or positioning based on whether the
                //item has depth, which most of the time means that it's a block
                if (treatAsDepthModel) {
                    x = (this.random.nextFloat() * 2f - 1f) * .1f;
                    y = (this.random.nextFloat() * 2f - 1f) * .1f;
                    float z = (this.random.nextFloat() * 2f - 1f) * .1f;
                    matrices.translate(x, y, z);
                } else {
                    matrices.translate(0, 0.125f, 0.0D);
                    matrices.multiply(Vec3f.POSITIVE_Z.getRadialQuaternion((this.random.nextFloat() - 0.5f)));
                    matrices.translate(0, -0.125f, 0.0D);
                }
            }

            //Only apply the scale and rotation part of the model transform to avoid weird issues with alignment and rotation
            matrices.multiply(new Quaternion(transform.rotation.getX(), transform.rotation.getY(), transform.rotation.getZ(), true));
            matrices.scale(scaleX, scaleY, scaleZ);

            this.itemRenderer.renderItem(itemStack, ModelTransformation.Mode.NONE, false, matrices, vertexConsumerProvider, light, OverlayTexture.DEFAULT_UV, bakedModel);

            matrices.pop();

            // Translate normal items to create visual layering
            if (!treatAsDepthModel) {
                matrices.translate(0, 0, 0.1F * scaleZ);
            }
        }

        matrices.pop();
        super.render(entity, f, tickDelta, matrices, vertexConsumerProvider, light);
        callback.cancel();
    }
}
