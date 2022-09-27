package interactic.util;

import interactic.InteracticInit;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.ConfigHolder;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;
import net.minecraft.util.ActionResult;

@Config(name = InteracticInit.MOD_ID)
public class InteracticConfig implements ConfigData {

    @ConfigEntry.Category("client")
    @ConfigEntry.Gui.RequiresRestart
    public boolean clientOnlyMode = false;

    @ServerSideConfigEntry
    @ConfigEntry.Category("global")
    @Comment("Whether players can pick up items by clicking them. This also affects the keybind")
    @ConfigEntry.Gui.RequiresRestart
    public boolean rightClickPickup = true;

    @ServerSideConfigEntry
    @ConfigEntry.Category("global")
    @Comment("Whether players can throw items farther than normal by holding down the drop key")
    @ConfigEntry.Gui.RequiresRestart
    public boolean itemThrowing = true;

    @ServerSideConfigEntry
    @ConfigEntry.Category("global")
    @Comment("Whether the Item Filter should be loaded")
    @ConfigEntry.Gui.RequiresRestart
    public boolean itemFilterEnabled = true;


    @ServerSideConfigEntry
    @ConfigEntry.Category("global")
    @Comment("Whether items that have damage modifiers should also deal damage when thrown")
    public boolean itemsActAsProjectiles = true;

    @ServerSideConfigEntry
    @ConfigEntry.Category("global")
    @Comment("Whether players should be able to pick up items like normal")
    public boolean autoPickup = true;

    @ConfigEntry.Category("client")
    @Comment("Whether Interactic should override Minecraft's default item rendering with a more fancy version. Highly recommended")
    public boolean fancyItemRendering = true;

    @ConfigEntry.Category("client")
    @Comment("Whether Interactic should render the tooltips of items under the crosshair")
    public boolean renderItemTooltips = true;

    @ConfigEntry.Category("client")
    @Comment("Whether Interactic should render the full tooltip of items")
    public boolean renderFullTooltip = true;

    @ConfigEntry.Category("client")
    @Comment("Whether your arms should swing when dropping items")
    public boolean swingArm = true;

    @ConfigEntry.Category("client")
    @Comment("Whether block items should lay flat on the ground")
    public boolean blocksLayFlat = false;

    public static ActionResult processClientOnlyMode(ConfigHolder<InteracticConfig> configHolder, InteracticConfig config) {
        if (!config.clientOnlyMode) return ActionResult.PASS;

        config.itemsActAsProjectiles = false;
        config.itemThrowing = false;
        config.itemFilterEnabled = false;
        config.autoPickup = true;
        config.rightClickPickup = false;

        return ActionResult.PASS;
    }
}
