package interactic.data;

import interactic.InteracticInit;
import net.fabricmc.fabric.api.client.datagen.v1.provider.FabricModelProvider;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.client.data.*;
import net.minecraft.client.render.item.model.ItemModel;
import net.minecraft.item.Item;

public class ItemModelProvider extends FabricModelProvider {

    public ItemModelProvider(FabricDataOutput output) {
        super(output);
    }

    @Override
    public void generateBlockStateModels(BlockStateModelGenerator blockStateModelGenerator) {

    }

    @Override
    public void generateItemModels(ItemModelGenerator itemModelGenerator) {
        Item item = InteracticInit.getItemFilter();

        TextureMap textures = TextureMap.layer0(TextureMap.getSubId(item, "_disabled"));
        Models.GENERATED.upload(ModelIds.getItemModelId(item), textures, itemModelGenerator.modelCollector);
        ItemModel.Unbaked unbaked = ItemModels.basic(ModelIds.getItemModelId(item));

        TextureMap texturesEnabled = TextureMap.layer0(TextureMap.getSubId(item, "_enabled"));
        Models.GENERATED.upload(ModelIds.getItemSubModelId(item, "_enabled"), texturesEnabled, itemModelGenerator.modelCollector);
        ItemModel.Unbaked unbakedEnabled = ItemModels.basic(ModelIds.getItemSubModelId(item, "_enabled"));

        itemModelGenerator.registerCondition(item, new EnabledProperty(), unbakedEnabled, unbaked);
    }
}
