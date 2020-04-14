package me.arifbanai.vShop;

import main.java.arifbanai.idLogger.IDLogger;
import me.arifbanai.vShop.commands.*;
import me.arifbanai.vShop.managers.ConfigManager;
import me.arifbanai.vShop.managers.database.DatabaseManager;
import me.arifbanai.vShop.managers.database.MySQLManager;
import me.arifbanai.vShop.managers.database.SQLiteManager;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;
import java.util.logging.Level;


public class Main extends JavaPlugin {

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
		//Nothing really needs to be here
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
		getCommand("sales").setExecutor(new Sales(this));
		
		//TODO more commands related to Transactions
	}

}
