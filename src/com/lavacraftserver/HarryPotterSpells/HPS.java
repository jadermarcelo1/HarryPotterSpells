package com.lavacraftserver.HarryPotterSpells;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.help.GenericCommandHelpTopic;
import org.bukkit.help.HelpTopic;
import org.bukkit.help.IndexHelpTopic;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.reflections.Reflections;

import com.lavacraftserver.HarryPotterSpells.Metrics.Graph;
import com.lavacraftserver.HarryPotterSpells.API.events.SpellBookRecipeAddEvent;
import com.lavacraftserver.HarryPotterSpells.Commands.HCommand;
import com.lavacraftserver.HarryPotterSpells.Commands.HCommandExecutor;
import com.lavacraftserver.HarryPotterSpells.Extensions.ExtensionManager;
import com.lavacraftserver.HarryPotterSpells.Jobs.EnableJob;
import com.lavacraftserver.HarryPotterSpells.Jobs.JobManager;
import com.lavacraftserver.HarryPotterSpells.Spells.Spell;
import com.lavacraftserver.HarryPotterSpells.Spells.SpellManager;
import com.lavacraftserver.HarryPotterSpells.Spells.Interfaces.Craftable;
import com.lavacraftserver.HarryPotterSpells.Utils.MetricStatistics;
import com.lavacraftserver.HarryPotterSpells.Utils.SVPBypass;
import com.lavacraftserver.HarryPotterSpells.configuration.ConfigurationManager;

public class HPS extends JavaPlugin {
	public static ConfigurationManager ConfigurationManager;
	public static PM PM;
	public static SpellManager SpellManager;
	public static Wand Wand;
	@Deprecated public static ExtensionManager ExtensionManager;
	public static SpellTargeter SpellTargeter;
	public static Localisation Localisation;
	public static Plugin Plugin;
		
	private static CommandMap commandMap;
	private static Collection<HelpTopic> helpTopics = new ArrayList<HelpTopic>();
	
	@Override
	public void onEnable() {	    
	    // Instance loading 
	    // ORDER IS IMPORTANT.
	    Plugin = this;
	    PM = new PM();
	    Localisation = new Localisation();
		ConfigurationManager = new ConfigurationManager();
		SpellManager = new SpellManager();
		Wand = new Wand();
		//ExtensionManager = new ExtensionManager();

		// Configuration
		loadConfig();
				
		// Hacky command map stuff
		try {
		    Class<?> craftServer = SVPBypass.getCurrentCBClass("CraftServer");
		    if(craftServer == null)
		        throw new Throwable("Computer says no");
            Field f = craftServer.getDeclaredField("commandMap");
            f.setAccessible(true);
            commandMap = (CommandMap) f.get(getServer());
        } catch (Throwable e){
            PM.log(Level.SEVERE, Localisation.getTranslation("errCommandMap"));
            PM.debug(e);
        }
		
		Reflections reflections = Reflections.collect();
				
		// Reflections - Commands
		int commands = 0;
		for(Class<? extends HCommandExecutor> clazz : reflections.getSubTypesOf(HCommandExecutor.class)) {
		    if(clazz.getName().equals(HCommandExecutor.class.getName()))
		        continue;
		    else if(addHackyCommand(clazz))
				commands++;
		}
		PM.debug(Localisation.getTranslation("dbgRegisteredCoreCommands", commands));
		
	    Bukkit.getHelpMap().addTopic(new IndexHelpTopic("HarryPotterSpells", Localisation.getTranslation("hlpDescription"), "", helpTopics));
	    PM.debug(Localisation.getTranslation("dbgHelpCommandsAdded"));
		
		int enable = 0;
		for(Class<? extends EnableJob> clazz : reflections.getSubTypesOf(EnableJob.class)) {
		    try {
                JobManager.addEnableJob(clazz.newInstance());
                enable++;
            } catch (Exception e) {
                PM.log(Level.WARNING, "Could not add enable job in class " + clazz.getSimpleName() + " to the job manager.");
                PM.debug(e);
            }
		}
		
		PM.debug(Localisation.getTranslation("dbgRegisteredCoreEnable", enable));
		
		// Plugin Metrics
		try {
		    Metrics metrics = new Metrics(this);
		    
		    // Total amount of spells cast
		    metrics.addCustomData(new Metrics.Plotter("Total Amount of Spells Cast") {
                
                @Override
                public int getValue() {
                    return MetricStatistics.getSpellsCast();
                }
                
            });
		    
		    // Types of spell cast
		    Graph typesOfSpellCast = metrics.createGraph("Types of Spell Cast");
		    
		    for(final Spell spell : SpellManager.getSpells()) {
		        typesOfSpellCast.addPlotter(new Metrics.Plotter(spell.getName()) {
                    
                    @Override
                    public int getValue() {
                        return MetricStatistics.getAmountOfTimesCast(spell);
                    }
                    
                });
		    }
		    
		    // Spell success rate
		    Graph spellSuccessRate = metrics.createGraph("Spell Success Rate");
		    
		    spellSuccessRate.addPlotter(new Metrics.Plotter("Successes") {

                @Override
                public int getValue() {
                    return MetricStatistics.getSuccesses();
                }
		        
		    });
		    
		    spellSuccessRate.addPlotter(new Metrics.Plotter("Failures") {
                
                @Override
                public int getValue() {
                    return MetricStatistics.getFailures();
                }
            });
		    
		    metrics.start();
		} catch (IOException e) {
		    PM.log(Level.WARNING, Localisation.getTranslation("errPluginMetrics"));
		    PM.debug(e);
		}

		// Crafting Changes
        PM.debug(Localisation.getTranslation("dbgCraftingStart"));

		if(getConfig().getBoolean("wand.crafting.enabled", true)) {
		    try {
    		    ShapedRecipe wandRecipe = new ShapedRecipe(Wand.getLorelessWand());
    		    List<String> list = getConfig().getStringList("wand.crafting.recipe");
    		    Set<String> ingredients = getConfig().getConfigurationSection("wand.crafting.ingredients").getKeys(false);

    		    wandRecipe.shape(list.get(0), list.get(1), list.get(2));
    		    for(String string : ingredients)
    		        wandRecipe.setIngredient(string.toCharArray()[0], Material.getMaterial(getConfig().getInt("wand.crafting.ingredients." + string)));

    		    getServer().addRecipe(wandRecipe);
		    } catch(Exception e) { // It's surrounded by a try/catch block because we can't let any stupid errors in config disable the plugin.
		        PM.log(Level.INFO, Localisation.getTranslation("errCraftingChanges"));
		        PM.debug(e);
		    }
		}

		if(getConfig().getBoolean("spells-craftable", true)) {
		    for(Spell s : SpellManager.getSpells()) {
		        if(s instanceof Craftable) {
		            SpellBookRecipeAddEvent e = new SpellBookRecipeAddEvent(((Craftable) s).getCraftingRecipe());
		            getServer().getPluginManager().callEvent(e);
		            if(!e.isCancelled())
		                getServer().addRecipe(e.getRecipe());
		        }
		    }
		}

        PM.debug(Localisation.getTranslation("dbgCraftingEnd"));

		JobManager.executeEnableJobs(getServer().getPluginManager());

		PM.log(Level.INFO, Localisation.getTranslation("genPluginEnabled"));
	}
	
	@Override
	public void onDisable() {
		JobManager.executeClearJobs();
		JobManager.executeDisableJob(getServer().getPluginManager());

		PM.log(Level.INFO, Localisation.getTranslation("genPluginDisabled"));
	}
	
	public void loadConfig() {
		File file = new File(this.getDataFolder(), "config.yml");
		if(!file.exists()) {
			getConfig().options().copyDefaults(true);
			saveDefaultConfig();
		}
	}
	
	/**
	 * Registers a {@link HackyCommand} to the plugin
	 * @param clazz a class that extends {@code CommandExecutor}
	 * @return {@code true} if the command was added successfully
	 */
	public static boolean addHackyCommand(Class<? extends HCommandExecutor> clazz) {
		if(!clazz.isAnnotationPresent(HCommand.class)) {
			PM.log(Level.INFO, Localisation.getTranslation("errAddCommandMapAnnotation", clazz.getSimpleName()));
			return false;
		}
		
		HCommand cmdInfo = clazz.getAnnotation(HCommand.class);
		String name = cmdInfo.name().equals("") ? clazz.getSimpleName().toLowerCase() : cmdInfo.name();
		String permission = cmdInfo.permission().equals("") ? "HarryPotterSpells." + name : cmdInfo.permission();
		List<String> aliases = cmdInfo.aliases().equals("") ? new ArrayList<String>() : Arrays.asList(cmdInfo.aliases().split(","));
		Permission perm = new Permission(permission, PermissionDefault.getByName(cmdInfo.permissionDefault()));
		Bukkit.getServer().getPluginManager().addPermission(perm);
		HackyCommand hacky = new HackyCommand(name, Localisation.getTranslation(cmdInfo.description()), cmdInfo.usage(), aliases);
		hacky.setPermission(permission);
		hacky.setPermissionMessage(Localisation.getTranslation(cmdInfo.noPermissionMessage()));
		try {
			hacky.setExecutor(clazz.newInstance());
		} catch (Exception e) {
			PM.log(Level.WARNING, Localisation.getTranslation("errAddCommandMap", clazz.getSimpleName()));
			PM.debug(e);
			return false;
		}
		commandMap.register("", hacky);
		helpTopics.add(new GenericCommandHelpTopic(hacky));
		return true;
	}
	
	/**
	 * A very hacky class used to register commands at plugin runtime
	 */
	private static class HackyCommand extends Command {
		private CommandExecutor executor;

		public HackyCommand(String name, String description, String usageMessage, List<String> aliases) {
			super(name, description, usageMessage, aliases);
		}

		@Override
		public boolean execute(CommandSender sender, String commandLabel, String[] args) {
		    String s = sender instanceof Player ? "/" : "";
		    if(executor == null)
		        sender.sendMessage(Localisation.getTranslation("cmdUnknown", s));
		    else if(!sender.hasPermission(getPermission()))
		        PM.dependantMessagingWarn(sender, getPermissionMessage());
		    else {
		        boolean success = executor.onCommand(sender, this, commandLabel, args);
		        if(!success)
		            PM.dependantMessagingTell(sender, ChatColor.RED + Localisation.getTranslation("cmdUsage", s, getUsage().replace("<command>", commandLabel)));
		    }
		    return true;
		}
		
		/**
		 * Sets the executor to be used for this hacky command
		 * @param executor the executor
		 */
		public void setExecutor(CommandExecutor executor) {
			this.executor = executor;
		}
		
	}
	
}
