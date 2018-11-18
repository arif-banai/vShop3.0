package me.arifBanai.vShop.managers.database;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;

import com.huskehhh.bukkitSQL.mysql.MySQL;

import me.arifBanai.vShop.Main;
import me.arifBanai.vShop.managers.ConfigManager;

public class MySQLManager extends DatabaseManager {

	public MySQLManager(final Main plugin) {
		super(plugin);
	}

	@Override
	public void setupDb() throws ClassNotFoundException, SQLException {
		ConfigManager config = new ConfigManager(plugin);

		plugin.getLogger().log(Level.INFO, "Using MySQL");

		db = new MySQL(plugin, config.getHost(), config.getPort(), config.getDatabase(), config.getUsername(),
				config.getPassword());
		db.openConnection();
		
		Statement statement = db.getConnection().createStatement();
		statement.executeUpdate(
				"CREATE TABLE IF NOT EXISTS stock ("
				+ "sellerUUID VARCHAR(40) NOT NULL,"
				+ "textID VARCHAR(255) NOT NULL,"
				+ "amount INT NOT NULL,"
				+ "price DOUBLE NOT NULL,"
				+ "PRIMARY KEY (sellerUUID, textID)"
				+ ");");
		
		statement.executeUpdate(
				"CREATE TABLE IF NOT EXISTS transactions ("
				+ "id INT NOT NULL AUTO_INCREMENT,"
				+ "sellerUUID VARCHAR(40) NOT NULL,"
				+ "buyerUUID VARCHAR(40) NOT NULL,"
				+ "textID VARCHAR(255) NOT NULL,"
				+ "amount INT NOT NULL,"
				+ "price DOUBLE NOT NULL,"
				+ "PRIMARY KEY (id),"
				+ "KEY (sellerUUID),"
				+ "KEY (buyerUUID),"
				+ "KEY (textID)"
				+ ");");
		
		statement.close();
	}

	@Override
	public void closeDb() throws SQLException {
		plugin.getLogger().log(Level.INFO, "Disconnecting MySQL");
		db.closeConnection();
	}
}
