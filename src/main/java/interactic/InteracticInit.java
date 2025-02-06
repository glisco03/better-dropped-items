package interactic;

import interactic.itemfilter.ItemFilterItem;
import interactic.itemfilter.ItemFilterScreenHandler;
import interactic.util.InteracticConfig;
import interactic.util.InteracticNetworking;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
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
            Registry.register(Registries.SCREEN_HANDLER, id("item_filter"), new ScreenHandlerType<>(ItemFilterScreenHandler::new, FeatureFlags.DEFAULT_ENABLED_FEATURES));

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
            Identifier id = id("item_filter");
            RegistryKey<Item> key = RegistryKey.of(RegistryKeys.ITEM, id);
            ITEM_FILTER = Registry.register(Registries.ITEM, id, new ItemFilterItem(key));
        }

        InteracticNetworking.init();
    }

    public static Identifier id(String path) {
        return Identifier.of(MOD_ID, path);
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

    public static float getItemRotationSpeedMultiplier() {
        return itemRotationSpeedMultiplier;
    }

    public static InteracticConfig getConfig() {
        return CONFIG;
    }
}

