package me.arifbanai.vShop.managers.database;

import me.arifbanai.idLogger.IDLogger;
import me.arifbanai.vShop.exceptions.OffersNotFoundException;
import me.arifbanai.vShop.interfaces.VShopCallback;
import me.arifbanai.vShop.objects.Offer;
import me.arifbanai.vShop.objects.Transaction;
import me.huskehhh.bukkitSQL.Database;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public abstract class DatabaseManager {

	protected Database db;
	protected JavaPlugin plugin;
	protected IDLogger idLogger;

	public DatabaseManager(final JavaPlugin plugin, final IDLogger instance) {
		this.plugin = plugin;
		idLogger = instance;
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

	public void updateQuantityAndPrice(String sellerUUID, String textID, int newQuantity, double newPrice)
			throws SQLException, ClassNotFoundException {

		PreparedStatement safeStatement;
		safeStatement = db.getConnection()
				.prepareStatement("UPDATE stock SET " + "amount = ?, price = ? WHERE " + "sellerUUID = ? AND textID = ?");

		safeStatement.setInt(1, newQuantity);
		safeStatement.setDouble(2, newPrice);
		safeStatement.setString(3, sellerUUID);
		safeStatement.setString(4, textID);

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

	public List<Offer> getOfferBySellerForItem(String sellerUUID, String textID) throws SQLException, ClassNotFoundException {

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

				} catch (SQLException | ClassNotFoundException throwables) {
					VShopCallback.onFailure(throwables);
				}
			}
		});
	}

	public void doAsyncGetItemOffers(final String itemName, final VShopCallback<List<Offer>> VShopCallback) {
		Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
			@Override
			public void run() {
				try {
					final List<Offer> offersByItem = getItemOffers(itemName);

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

				} catch (SQLException | ClassNotFoundException throwables) {
					VShopCallback.onFailure(throwables);
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
				} catch (SQLException | ClassNotFoundException throwables) {
					VShopCallback.onFailure(throwables);
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
				} catch (SQLException | ClassNotFoundException throwables) {
					VShopCallback.onFailure(throwables);
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

				} catch (SQLException | ClassNotFoundException throwables) {
					VShopCallback.onFailure(throwables);
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
							VShopCallback.onSuccess(null);
						}
					});

				} catch (SQLException | ClassNotFoundException throwables) {
					VShopCallback.onFailure(throwables);
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
				} catch (SQLException | ClassNotFoundException throwables) {
					VShopCallback.onFailure(throwables);
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
				} catch (SQLException | ClassNotFoundException throwables) {
					VShopCallback.onFailure(throwables);
				}
			}
		});
	}
}
