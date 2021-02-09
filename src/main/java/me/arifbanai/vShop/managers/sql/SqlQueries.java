package me.arifbanai.vShop.managers.sql;

import me.arifbanai.easypool.DataSourceManager;
import me.arifbanai.vShop.managers.QueryManager;
import me.arifbanai.vShop.objects.Offer;
import me.arifbanai.vShop.objects.Transaction;

import java.sql.*;
import java.util.List;

@SuppressWarnings("UnnecessarySemicolon")
public class SqlQueries extends QueryManager {

    public SqlQueries(DataSourceManager dataSourceManager) {
        super(dataSourceManager);
    }

    @Override
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

    @Override
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

    @Override
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

    @Override
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

    @Override
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

    @Override
    public int getAmountOfItemOffers(String textID) throws SQLException {
        int amountOfOffersForItem = 0;

        String amountOfOffersForItemSQL = "SELECT COUNT(*) FROM stock AS total WHERE textID = ?";

        try(Connection connection = dataSourceManager.getConnection();
            PreparedStatement ps = connection.prepareStatement(amountOfOffersForItemSQL)
        )  {
            ps.setString(1, textID);
            try(ResultSet rs = ps.executeQuery()) {
                if(rs.next()) {
                    amountOfOffersForItem = rs.getInt(1);
                }
            }
        }

        return amountOfOffersForItem;
    }

    /**
     * Returns a List of offers for some item, with an optional maxPrice
     * This is used when trying to buy offers or finding offers
     *
     * The maxPrice arg is used when buying items, as we only want
     * 	the offers that are selling BELOW or EQUAL TO maxPrice
     *
     * Offers are sorted in ascending order, lowest price first
     * Set lowest price to 0 or lower to disable max ordering
     *
     * @param textID the item's textID
     * @param maxPrice optional - maximum price
     * @return All offers for some item, optionally, below the maxPrice
     * @throws SQLException
     * @see Offer#listOffers(ResultSet rs)
     */
    @Override
    public List<Offer> getItemOffers(String textID, float maxPrice) throws SQLException {
        List<Offer> itemOffers;

        //TODO maybe separate into two different methods
        //TODO getItemOffers and getItemOffersMaxPrice
        //Modify SQL Query if used for buying offers
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
     * Paginated version of getItemOffers
     * TODO Optimize SQL
     * @param textID textID of an item
     * @param offset number of rows to skip
     * @return Up to NINE offers for an item, starting at some offset
     * @throws SQLException
     * @see QueryManager#getItemOffers(String textID, float maxPrice)
     */
    @Override
    public List<Offer> getItemOffersOffset(String textID, int offset) throws SQLException {
        List<Offer> itemOffers;

        String getItemOffersSQL = "SELECT * FROM stock " +
                "WHERE textID = ? ";

        getItemOffersSQL += "ORDER BY price ASC LIMIT 9 OFFSET ?;";

        // try-with-resource block closes the connection, preparedstatement, and result
        try(Connection connection = dataSourceManager.getConnection();
            PreparedStatement ps = connection.prepareStatement(getItemOffersSQL)
        )  {
            ps.setString(1, textID);
            ps.setInt(2, offset);

            try(ResultSet rs = ps.executeQuery()) {
                itemOffers = Offer.listOffers(rs);
            }
        }

        return itemOffers;
    }

    /**
     * TODO pagination
     * @param sellerUUID the seller's UUID
     * @return All offers by a singular seller
     * @throws SQLException
     */
    @Override
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

    /**
     * Returns a list of all offers by a seller for an item
     * There should only ever be one offer returned in this list, or none if not existing
     * TODO return a single offer?
     * @param sellerUUID the seller's UUID
     * @param textID	 the textID of the item
     * @return A singular Offer by singular seller for a specific item
     * @throws SQLException
     */
    @Override
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

    // Transactions

    /**
     * Add a new transaction
     * @param transaction
     * @throws SQLException
     * @see Transaction
     */
    @Override
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

    /**
     * TODO Optimize, use pagination
     * @return A list of all transactions
     * @throws SQLException
     */
    @Override
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

    /**
     * TODO pagination
     * @param sellerUUID
     * @return All sales by some seller
     * @throws SQLException
     */
    @Override
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

    /**
     * @param buyerUUID
     * @return All transactions by some customer
     * @throws SQLException
     */
    @Override
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

    /**
     *
     * @param sellerUUID the UUID of the seller
     * @param buyerUUID the UUID of the buyer
     * @return All transactions between some seller and some buyer
     * @throws SQLException
     */
    @Override
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

    /**
     *
     * @param sellerUUID the sellers UUID
     * @param buyerUUID the buyers UUID
     * @param textID the item's textID
     * @return All transactions by some seller and some buyer for some item
     * @throws SQLException
     */
    @Override
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

    /**
     * Setup tables using MySQL syntax
     * @throws SQLException
     */
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

    /**
     * Setup tables using SQLite syntax
     * @throws SQLException
     */
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
