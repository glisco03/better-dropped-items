package interactic.mixin;

import interactic.InteracticInit;
import interactic.util.Helpers;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collections;
import java.util.List;

@Mixin(InGameHud.class)
public class InGameHudMixin {

    @Shadow
    private int scaledWidth;

    @Shadow
    private int scaledHeight;

    @Inject(method = "renderCrosshair", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawGuiTexture(Lnet/minecraft/util/Identifier;IIII)V", ordinal = 0))
    private void renderItemTooltip(DrawContext context, CallbackInfo ci) {
        if (!InteracticInit.getConfig().renderItemTooltips()) return;

        final var client = MinecraftClient.getInstance();
        final var item = Helpers.raycastItem(client.getCameraEntity(), 5);

        if (item == null) return;
        var tooltip = InteracticInit.getConfig().renderFullTooltip()
                ? item.getStack().getTooltip(client.player, TooltipContext.Default.BASIC)
                : List.of(item.getStack().getName());

        for (int i = 0, tooltipSize = tooltip.size(); i < tooltipSize; i++) {
            final var text = tooltip.get(i);
            context.drawText(client.textRenderer, text, this.scaledWidth / 2 - client.textRenderer.getWidth(text) / 2, this.scaledHeight / 2 + 15 + i * 10, 0xFFFFFF, true);
        }
    }

}
