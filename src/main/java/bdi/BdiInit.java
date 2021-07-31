package bdi;

import bdi.util.Helpers;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry;
import net.minecraft.item.Item;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class BdiInit implements ModInitializer {

    public static final String MOD_ID = "betterdroppeditems";

    public static final Item ITEM_FILTER = new ItemFilterItem();

    private static BdiConfig CONFIG;

    public static final ScreenHandlerType<ItemFilterScreenHandler> ITEM_FILTER_SCREEN_HANDLER =
            ScreenHandlerRegistry.registerSimple(new Identifier(MOD_ID, "item_filter"), ItemFilterScreenHandler::new);

    @Override
    public void onInitialize() {
        AutoConfig.register(BdiConfig.class, JanksonConfigSerializer::new);
        CONFIG = AutoConfig.getConfigHolder(BdiConfig.class).getConfig();

        if(CONFIG.itemFilterEnabled) {
            Registry.register(Registry.ITEM, new Identifier(MOD_ID, "item_filter"), ITEM_FILTER);
        }

        if(CONFIG.rightClickPickup) {
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

    }

    public static BdiConfig getConfig() {
        return CONFIG;
    }

    @Config(name = MOD_ID)
    public static class BdiConfig implements ConfigData {

        @Comment("Whether players can pick up items by clicking them")
        @ConfigEntry.Gui.RequiresRestart
        public boolean rightClickPickup = true;

        @Comment("Whether the Item Filter should be loaded")
        @ConfigEntry.Gui.RequiresRestart
        public boolean itemFilterEnabled = true;

        @Comment("Whether players should be able to pick up items like normal")
        public boolean autoPickup = true;

        @Comment("Whether BDI should override Minecraft's default item rendering with a more fancy version. Highly recommended")
        public boolean fancyItemRendering = true;

        @Comment("Whether BDI should render the tooltips of items under the crosshair")
        public boolean renderItemTooltips = true;

        @Comment("Whether BDI should render the full tooltip of items")
        public boolean renderFullTooltip = true;
    }
}

