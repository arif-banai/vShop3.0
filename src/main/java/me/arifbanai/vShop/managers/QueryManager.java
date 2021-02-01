package me.arifbanai.vShop.managers;

import me.arifbanai.easypool.DataSourceManager;
import me.arifbanai.easypool.enums.DataSourceType;
import me.arifbanai.idLogger.IDLogger;
import me.arifbanai.vShop.exceptions.OffersNotFoundException;
import me.arifbanai.vShop.interfaces.VShopCallback;
import me.arifbanai.vShop.objects.Offer;
import me.arifbanai.vShop.objects.Transaction;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.*;
import java.util.List;
import java.util.logging.Level;

//TODO Optimize SQL statements for efficiency
//TODO javadoc

public class QueryManager {

	private final JavaPlugin plugin;
	private final DataSourceManager dataSourceManager;
	private final DataSourceType dataSourceType;


	public QueryManager(final JavaPlugin plugin, final IDLogger idLogger, final DataSourceManager dataSourceManager) {
		this.plugin = plugin;
		this.dataSourceManager = dataSourceManager;
		dataSourceType = dataSourceManager.getDataSourceType();
	}

	public void prepareDB() throws SQLException {
		switch(dataSourceType) {
			case MYSQL:
				setupMySQL();
				break;
			case SQLITE:
				setupSQLite();
				break;
			default:
				throw new SQLException("Error setting up the data source.");
		}
	}

	public void close() {
		plugin.getLogger().log(Level.INFO, "Disconnecting data source...");
		dataSourceManager.close();
	}

	public void addOffer(Offer offer) throws SQLException {
		String addOfferSQL = "INSERT INTO stock(sellerUUID,textID,amount,price) VALUES(?,?,?,?)";

		try(Connection connection = dataSourceManager.getConnection();
				PreparedStatement ps = connection.prepareStatement(addOfferSQL);
		) {
			ps.setString(1, offer.sellerUUID);
			ps.setString(2, offer.textID);
			ps.setInt(3, offer.amount);
			ps.setDouble(4, offer.price);

			ps.executeUpdate();
		}
	}

	public void deleteOffer(String playerUUID, String textID) throws SQLException {
		String deleteOfferSQL = "DELETE FROM stock WHERE sellerUUID = ? AND textID = ?";

		try(Connection connection = dataSourceManager.getConnection();
			PreparedStatement ps = connection.prepareStatement(deleteOfferSQL);
		) {
			ps.setString(1, playerUUID);
			ps.setString(2, textID);

			ps.executeUpdate();
		}
	}

	public void updateQuantity(String sellerUUID, String textID, int newQuantity) throws SQLException {

		String updateQuantitySQL = "UPDATE stock SET amount = ? WHERE sellerUUID = ? AND textID = ?";

		try(Connection connection = dataSourceManager.getConnection();
			PreparedStatement ps = connection.prepareStatement(updateQuantitySQL);
		) {
			ps.setInt(1, newQuantity);
			ps.setString(2, sellerUUID);
			ps.setString(3, textID);

			ps.executeUpdate();
		}
	}

	public void updateQuantityAndPrice(String sellerUUID, String textID, int newQuantity, double newPrice)
			throws SQLException {

		String updateQuantityAndPriceSQL = "UPDATE stock SET amount = ?, price = ? WHERE sellerUUID = ? AND textID = ?";

		try(Connection connection = dataSourceManager.getConnection();
			PreparedStatement ps = connection.prepareStatement(updateQuantityAndPriceSQL);
		) {
			ps.setInt(1, newQuantity);
			ps.setDouble(2, newPrice);
			ps.setString(3, sellerUUID);
			ps.setString(4, textID);

			ps.executeUpdate();
		}
	}

	/**
	 * Returns a List of offers for some item, with an optional maxPrice
	 * This is used when trying to buy offers or finding offers
	 *
	 * The maxPrice arg is used when buying items, as we only want
	 * 	the offers that are selling BELOW or EQUAL TO maxPrice
	 *
	 * Offers are sorted in ascending order, lowest price first
	 *
	 * @param textID the item's textID
	 * @param maxPrice optional - maximum price willing
	 * @return
	 * @throws SQLException
	 */
	public List<Offer> getItemOffers(String textID, float maxPrice) throws SQLException {
		List<Offer> itemOffers;

		//TODO maybe separate into two different methods
		//TODO getItemOffers and getItemOffersMaxPrice
		//Modify SQL Query based if used for buying offers
		String getItemOffersSQL = "SELECT * FROM stock " +
									"WHERE textID = ? ";

		if(maxPrice > 0) {
			getItemOffersSQL += "AND price <= ? ";
		}

		getItemOffersSQL += "ORDER BY price ASC;";

		// try-with-resource block closes the connection, preparedstatement, and result
		try(Connection connection = dataSourceManager.getConnection();
			PreparedStatement ps = connection.prepareStatement(getItemOffersSQL)
		)  {
			ps.setString(1, textID);

			if(maxPrice > 0) {
				ps.setFloat(2, maxPrice);
			}

			try(ResultSet rs = ps.executeQuery()) {
				itemOffers = Offer.listOffers(rs);
			}
		}

		return itemOffers;
	}

	/**
	 * TODO Is this really necessary?
	 * TODO Yes
	 *
	 * Used in calculating how many pages there are for displaying offers to users
	 * @param textID the textID of the item
	 * @return the number of offers for an item
	 * @throws SQLException
	 */
	public int getAmountOfItemOffers(String textID) throws SQLException {
		int amountOfOffersForItem;

		String amountOfOffersForItemSQL = "SELECT COUNT(*) FROM stock AS total WHERE textID = ?";

		try(Connection connection = dataSourceManager.getConnection();
			PreparedStatement ps = connection.prepareStatement(amountOfOffersForItemSQL)
		)  {
			ps.setString(1, textID);
			try(ResultSet rs = ps.executeQuery()) {
				rs.next();
				amountOfOffersForItem = rs.getInt("total");
			}
		}

		return amountOfOffersForItem;
	}

	public List<Offer> searchBySeller(String sellerUUID) throws SQLException {
		List<Offer> sellerOffers;

		String searchBySellerSQL = "SELECT * FROM stock WHERE sellerUUID = ?";
		try(Connection connection = dataSourceManager.getConnection();
			PreparedStatement ps = connection.prepareStatement(searchBySellerSQL)
		)  {
			ps.setString(1, sellerUUID);
			try(ResultSet rs = ps.executeQuery()) {
				sellerOffers = Offer.listOffers(rs);
			}
		}

		return sellerOffers;
	}

	public List<Offer> getOfferBySellerForItem(String sellerUUID, String textID) throws SQLException {
		List<Offer> offerBySellerForItem;

		String getOfferBySellerForItemSQL = "SELECT * FROM stock WHERE " + "sellerUUID = ? AND textID = ?";
		try(Connection connection = dataSourceManager.getConnection();
			PreparedStatement ps = connection.prepareStatement(getOfferBySellerForItemSQL)
		)  {
			ps.setString(1, sellerUUID);
			ps.setString(2, textID);
			try(ResultSet rs = ps.executeQuery()) {
				offerBySellerForItem = Offer.listOffers(rs);
			}
		}

		return offerBySellerForItem;
	}

	public void logTransaction(Transaction transaction) throws SQLException {
		String logTransactionSQL = "INSERT INTO " + "transactions(sellerUUID,buyerUUID,textID,amount,price)"
									+ "VALUES(?,?,?,?,?)";

		try(Connection connection = dataSourceManager.getConnection();
			PreparedStatement ps = connection.prepareStatement(logTransactionSQL);
		) {
			ps.setString(1, transaction.sellerUUID);
			ps.setString(2, transaction.buyerUUID);
			ps.setString(3, transaction.textID);
			ps.setInt(4, transaction.amount);
			ps.setDouble(5, transaction.price);

			ps.executeUpdate();
		}
	}

	public List<Transaction> getAllTransactions() throws SQLException {
		List<Transaction> allTransactions;

		String getAllTransactionsSQL = "SELECT * FROM transactions " + "ORDER BY id DESC";
		try(Connection connection = dataSourceManager.getConnection();
			PreparedStatement ps = connection.prepareStatement(getAllTransactionsSQL)
		)  {
			try(ResultSet rs = ps.executeQuery()) {
				allTransactions = Transaction.listTransactions(rs);
			}
		}
		return allTransactions;
	}

	public List<Transaction> getTransactionsBySeller(String sellerUUID) throws SQLException {
		List<Transaction> transactionsBySeller;

		String getTransactionBySeller = "SELECT * FROM transactions " + "WHERE sellerUUID = ? " + "ORDER BY id DESC";
		try(Connection connection = dataSourceManager.getConnection();
			PreparedStatement ps = connection.prepareStatement(getTransactionBySeller)
		)  {
			ps.setString(1, sellerUUID);
			try(ResultSet rs = ps.executeQuery()) {
				transactionsBySeller = Transaction.listTransactions(rs);
			}
		}

		return transactionsBySeller;
	}

	public List<Transaction> getTransactionsByBuyer(String buyerUUID) throws SQLException {
		List<Transaction> transactionsByBuyer;

		String getTransactionsByBuyerSQL = "SELECT * FROM transactions WHERE buyerUUID = ? ORDER BY id DESC";

		try(Connection connection = dataSourceManager.getConnection();
			PreparedStatement ps = connection.prepareStatement(getTransactionsByBuyerSQL);
		)  {
			ps.setString(1, buyerUUID);
			try(ResultSet rs = ps.executeQuery()) {
				transactionsByBuyer = Transaction.listTransactions(rs);
			}
		}

		return transactionsByBuyer;
	}

	public List<Transaction> getTransactionsBySellerBuyer(String sellerUUID, String buyerUUID) throws SQLException {
		List<Transaction> transactionsBetweenSellerAndBuyer;

		//Order transactions between buyer and seller by bigger ID (Newest transactions first)
		String getTransactionsBySellerBuyerSQL = "SELECT * FROM transactions WHERE sellerUUID = ? AND buyerUUID = ? "
													+ "ORDER BY id DESC";
		try(Connection connection = dataSourceManager.getConnection();
			PreparedStatement ps = connection.prepareStatement(getTransactionsBySellerBuyerSQL);
		)  {
			ps.setString(1, sellerUUID);
			ps.setString(2, buyerUUID);
			try(ResultSet rs = ps.executeQuery()) {
				transactionsBetweenSellerAndBuyer = Transaction.listTransactions(rs);
			}
		}

		return transactionsBetweenSellerAndBuyer;
	}

	public List<Transaction> getTransactionsBySellerBuyerAndItem(String sellerUUID, String buyerUUID, String textID) throws SQLException {
		List<Transaction> transactionsBetweenSellerAndBuyerForItem;

		String getTransactionsBySellerBuyerForItemSQL = "SELECT * FROM transactions "
				+ "WHERE sellerUUID = ? "
				+ "AND buyerUUID = ? "
				+ "AND textID = ? "
				+ "ORDER BY id DESC";

		try(Connection connection = dataSourceManager.getConnection();
			PreparedStatement ps = connection.prepareStatement(getTransactionsBySellerBuyerForItemSQL);
		)  {
			ps.setString(1, sellerUUID);
			ps.setString(2, buyerUUID);
			ps.setString(3, textID);
			try(ResultSet rs = ps.executeQuery()) {
				transactionsBetweenSellerAndBuyerForItem = Transaction.listTransactions(rs);
			}
		}

		return transactionsBetweenSellerAndBuyerForItem;
	}

	public void doAsyncAddOffer(final Offer o, final VShopCallback<Void> VShopCallback) {
		Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
			@Override
			public void run() {
				try {
					addOffer(o);

					Bukkit.getScheduler().runTask(plugin, new Runnable() {
						@Override
						public void run() {
							VShopCallback.onSuccess(null);
						}
					});

				} catch (SQLException sqlException) {
					VShopCallback.onFailure(sqlException);
				}
			}
		});
	}

	public void doAsyncGetItemOffers(final String itemName, final float maxPrice, final VShopCallback<List<Offer>> VShopCallback) {
		Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
			@Override
			public void run() {
				try {
					final List<Offer> offersByItem = getItemOffers(itemName, maxPrice);

					Bukkit.getScheduler().runTask(plugin, new Runnable() {
						@Override
						public void run() {

							if (offersByItem == null || offersByItem.size() == 0) {
								VShopCallback.onFailure(new OffersNotFoundException());
								return;
							}

							VShopCallback.onSuccess(offersByItem);
						}
					});

				} catch (SQLException sqlException) {
					VShopCallback.onFailure(sqlException);
				}
			}
		});
	}

	public void doAsyncGetOffersBySeller(final String playerUUID, final VShopCallback<List<Offer>> VShopCallback) {
		Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
			@Override
			public void run() {
				try {
					final List<Offer> offersBySellerUUID = searchBySeller(playerUUID);

					Bukkit.getScheduler().runTask(plugin, new Runnable() {
						@Override
						public void run() {

							if(offersBySellerUUID.size() == 0) {
								VShopCallback.onFailure(new OffersNotFoundException());
								return;
							}

							VShopCallback.onSuccess(offersBySellerUUID);
						}
					});
				} catch (SQLException sqlException) {
					VShopCallback.onFailure(sqlException);
				}
			}
		});
	}

	public void doAsyncGetOfferBySellerForItem(final String playerUUID, final String itemID,
											   final VShopCallback<List<Offer>> VShopCallback) {
		Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
			@Override
			public void run() {
				try {
					final List<Offer> offersBySellerForItem = getOfferBySellerForItem(playerUUID, itemID);

					Bukkit.getScheduler().runTask(plugin, new Runnable() {
						@Override
						public void run() {
							if(offersBySellerForItem.size() == 0) {
								VShopCallback.onFailure(new OffersNotFoundException());
								return;
							}

							VShopCallback.onSuccess(offersBySellerForItem);
						}
					});
				} catch (SQLException sqlException) {
					VShopCallback.onFailure(sqlException);
				}
			}
		});
	}

	public void doAsyncUpdateOfferQuantity(final String playerUUID, final String itemName,
								   final int newQuantity, final VShopCallback<Void> VShopCallback) {
		Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
			@Override
			public void run() {
				try {
					updateQuantity(playerUUID, itemName, newQuantity);

					Bukkit.getScheduler().runTask(plugin, new Runnable() {
						@Override
						public void run() {
							VShopCallback.onSuccess(null);
						}
					});

				} catch (SQLException  sqlException) {
					VShopCallback.onFailure(sqlException);
				}
			}
		});
	}

	public void doAsyncUpdateOffer(final String playerUUID, final String itemName,
									final int newQuantity, final double newPrice, final VShopCallback<Void> VShopCallback) {
		Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
			@Override
			public void run() {
				try {
					updateQuantityAndPrice(playerUUID, itemName, newQuantity, newPrice);

					Bukkit.getScheduler().runTask(plugin, new Runnable() {
						@Override
						public void run() {
							VShopCallback.onSuccess( null);
						}
					});

				} catch (SQLException sqlException) {
					VShopCallback.onFailure(sqlException);
				}
			}
		});
	}

	public void doAsyncDeleteOffer(final String playerUUID, final String itemID, final VShopCallback<Void> VShopCallback) {
		Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
			@Override
			public void run() {
				try {
					deleteOffer(playerUUID, itemID);

					Bukkit.getScheduler().runTask(plugin, new Runnable() {
						@Override
						public void run() {
							VShopCallback.onSuccess(null);
						}
					});
				} catch (SQLException  sqlException) {
					VShopCallback.onFailure(sqlException);
				}
			}
		});
	}

	public void doAsyncLogTransaction(final Transaction t, final VShopCallback<Void> VShopCallback) {
		Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
			@Override
			public void run() {
				try {
					logTransaction(t);

					Bukkit.getScheduler().runTask(plugin, new Runnable() {
						@Override
						public void run() {
							VShopCallback.onSuccess(null);
						}
					});
				} catch (SQLException sqlException) {
					VShopCallback.onFailure(sqlException);
				}
			}
		});
	}

	private void setupMySQL() throws SQLException {
		String createStockTable = "CREATE TABLE IF NOT EXISTS stock ("
				+ "sellerUUID VARCHAR(40) NOT NULL,"
				+ "textID VARCHAR(255) NOT NULL,"
				+ "amount INT NOT NULL,"
				+ "price DOUBLE NOT NULL,"
				+ "PRIMARY KEY (sellerUUID, textID)"
				+ ");";

		String createTransactionsTable = "CREATE TABLE IF NOT EXISTS transactions ("
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
				+ ");";

		try (Connection connection = dataSourceManager.getConnection();
			 Statement statement = connection.createStatement();
		) {
			statement.executeUpdate(createStockTable);
			statement.executeUpdate(createTransactionsTable);
		}
	}

	private void setupSQLite() throws SQLException {
		String createStockTable = "CREATE TABLE IF NOT EXISTS stock ("
				+ "sellerUUID VARCHAR(40) NOT NULL,"
				+ "textID VARCHAR(255) NOT NULL,"
				+ "amount INT NOT NULL,"
				+ "price DOUBLE NOT NULL,"
				+ "PRIMARY KEY (sellerUUID, textID)"
				+ ");";

		String createTransactionsTable = "CREATE TABLE IF NOT EXISTS transactions ("
				+ "id INTEGER NOT NULL,"
				+ "sellerUUID VARCHAR(40) NOT NULL,"
				+ "buyerUUID VARCHAR(40) NOT NULL,"
				+ "textID VARCHAR(255) NOT NULL,"
				+ "amount INT NOT NULL,"
				+ "price DOUBLE NOT NULL,"
				+ "PRIMARY KEY (id)"
				+ ");";

		String createIndexSeller = "CREATE INDEX IF NOT EXISTS indexSeller on transactions (sellerUUID);";
		String createIndexBuyer = "CREATE INDEX IF NOT EXISTS indexBuyer on transactions (buyerUUID);";
		String createIndexTextID = "CREATE INDEX IF NOT EXISTS indexTextID on transactions (textID);";

		try (Connection connection = dataSourceManager.getConnection();
			 Statement statement = connection.createStatement()
		) {
			statement.executeUpdate(createStockTable);
			statement.executeUpdate(createTransactionsTable);
			statement.executeUpdate(createIndexSeller);
			statement.executeUpdate(createIndexBuyer);
			statement.executeUpdate(createIndexTextID);
		}
	}
}
