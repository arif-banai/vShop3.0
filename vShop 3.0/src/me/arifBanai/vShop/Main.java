package me.arifBanai.vShop;

import java.sql.SQLException;
import java.util.logging.Level;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import me.arifBanai.idLogger.IDLogger;
import me.arifBanai.vShop.commands.Buy;
import me.arifBanai.vShop.commands.Find;
import me.arifBanai.vShop.commands.Recall;
import me.arifBanai.vShop.commands.Sell;
import me.arifBanai.vShop.commands.Stock;
import me.arifBanai.vShop.commands.Test;
import me.arifBanai.vShop.managers.ConfigManager;
import me.arifBanai.vShop.managers.database.DatabaseManager;
import me.arifBanai.vShop.managers.database.MySQLManager;
import me.arifBanai.vShop.managers.database.SQLiteManager;

public class Main extends JavaPlugin {

	//TODO Bug list:
	//TODO Offers not being removed from database when amount = 1

	public static Economy economy = null;

	private ConfigManager configMan;
	private DatabaseManager db;
	private IDLogger idLogger;

	@Override
	// This method executes when the plugin is initialized by the server
	public void onEnable() {
		this.saveDefaultConfig();

		configMan = new ConfigManager(this);
		idLogger = (IDLogger) Bukkit.getPluginManager().getPlugin("IDLogger");

		if (!setupEconomy()) {
			this.getLogger().info("Problem depending Vault. Disabling...");
			this.getServer().getPluginManager().disablePlugin(this);
			return;
		}

		if (configMan.usingMySQL()) {
			db = new MySQLManager(this);
			try {
				db.setupDb();
			} catch (ClassNotFoundException | SQLException e) {
				this.getLogger().log(Level.SEVERE, "Unable to connect to MySQL. Displaying stack-trace.");
				e.printStackTrace();
				this.getServer().getPluginManager().disablePlugin(this);
			}
		} else {
			db = new SQLiteManager(this);

			try {
				db.setupDb();
			} catch (ClassNotFoundException | SQLException e) {
				this.getLogger().log(Level.SEVERE, "Unable to use SQLite. Displaying stack-trace.");
				e.printStackTrace();
				this.getServer().getPluginManager().disablePlugin(this);
			}
		}

		setupCommands();
	}

	@Override
	public void onDisable() {
		// TODO not sure anything needs to go here
	}

	public DatabaseManager getSQL() {
		return db;
	}
	
	public IDLogger getIDLogger() {
		return idLogger;
	}

	public ConfigManager getConfigManager() {
		return configMan;
	}

	// disables plugin
	public void disablePlugin() {
		this.getLogger().log(Level.SEVERE, "Unable to connect to MySQL. Shutting down plugin...");
		this.getServer().getPluginManager().disablePlugin(this);
	}

	private boolean setupEconomy() {
		RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager()
				.getRegistration(net.milkbowl.vault.economy.Economy.class);
		if (economyProvider != null) {
			economy = economyProvider.getProvider();
		}

		return (economy != null);
	}

	private void setupCommands() {
		getCommand("buy").setExecutor(new Buy(this));
		getCommand("sell").setExecutor(new Sell(this));
		getCommand("find").setExecutor(new Find(this));
		getCommand("stock").setExecutor(new Stock(this));
		getCommand("recall").setExecutor(new Recall(this));
		// TODO add sales command once implemented
		
		//Test command to help with debugging
		getCommand("test").setExecutor(new Test(this));
	}

}
