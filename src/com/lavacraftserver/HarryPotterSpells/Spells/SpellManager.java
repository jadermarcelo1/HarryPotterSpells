package com.lavacraftserver.HarryPotterSpells.Spells;

import java.util.ArrayList;
import java.util.logging.Level;

import org.bukkit.configuration.ConfigurationSection;
import org.reflections.Reflections;

import com.lavacraftserver.HarryPotterSpells.HarryPotterSpells;

public class SpellManager {
	private ArrayList<Spell> spellList = new ArrayList<Spell>();
	HarryPotterSpells plugin;
	
	public SpellManager(HarryPotterSpells instance){
		plugin=instance;
		Reflections ref = new Reflections("com.lavacraftserver.HarryPotterSpells.Spells");
		for(Class<?> clazz : ref.getTypesAnnotatedWith(Spell.spell.class)) {
			Spell spell;
			if(clazz == Spell.class || clazz == InvalidSpell.class || !Spell.class.isAssignableFrom(clazz))
				continue;
			try {
				spell = (Spell) clazz.getConstructor(HarryPotterSpells.class).newInstance(plugin);
			} catch (Exception e) {
				plugin.PM.log("An error occurred whilst adding the " + clazz.getName() + " spell to the spell list. That spell will not be available." , Level.WARNING);
				e.printStackTrace();
				continue;
			}
			spellList.add(spell);
		}
		
		load();
	}
	
	public Spell getSpell(String name) {
		for(Spell spell:spellList){
			if(spell.getName().equalsIgnoreCase(name)||spell.toString().equalsIgnoreCase(name)){
				return spell;
			}
		}
		return new InvalidSpell(plugin);
	}
	
	public void addSpell(Spell spell) {
		spellList.add(spell);
	}
	
	public ArrayList<Spell> getSpells() {
		return spellList;
	}
	
	public boolean isSpell(String name) {
		for(Spell spell:spellList){
			if(spell.getName().equalsIgnoreCase(name)||spell.toString().equalsIgnoreCase(name)){
				return true;
			}
		}
		return false;
	}
	
	public void save() {
		plugin.getConfig().createSection("spells");
		ConfigurationSection configSpells = plugin.getConfig().getConfigurationSection("spells");
		for(Spell s : spellList) {
			configSpells.createSection(s.getInternalName());
			configSpells.set(s.getInternalName(), s.save(configSpells.getConfigurationSection(s.getInternalName())));
		}
		plugin.getConfig().set("arenas", configSpells);
	}

	public void load() {
		try{
			if(!plugin.getConfig().isSet("spells"))
				plugin.getConfig().getConfigurationSection("spells");
			ConfigurationSection configSpells = plugin.getConfig().getConfigurationSection("spells");
			for(String k : configSpells.getKeys(false))
				getSpell(k).load(configSpells.getConfigurationSection(k));
		} catch(Exception e){
			//TODO catch this exception
		}
	}
	
}
