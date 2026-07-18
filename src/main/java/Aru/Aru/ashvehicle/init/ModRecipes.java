package Aru.Aru.ashvehicle.init;

import Aru.Aru.ashvehicle.AshVehicle;
import Aru.Aru.ashvehicle.recipe.JerryCanRefillRecipe;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

public class ModRecipes {
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(BuiltInRegistries.RECIPE_SERIALIZER, AshVehicle.MODID);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<JerryCanRefillRecipe>> JERRY_CAN_REFILL =
            SERIALIZERS.register("jerry_can_refill", () -> new SimpleCraftingRecipeSerializer<>(JerryCanRefillRecipe::new));
}
