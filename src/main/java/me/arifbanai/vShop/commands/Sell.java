package me.arifbanai.vShop.commands;

import me.arifbanai.vShop.Main;
import me.arifbanai.vShop.exceptions.OffersNotFoundException;
import me.arifbanai.vShop.interfaces.VShopCallback;
import me.arifbanai.vShop.managers.database.DatabaseManager;
import me.arifbanai.vShop.objects.Offer;
import me.arifbanai.vShop.utils.ChatUtils;
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

    private Main plugin;
    private DatabaseManager db;

    public Sell(final Main instance) {
        plugin = instance;
        db = plugin.getSQL();
    }

    @Override
    /*
     * This command will take an item a player has and attempt to place an amount of
     * them in the database, with a price PER EACH item The command format is /sell
     * <amount> <itemName/hand> <pricePerEach>
     */
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!cmd.getName().equalsIgnoreCase("sell")) {
            return false;
        }
        // Check if the sender is NOT and instance of Player
        if (!(sender instanceof Player)) {
            ChatUtils.denyConsole(sender);
            return false;
        }

        Player player = (Player) sender;

        // Check if the player has permission to use the command
        if (!player.hasPermission(cmd.getPermission())) {
            ChatUtils.noPermission(player);
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

        // If args.length is greater than 3, we should know this ahead of time
        // 	for processing the item name
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

        // Check if item name is longer than one word
        if (bigItemName) {

            String itemLookup;

            itemLookup = args[1];
            for (int i = 2; i < args.length - 1; i++) {
                itemLookup += " " + args[i];
            }

            item = Material.matchMaterial(itemLookup);

            // If the above method can't find the item, it will return null
            if (item == null) {
                ChatUtils.wrongItem(player, itemLookup);
                return false;
            }

        } else {
            if (args[1].equalsIgnoreCase("hand")) {
                // Get item from main hand
                item = inv.getItemInMainHand().getType();

            } else {
                item = Material.matchMaterial(args[1]);
            }
        }

        // Check if the item is Material.AIR (aka nothing)
        if (item == null || item.equals(Material.AIR)) {
            ChatUtils.wrongItem(player, "AIR");
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
        db.doAsyncGetOfferBySellerForItem(player.getUniqueId().toString(), item.toString(), new VShopCallback<List<Offer>>() {
            @Override
            public void onSuccess(List<Offer> result) {
                List<Offer> theOffers = result;

                int existingAmount = amountListed;

                //If player has an existing offer for the same item, we need to "merge" the two offers.
                //This is done by adding the amount the player wants to sell now with the existing amount they were
                //already selling. The existing offer is then deleted,

                for (Offer o : theOffers)
                    existingAmount += o.amount;

                final int finalAmount = existingAmount;

                db.doAsyncUpdateOffer(player.getUniqueId().toString(), item.toString(),
                                    finalAmount, price, new VShopCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        broadcastAndRemoveFromInv(player, item, finalAmount, price);
                    }

                    @Override
                    public void onFailure(Throwable cause) {
                        handleSqlError(cause, player);
                    }
                });
            }

            @Override
            public void onFailure(Throwable cause) {
                if(cause instanceof OffersNotFoundException) {
                    final Offer offerToList = new Offer(player.getUniqueId().toString(), item.toString(), amountListed, price);

                    db.doAsyncAddOffer(offerToList, new VShopCallback<Void>() {
                        @Override
                        public void onSuccess(Void result) {
                            broadcastAndRemoveFromInv(player, item, amountListed, price);
                        }

                        @Override
                        public void onFailure(Throwable cause) {
                            handleSqlError(cause, player);
                        }
                    });
                    return;
                }

                handleSqlError(cause, player);
            }
        });

        // Command has completed successfully
        return true;
    }

    private void broadcastAndRemoveFromInv(Player p, Material item, int amount, double price) {
        if (plugin.getConfigManager().broadcastOffers()) {
            ChatUtils.broadcastOffer(p.getName(), amount, item.toString(), price);
        }
        // Remove the items to be sold from the seller's inventory
        InventoryUtils.removeInventoryItems(p.getInventory(), item, amount);
    }

    private void handleSqlError(Throwable cause, Player player) {
        cause.printStackTrace();
        ChatUtils.sendError(player, "An SQLException occured. Please alert admins. vShop shutting down.");
        plugin.disablePlugin();
    }
}
