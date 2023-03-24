package interactic;

import interactic.util.Helpers;
import interactic.util.InteracticConfig;
import interactic.util.InteracticPlayerExtension;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;

import java.util.function.Consumer;

public class InteracticInit implements ModInitializer {

    public static final String MOD_ID = "interactic";

    private static Item ITEM_FILTER = null;

    private static final InteracticConfig CONFIG = InteracticConfig.createAndLoad();
    private static float itemRotationSpeedMultiplier = 1f;

    public static final ScreenHandlerType<ItemFilterScreenHandler> ITEM_FILTER_SCREEN_HANDLER =
            Registry.register(Registries.SCREEN_HANDLER, new Identifier(MOD_ID, "item_filter"), new ScreenHandlerType<>(ItemFilterScreenHandler::new, FeatureFlags.DEFAULT_ENABLED_FEATURES));

    @Override
    public void onInitialize() {
        CONFIG.subscribeToClientOnlyMode(clientOnlyMode -> {
            if (!clientOnlyMode) return;

            CONFIG.itemsActAsProjectiles(false);
            CONFIG.itemThrowing(false);
            CONFIG.itemFilterEnabled(false);
            CONFIG.autoPickup(true);
            CONFIG.rightClickPickup(false);
        });

        enforceInClientOnlyMode(CONFIG::subscribeToItemsActAsProjectiles, CONFIG::itemsActAsProjectiles, false);
        enforceInClientOnlyMode(CONFIG::subscribeToItemThrowing, CONFIG::itemThrowing, false);
        enforceInClientOnlyMode(CONFIG::subscribeToItemFilterEnabled, CONFIG::itemFilterEnabled, false);
        enforceInClientOnlyMode(CONFIG::subscribeToAutoPickup, CONFIG::autoPickup, true);
        enforceInClientOnlyMode(CONFIG::subscribeToRightClickPickup, CONFIG::rightClickPickup, false);

        if (FabricLoader.getInstance().isModLoaded("iris")) itemRotationSpeedMultiplier = 0.5f;

        if (CONFIG.itemFilterEnabled()) {
            ITEM_FILTER = Registry.register(Registries.ITEM, new Identifier(MOD_ID, "item_filter"), new ItemFilterItem());

            ServerPlayNetworking.registerGlobalReceiver(new Identifier(MOD_ID, "filter_mode_request"), (server, player, handler, buf, responseSender) -> {
                final boolean newMode = buf.readBoolean();
                server.execute(() -> {
                    if (!(player.currentScreenHandler instanceof ItemFilterScreenHandler filterHandler)) return;
                    filterHandler.setFilterMode(newMode);
                });
            });
        }

        if (CONFIG.rightClickPickup()) {
            ServerPlayNetworking.registerGlobalReceiver(new Identifier(MOD_ID, "pickup"), (server, player, handler, buf, responseSender) -> {
                server.execute(() -> {
                    final var item = Helpers.raycastItem(player.getCameraEntity(), 6);
                    if (item == null) return;

                    if (player.getInventory().insertStack(item.getStack().copy())) {
                        player.sendPickup(item, item.getStack().getCount());
                        item.discard();
                    }
                });
            });
        }

        if (CONFIG.itemThrowing()) {
            ServerPlayNetworking.registerGlobalReceiver(new Identifier(MOD_ID, "drop_with_power"), (server, player, handler, buf, responseSender) -> {
                final float power = buf.readFloat();
                final boolean dropAll = buf.readBoolean();
                server.execute(() -> {
                    ((InteracticPlayerExtension) player).setDropPower(power);
                    dropSelected(player, dropAll);
                });
            });
        }
    }


    public static Item getItemFilter() {
        return ITEM_FILTER;
    }

    private static void enforceInClientOnlyMode(Consumer<Consumer<Boolean>> eventSource, Consumer<Boolean> setter, boolean defaultValue) {
        eventSource.accept(value -> {
            if (!CONFIG.clientOnlyMode()) return;
            if (value != defaultValue) setter.accept(defaultValue);
        });
    }

    private void dropSelected(PlayerEntity player, boolean dropAll) {
        player.dropItem(player.getInventory().removeStack(player.getInventory().selectedSlot, dropAll && !player.getInventory().getMainHandStack().isEmpty() ? player.getInventory().getMainHandStack().getCount() : 1), false, true);
    }

    public static float getItemRotationSpeedMultiplier() {
        return itemRotationSpeedMultiplier;
    }

    public static InteracticConfig getConfig() {
        return CONFIG;
    }
}

