package interactic.itemfilter;

import com.mojang.serialization.Codec;
import interactic.InteracticInit;
import interactic.util.InteracticNetworking;
import io.wispforest.endec.Endec;
import io.wispforest.endec.impl.RecordEndec;
import io.wispforest.endec.impl.ReflectiveEndecBuilder;
import io.wispforest.owo.serialization.CodecUtils;
import io.wispforest.owo.serialization.endec.MinecraftEndecs;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.component.ComponentType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ItemFilterItem extends Item {

    static {
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(entries -> {
            entries.add(InteracticInit.getItemFilter());
        });
    }

    public static final ComponentType<Boolean> ENABLED = Registry.register(
            Registries.DATA_COMPONENT_TYPE,
            InteracticInit.id("item_filter_enabled"),
            ComponentType.<Boolean>builder()
                    .codec(Codec.BOOL)
                    .packetCodec(PacketCodecs.BOOLEAN)
                    .build()
    );

    public static final ComponentType<Boolean> BLOCK_MODE = Registry.register(
            Registries.DATA_COMPONENT_TYPE,
            InteracticInit.id("item_filter_block_mode"),
            ComponentType.<Boolean>builder()
                    .codec(Codec.BOOL)
                    .packetCodec(PacketCodecs.BOOLEAN)
                    .build()
    );

    public static final ComponentType<DefaultedList<ItemStack>> FILTER_SLOTS = Registry.register(
            Registries.DATA_COMPONENT_TYPE,
            InteracticInit.id("item_filter_slots"),
            ComponentType.<DefaultedList<ItemStack>>builder()
                    .codec(CodecUtils.toCodec(InventoryEntry.INVENTORY_ENDEC))
                    .packetCodec(CodecUtils.toPacketCodec(InventoryEntry.INVENTORY_ENDEC))
                    .build()
    );

    public ItemFilterItem(RegistryKey<Item> key) {
        super(new Settings()
                .maxCount(1)
                .component(ENABLED, true)
                .component(BLOCK_MODE, true)
                .component(FILTER_SLOTS, DefaultedList.ofSize(ItemFilterScreenHandler.SLOT_COUNT, ItemStack.EMPTY))
                .registryKey(key)
        );
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        final var playerStack = user.getStackInHand(hand);
        if (user.isSneaking()) {
            playerStack.apply(ENABLED, false, enabled -> !enabled);
        } else {
            if (world.isClient) return ActionResult.PASS;
            final var inv = new FilterInventory(playerStack);
            final var factory = new NamedScreenHandlerFactory() {
                @Override
                public @NotNull ScreenHandler createMenu(int syncId, PlayerInventory playerInv, PlayerEntity player) {
                    return new ItemFilterScreenHandler(syncId, playerInv, inv);
                }

                @Override
                public Text getDisplayName() {
                    return getName();
                }
            };
            user.openHandledScreen(factory);
            InteracticNetworking.CHANNEL.serverHandle(user).send(new SetFilterModePacket(inv.getFilterMode()));
        }
        return ActionResult.PASS;
    }

    public static List<Item> getItemsInFilter(ItemStack stack) {
        return stack.getOrDefault(FILTER_SLOTS, DefaultedList.<ItemStack>of()).stream().map(ItemStack::getItem).toList();
    }

    public static class FilterInventory implements Inventory {

        public final ItemStack filter;
        private final DefaultedList<ItemStack> items = DefaultedList.ofSize(ItemFilterScreenHandler.SLOT_COUNT, ItemStack.EMPTY);

        public FilterInventory(ItemStack filter) {
            this.filter = filter;

            var filterItems = filter.getOrDefault(FILTER_SLOTS, this.items);
            for (int i = 0; i < filterItems.size(); i++) {
                this.items.set(i, filterItems.get(i));
            }
        }

        public void setFilterMode(boolean mode) {
            this.filter.set(BLOCK_MODE, mode);
        }

        public boolean getFilterMode() {
            return this.filter.getOrDefault(BLOCK_MODE, false);
        }

        @Override
        public int size() {
            return ItemFilterScreenHandler.SLOT_COUNT;
        }

        @Override
        public boolean isEmpty() {
            return this.items.stream().allMatch(ItemStack::isEmpty);
        }

        @Override
        public ItemStack getStack(int slot) {
            return this.items.get(slot);
        }

        @Override
        public ItemStack removeStack(int slot, int amount) {
            var stack = this.items.get(slot).copy();
            this.items.set(slot, ItemStack.EMPTY);
            return stack;
        }

        @Override
        public ItemStack removeStack(int slot) {
            var stack = this.items.get(slot).copy();
            this.items.set(slot, ItemStack.EMPTY);
            return stack;
        }

        @Override
        public void setStack(int slot, ItemStack stack) {
            this.items.set(slot, stack);
        }

        @Override
        public void markDirty() {
            this.filter.set(FILTER_SLOTS, this.items);
        }

        @Override
        public boolean canPlayerUse(PlayerEntity player) {
            return player.getInventory().contains(filter);
        }

        @Override
        public void clear() {
            Collections.fill(this.items, ItemStack.EMPTY);
        }
    }

    public record InventoryEntry(ItemStack stack, int slot) {
        private static final ReflectiveEndecBuilder BUILDER = new ReflectiveEndecBuilder(MinecraftEndecs::addDefaults);

        public static final Endec<InventoryEntry> ENDEC = RecordEndec.create(BUILDER, InventoryEntry.class);
        public static final Endec<DefaultedList<ItemStack>> INVENTORY_ENDEC = InventoryEntry.ENDEC.listOf().xmap(
                entries -> {
                    var list = DefaultedList.ofSize(ItemFilterScreenHandler.SLOT_COUNT, ItemStack.EMPTY);
                    entries.forEach(entry -> list.set(entry.slot, entry.stack));
                    return list;
                }, stacks -> {
                    var entries = new ArrayList<InventoryEntry>();
                    for (int i = 0; i < stacks.size(); i++) {
                        if (stacks.get(i).isEmpty()) continue;
                        entries.add(new InventoryEntry(stacks.get(i), i));
                    }
                    return entries;
                }
        );
    }

    public record SetFilterModePacket(boolean mode) {}
}
