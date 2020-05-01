package me.arifbanai.vShop;

import me.arifbanai.easypool.DataSourceManager;
import me.arifbanai.easypool.MySQLDataSourceManager;
import me.arifbanai.easypool.SQLiteDataSourceManager;
import me.arifbanai.idLogger.IDLogger;
import me.arifbanai.vShop.commands.*;
import me.arifbanai.vShop.managers.QueryManager;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.sql.SQLException;


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
			getLogger().info("Problem depending Vault. Disabling...");
			getServer().getPluginManager().disablePlugin(this);
			return;
		}

		if(!initDataSourceManager()) {
			handleUnexpectedException(new Exception("Failed to initialize DataSourceManager."));
		}

		queryManager = new QueryManager(this, idLogger, dataSourceManager);

		try {
			queryManager.prepareDB();
		} catch (SQLException sqlException) {
			handleUnexpectedException(sqlException);
		}

		setupCommands();
	}

	@Override
	public void onDisable() {
		getLogger().info("Disabling plugin...");
		queryManager.close();
	}

	private boolean setupEconomy() {
		RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager()
				.getRegistration(net.milkbowl.vault.economy.Economy.class);
		if (economyProvider != null) {
			economy = economyProvider.getProvider();
		}

		return (economy != null);
	}

	private boolean initDataSourceManager() {
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

	private void setupCommands() {
		getCommand("buy").setExecutor(new Buy(this, queryManager));
		getCommand("sell").setExecutor(new Sell(this, queryManager));
		getCommand("find").setExecutor(new Find(this, queryManager, idLogger));
		getCommand("stock").setExecutor(new Stock(this, queryManager, idLogger));
		getCommand("recall").setExecutor(new Recall(this, queryManager));
		//TODO getCommand("sales").setExecutor(new Sales(this));
		
		//TODO more commands related to Transactions
	}

	public void handleUnexpectedException(Exception e) {
		this.getLogger().severe(e.toString());
		e.printStackTrace();
		this.getServer().getPluginManager().disablePlugin(this);
	}
}
