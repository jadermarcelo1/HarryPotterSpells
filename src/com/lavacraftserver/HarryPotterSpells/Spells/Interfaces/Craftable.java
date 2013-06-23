package com.lavacraftserver.HarryPotterSpells.Spells.Interfaces;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;

import com.lavacraftserver.HarryPotterSpells.API.SpellBookRecipe;

/**
 * Represents a spell that is craftable into a spell book (tome)<br>
 * It does not matter what you set the {@link Recipe} to return. 
 * The server will change the {@link ItemStack} to a generated book (or tome).
 */
public interface Craftable {

    SpellBookRecipe getCraftingRecipe();

}
