package me.arifbanai.vShop;

import me.arifbanai.easypool.DataSourceManager;
import me.arifbanai.easypool.MySQLDataSourceManager;
import me.arifbanai.easypool.SQLiteDataSourceManager;
import me.arifbanai.idLogger.IDLogger;
import me.arifbanai.vShop.commands.*;
import me.arifbanai.vShop.exceptions.DataSourceSetupFailedException;
import me.arifbanai.vShop.exceptions.EconomySetupFailedException;
import me.arifbanai.vShop.managers.QueryManager;
import me.arifbanai.vShop.managers.sql.SqlQueryManager;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;


public class VShop extends JavaPlugin {

	public static Economy economy = null;

	private IDLogger idLogger;
	private DataSourceManager dataSourceManager;

	private QueryManager queryManager;

	@Override
	// This method executes when the plugin is initialized by the server
	public void onEnable() {
		this.saveDefaultConfig();
		this.saveResource("hikari.properties", false);

		String path = getDataFolder().toPath().toString();
		System.setProperty("hikaricp.configurationFile", path + "/hikari.properties");

		idLogger = (IDLogger) Bukkit.getPluginManager().getPlugin("IDLogger");

		if (!setupEconomy()) {
			handleUnexpectedException(new EconomySetupFailedException());
		}

		if(!setupDataSourceManager()) {
			handleUnexpectedException(new DataSourceSetupFailedException());
		}

		queryManager = new SqlQueryManager(this, dataSourceManager);

		try {
			queryManager.prepareDB();
		} catch (Exception e) {
			handleUnexpectedException(e);
		}

		setupCommands();
	}

	@Override
	public void onDisable() {
		getLogger().info("Disabling plugin...");
		dataSourceManager.close();
	}

	/**
	 * Setup Vault hook in provided economy system
	 * @return true if successful, false otherwise
	 */
	private boolean setupEconomy() {
		RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager()
				.getRegistration(net.milkbowl.vault.economy.Economy.class);
		if (economyProvider != null) {
			economy = economyProvider.getProvider();
		}

		return (economy != null);
	}

	/**
	 * Setup the DSM with some RDBMS specified in config
	 * @return true if setup was successful, false if failed
	 */
	private boolean setupDataSourceManager() {
		FileConfiguration config = getConfig();
		dataSourceManager = null;

		if(config.getBoolean("using-sqlite", true)) {
			String path = getDataFolder().toPath().toString();
			try {
				dataSourceManager = new SQLiteDataSourceManager(path, this.getName());
			} catch (IOException e) {
				handleUnexpectedException(e);
			}
		} else {
			String host = config.getString("db.host");
			String port = config.getString("db.port");
			String schema = config.getString("db.schema");
			String user = config.getString("db.username");
			String password = config.getString("db.password");
			//TODO String dialect = config.getString("db.dialect");
			//TODO Use a switch to handle multiple sql dialects

			dataSourceManager = new MySQLDataSourceManager(host, port, schema, user, password);

		}

		return (dataSourceManager != null);
	}

	/**
	 * Enable the commands
	 */
	private void setupCommands() {
		getCommand("buy").setExecutor(new Buy(this, queryManager));
		getCommand("sell").setExecutor(new Sell(this, queryManager));
		getCommand("find").setExecutor(new Find(this, queryManager, idLogger));
		getCommand("stock").setExecutor(new Stock(this, queryManager, idLogger));
		getCommand("recall").setExecutor(new Recall(this, queryManager));
		//TODO getCommand("sales").setExecutor(new Sales(this));
		
		//TODO more commands related to Transactions
	}

	/**
	 * Log the error in the exception and disable the plugin
	 * @param e The exception that caused a fatal error for the plugin
	 */
	public void handleUnexpectedException(Exception e) {
		this.getLogger().severe(e.toString());
		e.printStackTrace();
		this.getServer().getPluginManager().disablePlugin(this);
	}
}
