package interactic;

import interactic.util.InteracticConfig;
import interactic.util.ServerSideConfigEntry;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.gui.registry.api.GuiProvider;
import me.shedaniel.autoconfig.gui.registry.api.GuiRegistryAccess;
import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.object.builder.v1.client.model.FabricModelPredicateProviderRegistry;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

        final var guiRegistry = AutoConfig.getGuiRegistry(InteracticConfig.class);
        guiRegistry.registerAnnotationProvider(new InteracticConfigGuiProvider(), ServerSideConfigEntry.class);
        guiRegistry.registerPredicateTransformer((list, s, field, o, o1, guiRegistryAccess) -> {
            final var mutableList = new ArrayList<>(list);
            mutableList.add(ConfigEntryBuilder.create().startTextDescription(Text.of("This option disables all features in the Global category and removes them from the config screen after reboot. If you try to change them in the config file, they will be overwritten.")).build());
            return mutableList;
        }, field -> field.getName().equals("clientOnlyMode"));

        if (InteracticInit.getConfig().itemFilterEnabled) {
            ClientPlayNetworking.registerGlobalReceiver(new Identifier(InteracticInit.MOD_ID, "set_filter_mode"), (client, handler, buf, responseSender) -> {
                final boolean newMode = buf.readBoolean();
                client.execute(() -> {
                    if (!(client.currentScreen instanceof ItemFilterScreen screen)) return;
                    screen.blockMode = newMode;
                });
            });
        }
    }

    private static class InteracticConfigGuiProvider implements GuiProvider {

        @Override
        @SuppressWarnings("rawtypes")
        public List<AbstractConfigListEntry> get(String s, Field field, Object config, Object defaults, GuiRegistryAccess guiRegistryAccess) {
            ConfigEntryBuilder builder = ConfigEntryBuilder.create();

            if (((InteracticConfig) config).clientOnlyMode) return Collections.emptyList();

            try {
                return Collections.singletonList(builder.startBooleanToggle(Text.translatable(s), field.getBoolean(config))
                        .setSaveConsumer(aBoolean -> trySetBoolean(field, config, aBoolean))
                        .setDefaultValue(field.getBoolean(defaults)).build());
            } catch (IllegalAccessException e) {
                return Collections.emptyList();
            }
        }

        private void trySetBoolean(Field f, Object o, boolean value) {
            try {
                f.setBoolean(o, value);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }
}
