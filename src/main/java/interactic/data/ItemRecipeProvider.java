package interactic.data;

import interactic.InteracticInit;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.advancement.criterion.InventoryChangedCriterion;
import net.minecraft.data.recipe.RecipeExporter;
import net.minecraft.data.recipe.RecipeGenerator;
import net.minecraft.data.recipe.ShapedRecipeJsonBuilder;
import net.minecraft.item.Items;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;

import java.util.concurrent.CompletableFuture;

public class ItemRecipeProvider extends FabricRecipeProvider {

    public ItemRecipeProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected RecipeGenerator getRecipeGenerator(RegistryWrapper.WrapperLookup registryLookup, RecipeExporter exporter) {
        return new RecipeGenerator(registryLookup, exporter) {
            @Override
            public void generate() {
                var itemLookup = registryLookup.getOrThrow(RegistryKeys.ITEM);
                ShapedRecipeJsonBuilder.create(itemLookup, RecipeCategory.TOOLS, InteracticInit.getItemFilter())
                        .criterion("get_item", InventoryChangedCriterion.Conditions.items(Items.CRAFTING_TABLE))
                        .pattern(" c ")
                        .pattern("cec")
                        .pattern(" c ")
                        .input('c', Items.COPPER_INGOT)
                        .input('e', Items.ENDER_EYE)
                        .offerTo(exporter);
            }
        };
    }

    @Override
    public String getName() {
        return "recipe";
    }
}
