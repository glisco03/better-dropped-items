package interactic;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;
import net.fabricmc.fabric.api.object.builder.v1.client.model.FabricModelPredicateProviderRegistry;
import net.minecraft.util.Identifier;

public class InteracticClientInit implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        FabricModelPredicateProviderRegistry.register(new Identifier("enabled"), (stack, world, entity, seed) -> stack.getOrCreateTag().getBoolean("Enabled") ? 1 : 0);

        ScreenRegistry.register(InteracticInit.ITEM_FILTER_SCREEN_HANDLER, ItemFilterScreen::new);
    }
}
