package interactic;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ItemFilterScreen extends HandledScreen<ItemFilterScreenHandler> {

    private static final Identifier TEXTURE = new Identifier(InteracticInit.MOD_ID, "textures/gui/item_filter.png");

    public boolean blockMode = true;

    @Override
    protected void init() {
        super.init();
        titleX = (backgroundWidth - textRenderer.getWidth(title)) / 2;

        int i = (this.width - this.backgroundWidth) / 2;
        int j = (this.height - this.backgroundHeight) / 2;

        this.addDrawableChild(new TexturedButtonWidget(i + 43, j + 42, 60, 12, 176, 12, 12, TEXTURE, button -> {
            sendModeRequest(true);
        }));

        this.addDrawableChild(new TexturedButtonWidget(i + 108, j + 42, 60, 12, 176, 12, 12, TEXTURE, button -> {
            sendModeRequest(false);
        }));

    }

    private static void sendModeRequest(boolean mode) {
        final var buf = PacketByteBufs.create();
        buf.writeBoolean(mode);
        ClientPlayNetworking.send(new Identifier(InteracticInit.MOD_ID, "filter_mode_request"), buf);
    }

    public ItemFilterScreen(ItemFilterScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.passEvents = false;
        this.backgroundHeight = 142;
        this.playerInventoryTitleY = 69420;
    }

    @SuppressWarnings("IntegerDivisionInFloatingPointContext")
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        super.render(matrices, mouseX, mouseY, delta);

        int i = (this.width - this.backgroundWidth) / 2;
        int j = (this.height - this.backgroundHeight) / 2;

        client.textRenderer.draw(matrices, "Mode", i + 8, j + 44, 0x404040);

        int blockWidth = textRenderer.getWidth("Block");
        int allowWidth = textRenderer.getWidth("Allow");

        client.textRenderer.drawWithShadow(matrices, "Block", i + 73 - blockWidth / 2, j + 44, 0xFFFFFF);
        client.textRenderer.drawWithShadow(matrices, "Allow", i + 138 - allowWidth / 2, j + 44, 0xFFFFFF);

        this.drawMouseoverTooltip(matrices, mouseX, mouseY);
    }

    protected void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);
        int i = (this.width - this.backgroundWidth) / 2;
        int j = (this.height - this.backgroundHeight) / 2;
        this.drawTexture(matrices, i, j, 0, 0, this.backgroundWidth, this.backgroundHeight);

        if (!blockMode) {
            this.drawTexture(matrices, i + 7, j + 19, 0, 142, 162, 18);
        }
    }
}
