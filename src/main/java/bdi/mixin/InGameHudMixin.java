package bdi.mixin;

import bdi.util.RaycastHelper;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.ItemEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(InGameHud.class)
public class InGameHudMixin {

    @Shadow
    private int scaledWidth;

    @Shadow
    private int scaledHeight;

    @Inject(method = "renderCrosshair", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;drawTexture(Lnet/minecraft/client/util/math/MatrixStack;IIIIII)V", ordinal = 0))
    private void renderItemTooltip(MatrixStack matrices, CallbackInfo ci) {
        final var client = MinecraftClient.getInstance();
        final var item = RaycastHelper.raycastItem(client.getCameraEntity(), 5);

        if (item == null) return;

        List<Text> tooltip = item.getStack().getTooltip(null, TooltipContext.Default.NORMAL);
        for (int i = 0, tooltipSize = tooltip.size(); i < tooltipSize; i++) {
            final var text = tooltip.get(i);
            client.textRenderer.drawWithShadow(matrices, text, this.scaledWidth / 2 - client.textRenderer.getWidth(text) / 2, this.scaledHeight / 2 + 15 + i * 10, 0xFFFFFF);
        }

        RenderSystem.setShaderTexture(0, InGameHud.GUI_ICONS_TEXTURE);
    }

}
