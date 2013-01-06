package com.lavacraftserver.HarryPotterSpells.Spells;

import org.bukkit.entity.Player;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import com.lavacraftserver.HarryPotterSpells.HarryPotterSpells;
import com.lavacraftserver.HarryPotterSpells.Spells.Spell.spell;

@spell (
		name="Reparo",
		description="Repairs the item in your hand fully",
		range=0,
		goThroughWalls=false
)
public class Reparo extends Spell {

	public Reparo(HarryPotterSpells instance) {
		super(instance);
	}

	@Override
	public void cast(Player p) {
		repairItems(p.getInventory().getContents(), p);
	}
	
	private void repairItem(final ItemStack item) {
		final Material material = Material.getMaterial(item.getTypeId());
		if (material.isBlock() || material.getMaxDurability() < 1) {
			return;
		}
		item.setDurability((short) 0);
	}
	
	private void repairItems(ItemStack[] itemstack, Player p) {
		for (ItemStack item : itemstack) {
			if (item == null) {
				continue;
			}
			repairItem(item);
		}
	}
}
