package me.arifbanai.vShop;

import me.arifbanai.easypool.DataSourceManager;
import me.arifbanai.easypool.MySQLDataSourceManager;
import me.arifbanai.easypool.SQLiteDataSourceManager;
import me.arifbanai.easypool.enums.DataSourceType;
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

		// Set location of hikari properties file to System property
		// When HikariConfig default constructor is called, the properties file is loaded
		// see https://github.com/brettwooldridge/HikariCP (ctrl+F system property)
		String path = getDataFolder().toPath().toString();
		System.setProperty("hikaricp.configurationFile", path + "/hikari.properties");

		idLogger = (IDLogger) Bukkit.getPluginManager().getPlugin("IDLogger");

		if (!setupEconomy()) {
			handleUnexpectedException(new EconomySetupFailedException());
		}

		if(!setupDsmAndQueryManager()) {
			handleUnexpectedException(new DataSourceSetupFailedException());
		}

		try {
			queryManager.prepareDB();
		} catch (Exception e) {
			handleUnexpectedException(e);
		}

		setupCommands();
	}

	@Override
	public void onDisable() {
		getLogger().info("Disabling datasource...");
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
	 * Initialize the {@link QueryManager} with the right DSM implementation
	 * @return true if setup was successful, false if failed
	 */
	private boolean setupDsmAndQueryManager() {
		FileConfiguration config = getConfig();
		dataSourceManager = null;

		if(config.getBoolean("using-sqlite", true)) {
			String path = getDataFolder().toPath().toString();
			try {
				dataSourceManager = new SQLiteDataSourceManager(path, this.getName());
				queryManager = new SqlQueryManager(this, dataSourceManager);
			} catch (IOException e) {
				handleUnexpectedException(e);
			}
		} else {
			String host = config.getString("db.host");
			String port = config.getString("db.port");
			String schema = config.getString("db.schema");
			String user = config.getString("db.username");
			String password = config.getString("db.password");
			String dialect = config.getString("db.dialect");

			if (dialect == null)  {
				handleUnexpectedException(new DataSourceSetupFailedException("DB dialect is null"));
				return false;
			}

			// Use a switch to handle multiple sql dialects
			switch(DataSourceType.matchDialect(dialect)) {
				case MYSQL:
					dataSourceManager = new MySQLDataSourceManager(host, port, schema, user, password);
					queryManager = new SqlQueryManager(this, dataSourceManager);
					break;
				default:
					handleUnexpectedException(new DataSourceSetupFailedException("Unable to resolve DB dialect"));
					break;
			}
		}

		return (dataSourceManager != null && queryManager != null);
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
