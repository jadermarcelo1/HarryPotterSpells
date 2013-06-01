package com.lavacraftserver.HarryPotterSpells.Spells;

import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;

import com.lavacraftserver.HarryPotterSpells.Spells.Spell.spell;

@spell (
		name="Confringo",
		description="Launches a fireball from the tip of your wand",
		range=0,
		goThroughWalls=false,
		cooldown=60
)
public class Confringo extends Spell {

	public boolean cast(Player p) {
		Fireball fb = p.launchProjectile(Fireball.class);
		fb.setShooter(p);
		fb.setYield(2);
		fb.setBounce(false);
		return true;
	}
	
}
