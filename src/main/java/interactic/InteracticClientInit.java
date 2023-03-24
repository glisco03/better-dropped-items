package interactic;

import io.wispforest.owo.config.ui.ConfigScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;

public class InteracticClientInit implements ClientModInitializer {

    public static final KeyBinding PICKUP_ITEM = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.interactic.pickup_item",
            InputUtil.UNKNOWN_KEY.getCode(), "key.categories.misc"));

    @Override
    public void onInitializeClient() {
        ModelPredicateProviderRegistry.register(new Identifier("enabled"), (stack, world, entity, seed) -> stack.getOrCreateNbt().getBoolean("Enabled") ? 1 : 0);

        HandledScreens.register(InteracticInit.ITEM_FILTER_SCREEN_HANDLER, ItemFilterScreen::new);

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (PICKUP_ITEM.wasPressed()) {
                ClientPlayNetworking.send(new Identifier(InteracticInit.MOD_ID, "pickup"), PacketByteBufs.empty());
                client.player.swingHand(Hand.MAIN_HAND);
            }
        });

        ConfigScreen.registerProvider("interactic", InteracticConfigScreen::new);

        if (InteracticInit.getConfig().itemFilterEnabled()) {
            ClientPlayNetworking.registerGlobalReceiver(new Identifier(InteracticInit.MOD_ID, "set_filter_mode"), (client, handler, buf, responseSender) -> {
                final boolean newMode = buf.readBoolean();
                client.execute(() -> {
                    if (!(client.currentScreen instanceof ItemFilterScreen screen)) return;
                    screen.blockMode = newMode;
                });
            });
        }
    }
}
