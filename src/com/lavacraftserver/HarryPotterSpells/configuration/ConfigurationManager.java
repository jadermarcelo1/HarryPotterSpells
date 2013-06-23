package com.lavacraftserver.HarryPotterSpells.configuration;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import com.lavacraftserver.HarryPotterSpells.HPS;

/**
 * Manages all custom configuration files within the plugin
 */
public class ConfigurationManager {
    private Map<ConfigurationType, CustomConfiguration> configurationMap = new HashMap<ConfigurationType, CustomConfiguration>();

    /**
     * Constructs a new {@link ConfigurationManager}
     */
    public ConfigurationManager() {
        configurationMap.put(ConfigurationType.PLAYER_SPELL_CONFIG, new PlayerSpellConfig(new File(HPS.Plugin.getDataFolder(), "PlayerSpellConfig.yml"), HPS.class.getResourceAsStream("PlayerSpellConfig.yml")));
    }

    /**
     * Gets a {@link CustomConfiguration} that represents the given {@link Type} of configuration
     * @param type what configuration file you want to retrieve
     * @return the configuration file
     */
    public CustomConfiguration getConfig(ConfigurationType type) {
        return configurationMap.get(type);
    }

    /**
     * An enum representing all the different types of {@link CustomConfiguration} that exist
     */
    public enum ConfigurationType {
        /**
         * Stores a list of the spells that every player knows
         */
        PLAYER_SPELL_CONFIG;
    }

}
