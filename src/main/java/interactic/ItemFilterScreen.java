package interactic;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ItemFilterScreen extends HandledScreen<ItemFilterScreenHandler> {

    private static final Identifier TEXTURE = new Identifier(InteracticInit.MOD_ID, "textures/gui/item_filter.png");

    public boolean blockMode = true;

    private ButtonWidget blockButton = null;
    private ButtonWidget allowButton = null;

    public ItemFilterScreen(ItemFilterScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.backgroundHeight = 178;
        this.playerInventoryTitleY = 69420;
    }

    @Override
    protected void init() {
        super.init();
        this.titleX = (this.backgroundWidth - this.textRenderer.getWidth(this.title)) / 2;

        this.addDrawableChild(this.blockButton = ButtonWidget.builder(Text.literal("Block"), button -> sendModeRequest(true)).dimensions(this.x + 43, this.y + 78, 60, 12).build());
        this.addDrawableChild(this.allowButton = ButtonWidget.builder(Text.literal("Allow"), button -> sendModeRequest(false)).dimensions(this.x + 108, this.y + 78, 60, 12).build());
    }

    private static void sendModeRequest(boolean mode) {
        final var buf = PacketByteBufs.create();
        buf.writeBoolean(mode);
        ClientPlayNetworking.send(new Identifier(InteracticInit.MOD_ID, "filter_mode_request"), buf);
    }


    @SuppressWarnings({"ConstantConditions"})
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        context.drawText(this.client.textRenderer, "Mode", this.x + 8, this.y + 80, 0x404040, false);

        this.drawMouseoverTooltip(context, mouseX, mouseY);

        this.blockButton.active = !this.blockMode;
        this.allowButton.active = this.blockMode;
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        context.drawTexture(TEXTURE, this.x, this.y, 0, 0, this.backgroundWidth, this.backgroundHeight);

        if (!this.blockMode) {
            context.drawTexture(TEXTURE, this.x + 7, this.y + 19, 0, 178, 162, 54);
        }
    }
}
