package interactic.util;

import blue.endless.jankson.Comment;
import interactic.InteracticInit;
import io.wispforest.owo.config.annotation.Config;
import io.wispforest.owo.config.annotation.Hook;
import io.wispforest.owo.config.annotation.RestartRequired;
import io.wispforest.owo.config.annotation.SectionHeader;

@Config(name = InteracticInit.MOD_ID, wrapperName = "InteracticConfig")
public class InteracticConfigModel {

    @Hook
    @RestartRequired
    public boolean clientOnlyMode = false;

    @Hook
    @SectionHeader("global")
    @ServerSideConfigOption
    @Comment("Whether players can pick up items by clicking them. This also affects the keybind")
    @RestartRequired
    public boolean rightClickPickup = true;

    @Hook
    @ServerSideConfigOption
    @Comment("Whether players can throw items farther than normal by holding down the drop key")
    @RestartRequired
    public boolean itemThrowing = true;

    @Hook
    @ServerSideConfigOption
    @Comment("Whether the Item Filter should be loaded")
    @RestartRequired
    public boolean itemFilterEnabled = true;

    @Hook
    @ServerSideConfigOption
    @Comment("Whether items that have damage modifiers should also deal damage when thrown")
    public boolean itemsActAsProjectiles = true;

    @Hook
    @ServerSideConfigOption
    @Comment("Whether players should be able to pick up items like normal")
    public boolean autoPickup = true;

    @SectionHeader("client")
    @Comment("Whether Interactic should override Minecraft's default item rendering with a more fancy version. Highly recommended")
    public boolean fancyItemRendering = true;

    @Comment("Whether Interactic should render the tooltips of items under the crosshair")
    public boolean renderItemTooltips = true;

    @Comment("Whether Interactic should render the full tooltip of items")
    public boolean renderFullTooltip = true;

    @Comment("Whether your arms should swing when dropping items")
    public boolean swingArm = true;

    @Comment("Whether block items should lay flat on the ground")
    public boolean blocksLayFlat = false;
}
