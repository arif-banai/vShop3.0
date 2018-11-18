package me.arifBanai.vShop.managers.database;

import java.sql.SQLException;
import java.sql.Statement;

import com.huskehhh.bukkitSQL.sqlite.SQLite;

import me.arifBanai.vShop.Main;

public class SQLiteManager extends DatabaseManager {
	
	public SQLiteManager(final Main instance) {
		super(instance);
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
