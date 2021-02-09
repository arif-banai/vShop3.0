package me.arifbanai.vShop.managers.sql;

import me.arifbanai.easypool.DataSourceManager;
import me.arifbanai.vShop.exceptions.OffersNotFoundException;
import me.arifbanai.vShop.interfaces.VShopCallback;
import me.arifbanai.vShop.objects.Offer;
import me.arifbanai.vShop.objects.Transaction;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;
import java.util.List;

/**
 * Async wrappers for SqlQueries using BukkitScheduler to schedule Async tasks
 */
public class SqlQueryManager extends SqlQueries {

    private final JavaPlugin plugin;

    public SqlQueryManager(JavaPlugin plugin, DataSourceManager dataSourceManager) {
        super(dataSourceManager);
        this.plugin = plugin;
    }

    /**
     * Async wrapper for addOffer
     * @param o the Offer to add
     * @param VShopCallback callback
     * @see #addOffer(Offer o)
     */
    @Override
    public void doAsyncAddOffer(final Offer o, final VShopCallback<Void> VShopCallback) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                addOffer(o);

                Bukkit.getScheduler().runTask(plugin, () -> VShopCallback.onSuccess(null));

            } catch (SQLException sqlException) {
                VShopCallback.onFailure(sqlException);
            }
        });
    }

    /**
     * Async wrapper for getAmountOfItemOffers
     * @param textID
     * @param callback
     * @see #getAmountOfItemOffers(String textID)
     */
    @Override
    public void doAsyncGetAmountOfItemOffers(final String textID, final VShopCallback<Integer> callback) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                final int amountOfOffersForItem = getAmountOfItemOffers(textID);

                Bukkit.getScheduler().runTask(plugin, () -> {
                    if (amountOfOffersForItem == 0) {
                        callback.onFailure(new OffersNotFoundException());
                        return;
                    }

                    callback.onSuccess(amountOfOffersForItem);
                });
            } catch (SQLException sqlException) {
                callback.onFailure(sqlException);
            }
        });
    }

    /**
     * TODO get rid of magic number '0' for maxPrice, extract into separate method
     * Async wrapper for getItemOffers
     * @param itemName the item's textID
     * @param maxPrice the max price (set to 0 to disable)
     * @param callback
     * @see #getItemOffers(String textID, float maxPrice)
     */
    @Override
    public void doAsyncGetItemOffers(final String itemName, final float maxPrice, final VShopCallback<List<Offer>> callback) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                final List<Offer> offersByItem = getItemOffers(itemName, maxPrice);

                Bukkit.getScheduler().runTask(plugin, () -> {

                    if (offersByItem == null || offersByItem.size() == 0) {
                        //System.out.println("NO ITEMS FOUND YEEEEEEET");
                        callback.onFailure(new OffersNotFoundException());
                        return;
                    }

                    callback.onSuccess(offersByItem);
                });

            } catch (SQLException sqlException) {
                callback.onFailure(sqlException);
            }
        });
    }

    /**
     * Async wrapper for getItemOffersOffset
     * @param itemName the item's textID
     * @param offset the row to start our results at
     * @param callback
     * @see #getItemOffersOffset(String itemName, int offset)
     */
    @Override
    public void doAsyncGetItemOffersOffset(final String itemName, final int offset, final VShopCallback<List<Offer>> callback) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                final List<Offer> itemOffersOffset = getItemOffersOffset(itemName, offset);

                Bukkit.getScheduler().runTask(plugin, () -> {
                    if (itemOffersOffset.size() == 0) {
                        callback.onFailure(new OffersNotFoundException());
                        return;
                    }

                    callback.onSuccess(itemOffersOffset);
                });
            } catch (SQLException sqlException) {
                callback.onFailure(sqlException);
            }
        });
    }

    @Override
    public void doAsyncGetOffersBySeller(final String playerUUID, final VShopCallback<List<Offer>> callback) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                final List<Offer> offersBySellerUUID = searchBySeller(playerUUID);

                Bukkit.getScheduler().runTask(plugin, () -> {

                    if (offersBySellerUUID.size() == 0) {
                        callback.onFailure(new OffersNotFoundException());
                        return;
                    }

                    callback.onSuccess(offersBySellerUUID);
                });
            } catch (SQLException sqlException) {
                callback.onFailure(sqlException);
            }
        });
    }

    @Override
    public void doAsyncGetOfferBySellerForItem(final String playerUUID, final String itemID,
                                               final VShopCallback<List<Offer>> callback) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                final List<Offer> offersBySellerForItem = getOfferBySellerForItem(playerUUID, itemID);

                Bukkit.getScheduler().runTask(plugin, () -> {
                    if (offersBySellerForItem.size() == 0) {
                        callback.onFailure(new OffersNotFoundException());
                        return;
                    }

                    callback.onSuccess(offersBySellerForItem);
                });
            } catch (SQLException sqlException) {
                callback.onFailure(sqlException);
            }
        });
    }

    @Override
    public void doAsyncUpdateOfferQuantity(final String playerUUID, final String itemName,
                                           final int newQuantity, final VShopCallback<Void> callback) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                updateQuantity(playerUUID, itemName, newQuantity);

                Bukkit.getScheduler().runTask(plugin, () -> callback.onSuccess(null));

            } catch (SQLException  sqlException) {
                callback.onFailure(sqlException);
            }
        });
    }

    @Override
    public void doAsyncUpdateOffer(final String playerUUID, final String itemName,
                                   final int newQuantity, final double newPrice, final VShopCallback<Void> callback) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                updateQuantityAndPrice(playerUUID, itemName, newQuantity, newPrice);

                Bukkit.getScheduler().runTask(plugin, () -> callback.onSuccess(null));

            } catch (SQLException sqlException) {
                callback.onFailure(sqlException);
            }
        });
    }

    @Override
    public void doAsyncDeleteOffer(final String playerUUID, final String itemID, final VShopCallback<Void> callback) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                deleteOffer(playerUUID, itemID);

                Bukkit.getScheduler().runTask(plugin, () -> callback.onSuccess(null));
            } catch (SQLException  sqlException) {
                callback.onFailure(sqlException);
            }
        });
    }

    @Override
    public void doAsyncLogTransaction(final Transaction t, final VShopCallback<Void> callback) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                logTransaction(t);

                Bukkit.getScheduler().runTask(plugin, () -> callback.onSuccess(null));
            } catch (SQLException sqlException) {
                callback.onFailure(sqlException);
            }
        });
    }
}
