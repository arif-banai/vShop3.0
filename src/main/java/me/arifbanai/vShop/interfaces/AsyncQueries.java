package me.arifbanai.vShop.interfaces;

import me.arifbanai.vShop.exceptions.OffersNotFoundException;
import me.arifbanai.vShop.managers.QueryManager;
import me.arifbanai.vShop.objects.Offer;
import me.arifbanai.vShop.objects.Transaction;

import java.util.List;

/**
 * Methods needed to perform async queries
 *
 * By default, runs queries synchronously
 */
public interface AsyncQueries extends Queries {

    /**
     * Async wrapper for addOffer
     *
     * @param offer the Offer to add
     * @param callback callback function
     * @see Queries#addOffer(Offer o)
     */
    default void doAsyncAddOffer(final Offer offer, final VShopCallback<Void> callback) {
        try {
            addOffer(offer);

            callback.onSuccess(null);
        } catch (Exception e) {
            callback.onFailure(e);
        }
    }

    /**
     * Async wrapper for getAmountOfItemOffers
     *
     * @param textID the item's textID
     * @param callback callback function
     * @see QueryManager#getAmountOfItemOffers(String textID)
     */
    default void doAsyncGetAmountOfItemOffers(final String textID, final VShopCallback<Integer> callback) {
        try {
            final int amountOfItemOffers = getAmountOfItemOffers(textID);

            if(amountOfItemOffers == 0) {
                callback.onFailure(new OffersNotFoundException());
                return;
            }

            callback.onSuccess(amountOfItemOffers);
        } catch (Exception e) {
            callback.onFailure(e);
        }
    }

    /**
     * TODO get rid of magic number '0' for maxPrice, extract into separate method
     * Async wrapper for getItemOffers
     *
     * @param itemName the item's textID
     * @param maxPrice the max price (set to 0 to disable)
     * @param callback callback function
     * @see QueryManager#getItemOffers(String textID, float maxPrice)
     */
    default void doAsyncGetItemOffers(final String itemName, final float maxPrice, final VShopCallback<List<Offer>> callback) {
        try {
            final List<Offer> offersByItem = getItemOffers(itemName, maxPrice);

            if (offersByItem == null || offersByItem.size() == 0) {
                callback.onFailure(new OffersNotFoundException());
                return;
            }

            callback.onSuccess(offersByItem);

        } catch (Exception e) {
            callback.onFailure(e);
        }
    }

    /**
     * Async wrapper for getItemOffersOffset
     *
     * @param itemName the item's textID
     * @param offset   the row to start our results at
     * @param callback callback function
     * @see QueryManager#getItemOffersOffset(String itemName, int offset)
     */
    default void doAsyncGetItemOffersOffset(final String itemName, final int offset, final VShopCallback<List<Offer>> callback) {
        try {
            final List<Offer> itemOffersOffset = getItemOffersOffset(itemName, offset);

            if (itemOffersOffset.size() == 0) {
                callback.onFailure(new OffersNotFoundException());
                return;
            }

            callback.onSuccess(itemOffersOffset);
        } catch (Exception e) {
            callback.onFailure(e);
        }
    }

    default void doAsyncGetOffersBySeller(final String playerUUID, final VShopCallback<List<Offer>> callback) {
        try {
            final List<Offer> offersBySellerUUID = searchBySeller(playerUUID);

            if (offersBySellerUUID.size() == 0) {
                callback.onFailure(new OffersNotFoundException());
                return;
            }

            callback.onSuccess(offersBySellerUUID);
        } catch (Exception e) {
            callback.onFailure(e);
        }
    }

    default void doAsyncGetOfferBySellerForItem(final String playerUUID, final String itemID, final VShopCallback<List<Offer>> callback) {
        try {
            final List<Offer> offersBySellerForItem = getOfferBySellerForItem(playerUUID, itemID);

            if (offersBySellerForItem.size() == 0) {
                callback.onFailure(new OffersNotFoundException());
                return;
            }

            callback.onSuccess(offersBySellerForItem);

        } catch (Exception e) {
            callback.onFailure(e);
        }
    }

    default void doAsyncUpdateOfferQuantity(final String playerUUID, final String itemName, final int newQuantity, final VShopCallback<Void> callback) {
        try {
            updateQuantity(playerUUID, itemName, newQuantity);

            callback.onSuccess(null);

        } catch (Exception e) {
            callback.onFailure(e);
        }
    }

    default void doAsyncUpdateOffer(final String playerUUID, final String itemName, final int newQuantity, final double newPrice, final VShopCallback<Void> callback) {
        try {
            updateQuantityAndPrice(playerUUID, itemName, newQuantity, newPrice);

            callback.onSuccess(null);

        } catch (Exception e) {
            callback.onFailure(e);
        }
    }

    default void doAsyncDeleteOffer(final String playerUUID, final String itemID, final VShopCallback<Void> callback) {
        try {
            deleteOffer(playerUUID, itemID);

            callback.onSuccess(null);
        } catch (Exception e) {
            callback.onFailure(e);
        }
    }

    default void doAsyncLogTransaction(final Transaction t, final VShopCallback<Void> callback) {
        try {
            logTransaction(t);

            callback.onSuccess(null);
        } catch (Exception e) {
            callback.onFailure(e);
        }
    }

}
