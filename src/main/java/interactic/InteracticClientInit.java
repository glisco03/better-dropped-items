package interactic;

import interactic.util.InteracticConfig;
import interactic.util.ServerSideConfigEntry;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.gui.registry.api.GuiProvider;
import me.shedaniel.autoconfig.gui.registry.api.GuiRegistryAccess;
import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;
import net.fabricmc.fabric.api.object.builder.v1.client.model.FabricModelPredicateProviderRegistry;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;

public class InteracticClientInit implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        FabricModelPredicateProviderRegistry.register(new Identifier("enabled"), (stack, world, entity, seed) -> stack.getOrCreateTag().getBoolean("Enabled") ? 1 : 0);

        ScreenRegistry.register(InteracticInit.ITEM_FILTER_SCREEN_HANDLER, ItemFilterScreen::new);

        final var guiRegistry = AutoConfig.getGuiRegistry(InteracticConfig.class);
        guiRegistry.registerAnnotationProvider(new InteracticConfigGuiProvider(), ServerSideConfigEntry.class);
    }

    private static class InteracticConfigGuiProvider implements GuiProvider {

        @Override
        public List<AbstractConfigListEntry> get(String s, Field field, Object config, Object defaults, GuiRegistryAccess guiRegistryAccess) {
            ConfigEntryBuilder builder = ConfigEntryBuilder.create();

            if (((InteracticConfig) config).clientOnlyMode) return Collections.emptyList();

            try {
                return Collections.singletonList(builder.startBooleanToggle(new TranslatableText(s), field.getBoolean(config))
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
