package bdi;

import bdi.util.RaycastHelper;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.util.Identifier;

public class BdiInit implements ModInitializer {
    @Override
    public void onInitialize() {
        ServerPlayNetworking.registerGlobalReceiver(new Identifier("bdi", "pickup"), (server, player, handler, buf, responseSender) -> {
            server.execute(() -> {
                final var item = RaycastHelper.raycastItem(player.getCameraEntity(), 6);
                if (item == null) return;

                if (player.getInventory().insertStack(item.getStack().copy())) {
                    player.sendPickup(item, item.getStack().getCount());
                    item.discard();
                }
            });
        });
    }
}

