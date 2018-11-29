package main.java.arifbanai.vShop.managers.database;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import org.bukkit.plugin.java.JavaPlugin;

import main.java.arifbanai.vShop.objects.Offer;
import main.java.arifbanai.vShop.objects.Transaction;
import main.java.huskehhh.bukkitSQL.Database;



public abstract class DatabaseManager {

	protected Database db;
	protected JavaPlugin plugin;

	public DatabaseManager(final JavaPlugin instance) {
		plugin = instance;

	}

	public abstract void setupDb() throws ClassNotFoundException, SQLException;

	public abstract void closeDb() throws ClassNotFoundException, SQLException;

	public void addOffer(Offer offer) throws SQLException, ClassNotFoundException {

		PreparedStatement safeStatement;
		safeStatement = db.getConnection()
				.prepareStatement("INSERT INTO " + "stock(sellerUUID,textID,amount,price)" + "VALUES(?,?,?,?)");

		safeStatement.setString(1, offer.sellerUUID);
		safeStatement.setString(2, offer.textID);
		safeStatement.setInt(3, offer.amount);
		safeStatement.setDouble(4, offer.price);

		safeStatement.executeUpdate();
	}

	public void deleteOffer(String playerUUID, String textID) throws SQLException, ClassNotFoundException {

		PreparedStatement safeStatement;
		safeStatement = db.getConnection()
				.prepareStatement("DELETE FROM stock WHERE " + "sellerUUID = ? AND textID = ?");

		safeStatement.setString(1, playerUUID);
		safeStatement.setString(2, textID);

		safeStatement.executeUpdate();
	}

	public void updateQuantity(String sellerUUID, String textID, int newQuantity)
			throws SQLException, ClassNotFoundException {

		PreparedStatement safeStatement;
		safeStatement = db.getConnection()
				.prepareStatement("UPDATE stock SET " + "amount = ? WHERE " + "sellerUUID = ? AND textID = ?");

		safeStatement.setInt(1, newQuantity);
		safeStatement.setString(2, sellerUUID);
		safeStatement.setString(3, textID);

		safeStatement.executeUpdate();
	}	

	public List<Offer> getItemOffers(String textID) throws SQLException, ClassNotFoundException {

		PreparedStatement safeStatement;
		safeStatement = db.getConnection()
				.prepareStatement("SELECT * FROM stock WHERE " + "textID = ? ORDER BY price ASC");

		safeStatement.setString(1, textID);

		return Offer.listOffers(safeStatement.executeQuery());
	}

	public List<Offer> searchBySeller(String sellerUUID) throws SQLException, ClassNotFoundException {

		PreparedStatement safeStatement;
		safeStatement = db.getConnection().prepareStatement("SELECT * FROM stock WHERE " + "sellerUUID = ?");

		safeStatement.setString(1, sellerUUID);

		return Offer.listOffers(safeStatement.executeQuery());
	}

	public List<Offer> getPrices(String textID) throws SQLException, ClassNotFoundException {

		PreparedStatement safeStatement;
		safeStatement = db.getConnection()
				.prepareStatement("SELECT * FROM stock WHERE " + "textID = ? ORDER BY price ASC LIMIT 0,10");

		safeStatement.setString(1, textID);

		return Offer.listOffers(safeStatement.executeQuery());
	}

	public List<Offer> getSellerOffers(String sellerUUID, String textID) throws SQLException, ClassNotFoundException {

		PreparedStatement safeStatement;
		safeStatement = db.getConnection()
				.prepareStatement("SELECT * FROM stock WHERE " + "sellerUUID = ? AND textID = ?");

		safeStatement.setString(1, sellerUUID);
		safeStatement.setString(2, textID);

		return Offer.listOffers(safeStatement.executeQuery());
	}

	public void logTransaction(Transaction transaction) throws SQLException, ClassNotFoundException {

		PreparedStatement safeStatement;
		safeStatement = db.getConnection().prepareStatement(
				"INSERT INTO " + "transactions(sellerUUID,buyerUUID,textID,amount,price)" + "VALUES(?,?,?,?,?)");

		safeStatement.setString(1, transaction.sellerUUID);
		safeStatement.setString(2, transaction.buyerUUID);
		safeStatement.setString(3, transaction.textID);
		safeStatement.setInt(4, transaction.amount);
		safeStatement.setDouble(5, transaction.price);

		safeStatement.executeUpdate();
	}

	public List<Transaction> getAllTransactions() throws SQLException, ClassNotFoundException {

		PreparedStatement safeStatement;
		safeStatement = db.getConnection().prepareStatement("SELECT * FROM transactions " + "ORDER BY id DESC");

		return Transaction.listTransactions(safeStatement.executeQuery());
	}

	public List<Transaction> getTransactionsBySeller(String sellerUUID) throws SQLException, ClassNotFoundException {

		PreparedStatement safeStatement;
		safeStatement = db.getConnection()
				.prepareStatement("SELECT * FROM transactions " + "WHERE sellerUUID = ? " + "ORDER BY id DESC");

		safeStatement.setString(1, sellerUUID);

		return Transaction.listTransactions(safeStatement.executeQuery());
	}

	public List<Transaction> getTransactionsByBuyer(String buyerUUID) throws SQLException, ClassNotFoundException {

		PreparedStatement safeStatement;
		safeStatement = db.getConnection().prepareStatement("SELECT * FROM transactions " 
						+ "WHERE buyerUUID = ? " 
						+ "ORDER BY id DESC");

		safeStatement.setString(1, buyerUUID);

		return Transaction.listTransactions(safeStatement.executeQuery());
	}

	public List<Transaction> getTransactionsBySellerBuyer(String sellerUUID, String buyerUUID) throws SQLException, ClassNotFoundException {

		PreparedStatement safeStatement;
		safeStatement = db.getConnection().prepareStatement("SELECT * FROM transactions " 
						+ "WHERE sellerUUID = ? " 
						+ "AND buyerUUID = ? "
						+ "ORDER BY id DESC");

		safeStatement.setString(1, sellerUUID);
		safeStatement.setString(2, buyerUUID);

		return Transaction.listTransactions(safeStatement.executeQuery());
	}

	public List<Transaction> getTransactionsBySellerBuyerAndItem(String sellerUUID, String buyerUUID, String textID) throws SQLException, ClassNotFoundException {

		PreparedStatement safeStatement;
		safeStatement = db.getConnection().prepareStatement("SELECT * FROM transactions " 
						+ "WHERE sellerUUID = ? " 
						+ "AND buyerUUID = ? "
						+ "AND textID = ? "
						+ "ORDER BY id DESC");

		safeStatement.setString(1, sellerUUID);
		safeStatement.setString(2, buyerUUID);
		safeStatement.setString(3, textID);

		return Transaction.listTransactions(safeStatement.executeQuery());
	}

}
