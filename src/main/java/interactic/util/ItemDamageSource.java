package interactic.util;

import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.ProjectileDamageSource;
import net.minecraft.item.*;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import org.jetbrains.annotations.Nullable;

public class ItemDamageSource extends ProjectileDamageSource {

    public ItemDamageSource(ItemEntity projectile, @Nullable Entity attacker) {
        super("thrown_item", projectile, attacker);
    }

    @Override
    public Text getDeathMessage(LivingEntity entity) {
        Text attackerName = this.getAttacker() == null ? this.source.getDisplayName() : this.getAttacker().getDisplayName();
        ItemStack itemStack = ((ItemEntity) this.getSource()).getStack();
        String key = "death.attack." + this.name;
        if (itemStack.getItem() instanceof SwordItem) key = key + ".sword";
        if (itemStack.getItem() instanceof AxeItem) key = key + ".axe";
        if (itemStack.getItem() instanceof PickaxeItem) key = key + ".pickaxe";
        if (itemStack.getItem() instanceof ShovelItem) key = key + ".shovel";
        if (itemStack.getItem() instanceof HoeItem) key = key + ".hoe";
        return new TranslatableText(key, entity.getDisplayName(), attackerName, itemStack.toHoverableText());
    }
}
