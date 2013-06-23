package com.lavacraftserver.HarryPotterSpells.Spells;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.plugin.PluginManager;

import com.lavacraftserver.HarryPotterSpells.HPS;
import com.lavacraftserver.HarryPotterSpells.Jobs.EnableJob;
import com.lavacraftserver.HarryPotterSpells.Spells.Spell.SpellInfo;

@SpellInfo(
		name = "Wingardium Leviosa", 
		description = "descWingardiumLeviosa", 
		range = 0, 
		goThroughWalls = false,
		cooldown=180
)
public class WingardiumLeviosa extends Spell implements Listener, EnableJob {
	private List<String> players = new ArrayList<String>();
	
	@Override
	public void onEnable(PluginManager pm) {
	    pm.registerEvents(this, HPS.Plugin);
	}

	public boolean cast(final Player p) {
		if (players.contains(p.getName())) {
			p.setFlying(false);
			p.setAllowFlight(false);
			players.remove(p.getName());
		} else {
			p.setAllowFlight(true);
			p.setFlying(true);
			if(((Boolean) getConfig("cancel-fall-damage", true)) && !players.contains(p.getName())){
				players.add(p.getName());
			}
						
			Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(HPS.Plugin, new Runnable() {
			    
			    @Override
				public void run() {
					if (players.contains(p.getName())) {
						p.setFlying(false);
						p.setAllowFlight(false);
						players.remove(p.getName());
					}
				}
			    
			}, getTime("duration", 200l));
		}
		return true;
	}

	@EventHandler
	public void onPlayerDamage(EntityDamageEvent e) {
		if (e.getCause() == DamageCause.FALL && e.getEntity() instanceof Player) {
			Player p = (Player) e.getEntity();
			if (players.contains(p.getName()))
				e.setDamage(0);
		}
	}
	
}
