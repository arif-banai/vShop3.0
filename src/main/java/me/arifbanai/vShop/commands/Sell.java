package me.arifbanai.vShop.commands;

import me.arifbanai.vShop.VShop;
import me.arifbanai.vShop.exceptions.OffersNotFoundException;
import me.arifbanai.vShop.interfaces.VShopCallback;
import me.arifbanai.vShop.managers.QueryManager;
import me.arifbanai.vShop.objects.Offer;
import me.arifbanai.vShop.utils.ChatUtils;
import me.arifbanai.vShop.utils.CommandUtils;
import me.arifbanai.vShop.utils.InventoryUtils;
import me.arifbanai.vShop.utils.NumberUtils;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;

import java.util.List;

//TODO Test async changes.

public class Sell implements CommandExecutor {

    private final VShop plugin;
    private final QueryManager queryManager;

    public Sell(final VShop instance, final QueryManager queryManager) {
        plugin = instance;
        this.queryManager = queryManager;
    }

    @Override
    /*
     * This command will take an item a player has and attempt to place an amount of
     * them in the database, with a price PER EACH item The command format is
     * /sell <amount> <itemName/hand> <pricePerEach>
     */
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!cmd.getName().equalsIgnoreCase("sell")) {
            return false;
        }

        Player player;

        if(CommandUtils.isPlayerWithPerms(sender, cmd)) {
            player = (Player) sender;
        } else {
            return false;
        }

        // Check if the command is NOT formatted properly
        if (args.length < 3) {
            ChatUtils.sendError(player, "The proper usage is /sell <amount> <item> <price>");
            return false;
        }

        int amountListed = NumberUtils.getInteger(args[0]);

        // Check if the amount is invalid
        if (amountListed <= 0) {
            ChatUtils.sendError(player, "Invalid amount");
            return false;
        }

        final float price;

        // If args.length is greater than 3,
        // we should know this ahead of time for processing the item name
        // $$$ args.length > 3 when item name includes multiple words
        boolean bigItemName = false;

        if (args.length > 3) {
            price = NumberUtils.getFloat(args[args.length - 1]);
            bigItemName = true;
        } else {
            price = NumberUtils.getFloat(args[2]);
        }

        // Check if the price is invalid
        if (price <= 0) {
            ChatUtils.sendError(player, "Invalid amount");
            return false;
        }

        PlayerInventory inv = player.getInventory();

        Material item;
        String itemLookupStr = args[1];

        // If itemName is multiple args, combine them into one string
        // Avoid concatenating strings, use StringBuilder
        if (bigItemName) {
            StringBuilder builder = new StringBuilder(itemLookupStr);

            for (int i = 2; i < args.length - 1; i++) {
                builder.append(" ").append(args[i]);
            }
            itemLookupStr = builder.toString();
        }

        if(itemLookupStr.equalsIgnoreCase("hand")) {
            item = inv.getItemInMainHand().getType();
        } else {
            item = Material.matchMaterial(itemLookupStr);
        }

        // Check if the item is invalid or Material.AIR (non-null empty item)
        if (item == null || item.equals(Material.AIR)) {
            ChatUtils.wrongItem(player, itemLookupStr);
            return false;
        }

        // Check if player's inventory DOES NOT have at least <amount> of <item>
        if (!inv.contains(item, amountListed)) {
            ChatUtils.sendError(sender, "You do not have " + ChatUtils.formatAmount(NumberUtils.getInteger(args[0]))
                    + " " + ChatUtils.formatItem(item));
            return true;
        }

        // Check if offer already exists in database
        // If so, update existing offer with new amount and price
        // Otherwise, make new offer and insert into db
        queryManager.doAsyncGetOfferBySellerForItem(player.getUniqueId().toString(), item.toString(), new VShopCallback<List<Offer>>() {
            @Override
            public void onSuccess(List<Offer> result) {
                int existingAmount = amountListed;

                // If player has an existing offer for the same item, we need to "merge" the two offers.
                // This is done by adding the amount the player wants to sell now,
                //     with the existing amount they were already selling.
                // The existing offer is then deleted

                for (Offer o : result)
                    existingAmount += o.amount;

                final int finalAmount = existingAmount;

                queryManager.doAsyncUpdateOffer(player.getUniqueId().toString(), item.toString(),
                                    finalAmount, price, new VShopCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        broadcastAndRemoveFromInv(player, item, finalAmount, price);
                    }

                    @Override
                    public void onFailure(Exception cause) {
                        handleFatalError(cause, player);
                    }
                });
            }

            @Override
            public void onFailure(Exception cause) {
                if(cause instanceof OffersNotFoundException) {
                    final Offer offerToList = new Offer(player.getUniqueId().toString(), item.toString(), amountListed, price);

                    queryManager.doAsyncAddOffer(offerToList, new VShopCallback<Void>() {
                        @Override
                        public void onSuccess(Void result) {
                            broadcastAndRemoveFromInv(player, item, amountListed, price);
                        }

                        @Override
                        public void onFailure(Exception cause) {
                            handleFatalError(cause, player);
                        }
                    });
                    return;
                }

                handleFatalError(cause, player);
            }
        });

        // Command has completed successfully
        return true;
    }

    /**
     * Broadcast the offer (if enabled in config) and remove the items from the seller
     * @param p the player selling
     * @param item the item being offered
     * @param amount the amount of the item
     * @param price the price PER ITEM
     */
    private void broadcastAndRemoveFromInv(Player p, Material item, int amount, double price) {
        if (plugin.getConfig().getBoolean("broadcast-offers", true)) {
            ChatUtils.broadcastOffer(p.getName(), amount, item.toString(), price);
        }
        // Remove the items to be sold from the seller's inventory
        InventoryUtils.removeInventoryItems(p.getInventory(), item, amount);
    }

    private void handleFatalError(Exception cause, Player player) {
        ChatUtils.sendQueryError(player);
        plugin.handleUnexpectedException(cause);
    }
}
