package me.arifbanai.vShop.managers.database;

import me.arifbanai.idLogger.IDLogger;
import me.arifbanai.vShop.Main;
import me.huskehhh.bukkitSQL.sqlite.SQLite;

import java.sql.SQLException;
import java.sql.Statement;

public class SQLiteManager extends DatabaseManager {
	
	public SQLiteManager(final Main plugin, final IDLogger instance) {
		super(plugin, instance);
	}

	@Override
	public void setupDb() throws ClassNotFoundException, SQLException {
		db = new SQLite(plugin, "vShop.db");
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
	public void closeDb() throws ClassNotFoundException, SQLException {
		db.closeConnection();
	}
}
