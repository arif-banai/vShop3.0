package me.arifbanai.vShop.managers;

import org.bukkit.plugin.java.JavaPlugin;

public class ConfigManager {

	protected JavaPlugin plugin;

	public ConfigManager(final JavaPlugin instance) {
		plugin = instance;
	}

	public boolean broadcastOffers() {
		return plugin.getConfig().getBoolean("broadcast-offers", true);
	}

	public boolean logTransactions() {
		return plugin.getConfig().getBoolean("log-transactions", false);
	}

	public boolean usingMySQL() {
		return plugin.getConfig().getBoolean("using-mysql", false);
	}

	public String getHost() {
		return plugin.getConfig().getString("MySQL.host", "localhost");
	}

	public String getPort() {
		return plugin.getConfig().getString("MySQL.port", "3306");
	}

	public String getDatabase() {
		return plugin.getConfig().getString("MySQL.database", "minecraft");
	}

	public String getUsername() {
		return plugin.getConfig().getString("MySQL.username", "root");
	}

	public String getPassword() {
		return plugin.getConfig().getString("MySQL.password", "password");
	}
}
