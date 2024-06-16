package interactic.util;

import interactic.InteracticInit;
import interactic.ItemFilterItem;
import interactic.ItemFilterScreen;
import interactic.ItemFilterScreenHandler;
import interactic.mixin.ItemEntityAccessor;
import io.wispforest.owo.network.OwoNetChannel;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

public class InteracticNetworking {

    public static final OwoNetChannel CHANNEL = OwoNetChannel.create(InteracticInit.id("channel"));

    public static void init() {
        CHANNEL.registerClientboundDeferred(ItemFilterItem.SetFilterModePacket.class);


        CHANNEL.registerServerbound(Pickup.class, (message, access) -> {
            final var item = Helpers.raycastItem(access.player().getCameraEntity(), 6);
            if (item == null || ((ItemEntityAccessor) item).interactic$getPickupDelay() == Short.MAX_VALUE) {
                return;
            }

            if (access.player().getInventory().insertStack(item.getStack().copy())) {
                access.player().sendPickup(item, item.getStack().getCount());
                item.discard();
            }
        });


        CHANNEL.registerServerbound(DropWithPower.class, (message, access) -> {
            ((InteracticPlayerExtension) access.player()).setDropPower(message.power);

            var player = access.player();
            player.dropItem(player.getInventory().removeStack(player.getInventory().selectedSlot, message.dropAll && !player.getInventory().getMainHandStack().isEmpty() ? player.getInventory().getMainHandStack().getCount() : 1), false, true);
        });

        CHANNEL.registerServerbound(FilterModeRequest.class, (message, access) -> {
            if (!(access.player().currentScreenHandler instanceof ItemFilterScreenHandler filterHandler)) return;
            filterHandler.setFilterMode(message.newMode);
        });

    }

    @Environment(EnvType.CLIENT)
    public static void initClient() {
        CHANNEL.registerClientbound(ItemFilterItem.SetFilterModePacket.class, (message, access) -> {
            if (!(access.runtime().currentScreen instanceof ItemFilterScreen screen)) return;
            screen.blockMode = message.mode();
        });

    }

    public record Pickup() {}

    public record DropWithPower(float power, boolean dropAll) {}

    public record FilterModeRequest(boolean newMode) {}
}
