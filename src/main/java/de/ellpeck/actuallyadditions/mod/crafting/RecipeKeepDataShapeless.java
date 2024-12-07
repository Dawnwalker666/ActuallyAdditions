/*
 * This file ("RecipeKeepDataShapeless.java") is part of the Actually Additions mod for Minecraft.
 * It is created and owned by Ellpeck and distributed
 * under the Actually Additions License to be found at
 * http://ellpeck.de/actaddlicense
 * View the source code at https://github.com/Ellpeck/ActuallyAdditions
 *
 * © 2015-2017 Ellpeck
 */

package de.ellpeck.actuallyadditions.mod.crafting;

import com.mojang.serialization.MapCodec;
import de.ellpeck.actuallyadditions.mod.ActuallyAdditions;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapelessRecipe;

import javax.annotation.Nonnull;

public class RecipeKeepDataShapeless extends ShapelessRecipe {
    public static String NAME = "copy_nbt_shapeless";
    public RecipeKeepDataShapeless(String pGroup, CraftingBookCategory pCategory, ItemStack pResult, NonNullList<Ingredient> pIngredients) {
        super(pGroup, pCategory, pResult, pIngredients);
    }
    public RecipeKeepDataShapeless(ShapelessRecipe recipe) {
        super(recipe.getGroup(), recipe.category(), recipe.getResultItem(RegistryAccess.EMPTY), recipe.getIngredients());
    }

    @Nonnull
    @Override
    public RecipeSerializer<?> getSerializer() {
        return ActuallyRecipes.KEEP_DATA_SHAPELESS_RECIPE.get();
    }

    @Nonnull
    @Override
    public ItemStack assemble(@Nonnull CraftingInput pContainer, @Nonnull HolderLookup.Provider provider) {
        ItemStack result = super.assemble(pContainer, provider);

        TargetComponentIngredient donorIngredient = null;
        ItemStack datasource = ItemStack.EMPTY;
        NonNullList<Ingredient> ingredients = getIngredients();
        for (Ingredient ingredient : ingredients) {
            if (ingredient.getCustomIngredient() instanceof TargetComponentIngredient) {
                donorIngredient = (TargetComponentIngredient)ingredient.getCustomIngredient();
                break;
            }
        }

        if (donorIngredient != null && !pContainer.isEmpty()) {
            for (int i = 0; i < pContainer.size(); i++) {
                final ItemStack item = pContainer.getItem(i);
                if (!item.isEmpty() && donorIngredient.test(item)) {
                    datasource = item;
                    break;
                }
            }
        }

        if (!datasource.isEmpty() && !datasource.getComponents().isEmpty())
            result.applyComponents(datasource.getComponents());
        else {
            ActuallyAdditions.LOGGER.info("AA.KeepDataShapeless missing TargetComponentIngredient");
            return ItemStack.EMPTY;
        }

        return result;
    }

    public static class Serializer implements RecipeSerializer<RecipeKeepDataShapeless> {
        public static final MapCodec<RecipeKeepDataShapeless> CODEC = ShapelessRecipe.Serializer.CODEC.xmap(RecipeKeepDataShapeless::new, $ -> $);
        public static final StreamCodec<RegistryFriendlyByteBuf, RecipeKeepDataShapeless> STREAM_CODEC = StreamCodec.of(
                RecipeKeepDataShapeless.Serializer::toNetwork, RecipeKeepDataShapeless.Serializer::fromNetwork
        );

        @Nonnull
        @Override
        public MapCodec<RecipeKeepDataShapeless> codec() {
            return CODEC;
        }

        @Nonnull
        @Override
        public StreamCodec<RegistryFriendlyByteBuf, RecipeKeepDataShapeless> streamCodec() {
            return STREAM_CODEC;
        }

        public static RecipeKeepDataShapeless fromNetwork(RegistryFriendlyByteBuf pBuffer) {
            return new RecipeKeepDataShapeless(RecipeSerializer.SHAPELESS_RECIPE.streamCodec().decode((pBuffer)));
        }

        public static void toNetwork(RegistryFriendlyByteBuf pBuffer, RecipeKeepDataShapeless pRecipe) {
            try {
                RecipeSerializer.SHAPELESS_RECIPE.streamCodec().encode(pBuffer, pRecipe);
            }
            catch (Exception e) {
                ActuallyAdditions.LOGGER.info("Failed to serialize {} Recipe to packet: {}", NAME, e.getMessage());
                throw e;
            }
        }
    }
}
