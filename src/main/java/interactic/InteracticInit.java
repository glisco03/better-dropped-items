package interactic;

import interactic.util.Helpers;
import interactic.util.InteracticConfig;
import interactic.util.InteracticPlayerExtension;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry;
import net.minecraft.item.Item;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class InteracticInit implements ModInitializer {

    public static final String MOD_ID = "interactic";

    public static final Item ITEM_FILTER = new ItemFilterItem();

    private static InteracticConfig CONFIG;

    public static final ScreenHandlerType<ItemFilterScreenHandler> ITEM_FILTER_SCREEN_HANDLER =
            ScreenHandlerRegistry.registerSimple(new Identifier(MOD_ID, "item_filter"), ItemFilterScreenHandler::new);

    @Override
    public void onInitialize() {
        AutoConfig.register(InteracticConfig.class, JanksonConfigSerializer::new);

        AutoConfig.getConfigHolder(InteracticConfig.class).registerSaveListener(InteracticConfig::processClientOnlyMode);
        AutoConfig.getConfigHolder(InteracticConfig.class).registerLoadListener(InteracticConfig::processClientOnlyMode);

        CONFIG = AutoConfig.getConfigHolder(InteracticConfig.class).getConfig();

        if (CONFIG.itemFilterEnabled) {
            Registry.register(Registry.ITEM, new Identifier(MOD_ID, "item_filter"), ITEM_FILTER);
        }

        if (CONFIG.rightClickPickup) {
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

        if (CONFIG.itemThrowing) {
            ServerPlayNetworking.registerGlobalReceiver(new Identifier(MOD_ID, "drop_with_power"), (server, player, handler, buf, responseSender) -> {
                final float power = buf.readFloat();
                final boolean dropAll = buf.readBoolean();
                server.execute(() -> {
                    ((InteracticPlayerExtension) player).setDropPower(power);
                    player.dropSelectedItem(dropAll);
                });
            });
        }
    }

    public static InteracticConfig getConfig() {
        return CONFIG;
    }
}

