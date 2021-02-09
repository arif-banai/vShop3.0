package me.arifbanai.vShop.interfaces;

import me.arifbanai.vShop.managers.QueryManager;
import me.arifbanai.vShop.objects.Offer;
import me.arifbanai.vShop.objects.Transaction;

import java.sql.ResultSet;
import java.util.List;

public interface Queries {

    /**
     * Prepare the database connection/pool
     * @throws Exception some error setting up the DB/pool
     */
    void prepareDB() throws Exception;

    /**
     * Add a new offer
     * @param offer the offer to add
     * @throws Exception failed to add offer, possibly an SQLException or IOException
     * @see Offer
     */
    void addOffer(Offer offer) throws Exception;

    /**
     * Delete some existing offer
     * @param playerUUID the UUID of the player offering some item
     * @param textID the textID of the item being offered
     * @throws Exception failed to delete offer, possibly an SQLException or IOException
     */
    void deleteOffer(String playerUUID, String textID) throws Exception;

    /**
     * Update the amount of some existing offer
     * @param playerUUID the UUID of the player offering some item
     * @param textID the String of the Material
     * @param newQuantity the new quantity to update the offer with
     * @throws Exception possibly an SQLException or IOException
     */
    void updateQuantity(String playerUUID, String textID, int newQuantity) throws Exception;

    /**
     * Updates the quantity and price of exactly ONE offer
     * @param sellerUUID 	- UUID of seller
     * @param textID		- textID of item
     * @param newQuantity	- new amount of item
     * @param newPrice		- the new price of the item
     * @throws Exception possibly an SQLException or IOException
     */
    void updateQuantityAndPrice(String sellerUUID, String textID, int newQuantity, double newPrice)
            throws Exception;

    /**
     * Allows us to paginate the offers table.
     *
     * Used in calculating how many pages there are for displaying offers to users.
     * Allows us to query for only the dataset required using.
     * @param textID the textID of the item
     * @return The number of offers for an item - returns 0 if no offers found
     * @throws Exception possibly an SQLException or IOException
     */
    int getAmountOfItemOffers(String textID) throws Exception;

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
     * @throws Exception possibly an SQLException or IOException
     * @see Offer#listOffers(ResultSet rs)
     */
    List<Offer> getItemOffers(String textID, float maxPrice) throws Exception;

    /**
     * Paginated version of getItemOffers
     * @param textID textID of an item
     * @param offset number of rows to skip
     * @return Up to NINE offers for an item, starting at some offset
     * @throws Exception possibly an SQLException or IOException
     * @see QueryManager#getItemOffers(String textID, float maxPrice)
     */
    List<Offer> getItemOffersOffset(String textID, int offset) throws Exception;

    /**
     * @param sellerUUID the seller's UUID
     * @return All offers by a singular seller
     * @throws Exception possibly an SQLException or IOException
     */
    List<Offer> searchBySeller(String sellerUUID) throws Exception;

    /**
     * @param sellerUUID the seller's UUID
     * @param textID	 the textID of the item
     * @return All offers by a singular seller for a specific item
     * @throws Exception possibly an SQLException or IOException
     */
    List<Offer> getOfferBySellerForItem(String sellerUUID, String textID) throws Exception;

    // Transactions

    /**
     * Add a new transaction
     * @param transaction the transaction to record
     * @throws Exception possibly an SQLException or IOException
     * @see Transaction
     */
    void logTransaction(Transaction transaction) throws Exception;

    /**
     * TODO Optimize, use pagination
     * @return A list of all transactions
     * @throws Exception possibly an SQLException or IOException
     */
    List<Transaction> getAllTransactions() throws Exception;

    /**
     * TODO pagination
     * @param sellerUUID the UUID of the seller in the transactions
     * @return All sales by some seller
     * @throws Exception possibly an SQLException or IOException
     */
    List<Transaction> getTransactionsBySeller(String sellerUUID) throws Exception;

    /**
     * @param buyerUUID the UUID of the buyer in the transactions
     * @return All transactions by some customer
     * @throws Exception possibly an SQLException or IOException
     */
    List<Transaction> getTransactionsByBuyer(String buyerUUID) throws Exception;

    /**
     * @param sellerUUID the UUID of the seller
     * @param buyerUUID the UUID of the buyer
     * @return All transactions between some seller and some buyer
     * @throws Exception possibly an SQLException or IOException
     */
    List<Transaction> getTransactionsBySellerBuyer(String sellerUUID, String buyerUUID) throws Exception;

    /**
     * @param sellerUUID the sellers UUID
     * @param buyerUUID the buyers UUID
     * @param textID the item's textID
     * @return All transactions by some seller and some buyer for some item
     * @throws Exception possibly an SQLException or IOException
     */
    List<Transaction> getTransactionsBySellerBuyerAndItem(String sellerUUID, String buyerUUID, String textID) throws Exception;

}
