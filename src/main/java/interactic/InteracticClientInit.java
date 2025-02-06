package interactic;

import interactic.data.EnabledProperty;
import interactic.itemfilter.ItemFilterScreen;
import interactic.util.InteracticNetworking;
import io.wispforest.owo.config.ui.ConfigScreenProviders;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.item.property.bool.BooleanProperties;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Hand;

public class InteracticClientInit implements ClientModInitializer {

    public static final KeyBinding PICKUP_ITEM = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.interactic.pickup_item",
            InputUtil.UNKNOWN_KEY.getCode(), "key.categories.misc"));

    @Override
    public void onInitializeClient() {
        HandledScreens.register(InteracticInit.ITEM_FILTER_SCREEN_HANDLER, ItemFilterScreen::new);
        BooleanProperties.ID_MAPPER.put(InteracticInit.id("enabled"), EnabledProperty.CODEC);

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (PICKUP_ITEM.wasPressed()) {
                InteracticNetworking.CHANNEL.clientHandle().send(new InteracticNetworking.Pickup());
                client.player.swingHand(Hand.MAIN_HAND);
            }
        });

        ConfigScreenProviders.register("interactic", InteracticConfigScreen::new);
        InteracticNetworking.initClient();
    }
}
