package interactic;

import io.wispforest.owo.client.screens.ScreenUtils;
import io.wispforest.owo.client.screens.SlotGenerator;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class ItemFilterScreenHandler extends ScreenHandler {

    public static final int SLOT_COUNT = 27;
    private final Inventory inventory;
    private final PlayerEntity player;

    public ItemFilterScreenHandler(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory, new SimpleInventory(SLOT_COUNT));
    }

    public ItemFilterScreenHandler(int syncId, PlayerInventory playerInventory, Inventory inventory) {
        super(InteracticInit.ITEM_FILTER_SCREEN_HANDLER, syncId);
        this.inventory = inventory;
        checkSize(inventory, SLOT_COUNT);

        this.player = playerInventory.player;
        inventory.onOpen(player);

        SlotGenerator.begin(this::addSlot, 8, 20)
                .slotFactory(GhostSlot::new)
                .grid(inventory, 0, 9, 3)
                .defaultSlotFactory()
                .moveTo(8, 96)
                .playerInventory(playerInventory);
    }

    public void setFilterMode(boolean mode) {
        if (!(inventory instanceof ItemFilterItem.FilterInventory filterInventory)) return;
        filterInventory.setFilterMode(mode);

        final var buf = PacketByteBufs.create();
        buf.writeBoolean(mode);
        ServerPlayNetworking.send((ServerPlayerEntity) player, new Identifier(InteracticInit.MOD_ID, "set_filter_mode"), buf);
    }

    public boolean canUse(PlayerEntity player) {
        return this.inventory.canPlayerUse(player);
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int index) {
        return ScreenUtils.handleSlotTransfer(this, index, 0);
    }

    @Override
    public void onClosed(PlayerEntity playerEntity) {
        super.onClosed(playerEntity);
        this.inventory.onClose(playerEntity);
    }

    private static class GhostSlot extends Slot {

        public GhostSlot(Inventory inventory, int index, int x, int y) {
            super(inventory, index, x, y);
        }

        @Override
        public int getMaxItemCount() {
            return 1;
        }

        @Override
        public boolean canInsert(ItemStack stack) {
            this.setStack(new ItemStack(stack.getItem()));
            return false;
        }

        @Override
        public boolean canTakeItems(PlayerEntity playerEntity) {
            this.setStack(ItemStack.EMPTY);
            return false;
        }
    }
}
