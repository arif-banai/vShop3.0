package me.arifbanai.vShop.commands;

import me.arifbanai.vShop.Main;
import me.arifbanai.vShop.exceptions.OffersNotFoundException;
import me.arifbanai.vShop.interfaces.Callback;
import me.arifbanai.vShop.managers.database.DatabaseManager;
import me.arifbanai.vShop.objects.Offer;
import me.arifbanai.vShop.objects.Transaction;
import me.arifbanai.vShop.utils.ChatUtils;
import me.arifbanai.vShop.utils.NumberUtils;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;


public class Buy implements CommandExecutor {

	private Main plugin;
	private DatabaseManager db;

	public Buy(final Main instance) {
		plugin = instance;
		db = plugin.getSQL();
	}

	@Override
	/*
	 * This command is used to buy a certain amount of an item, with a max
	 * price to pay PER ITEM The command format is /buy <amount> <item> <maxPrice>
	 */
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().equalsIgnoreCase("buy")) {
			// Check if the sender is NOT an instance of Player
			if (!(sender instanceof Player)) {
				ChatUtils.denyConsole(sender);
				return false;
			}

			Player player = (Player) sender;

			// Check if the player DOES NOT have permission to use the command
			if (!player.hasPermission(cmd.getPermission())) {
				ChatUtils.noPermission(player);
				return false;
			}

			// Check if the command is NOT formatted properly
			if (args.length < 2) {
				ChatUtils.sendError(player, "Proper usage is /buy <amount> <item> [maxPrice]");
				return false;
			}

			int desiredAmount = NumberUtils.getInteger(args[0]);

			// Check if the amount is invalid
			if (desiredAmount <= 0) {
				ChatUtils.sendError(player, "Invalid amount");
				return false;
			}

			float maxPrice;

			if (NumberUtils.getFloat(args[args.length - 1]) > 0) {
				maxPrice = NumberUtils.getFloat(args[args.length - 1]);
			} else {
				ChatUtils.sendError(player, "Please set a max price you are willing to pay at the end of your command.");
				ChatUtils.sendError(player, "Proper usage is /buy <amount> <item> <maxPrice>");
				return false;
			}

			// Check if the price is invalid
			if (maxPrice <= 0) {
				ChatUtils.sendError(player, "The max price cannot be 0.");
				return false;
			}
			
			String itemLookup = args[1];
			
			for(int i = 2; i < args.length - 1; i++) {
				itemLookup += " " + args[i];
			}
			
			Material item = Material.matchMaterial(itemLookup);
			
			if (item == null || item.equals(Material.AIR)) {
				ChatUtils.wrongItem(player, args[1]);
				return false;
			}

			final String itemID = item.toString();

			db.doAsyncGetItemOffers(itemID, new Callback<List<Offer>>() {
				@Override
				public void onSuccess(List<Offer> result) {
					List<Offer> offers = result;

					// Used to keep track of how much the buyer has bought in total
					int bought = 0;
					// Used to keep track of how much the buyer has spent in total
					double spent = 0;

					for (Offer o : offers) {
						// Skip if the price is higher than maxPrice
						if (o.price > maxPrice) {
							continue;
						}

						// Skip if the seller is the player attempting to purchase
						if (o.sellerUUID.equals(player.getUniqueId().toString())) {
							continue;
						}

						boolean canBuyWholeOffer;
						int amountLeft;

						int canBuy;
						double cost;

						// If the amount that can be purchased is greater than or equal to the current offer's amount
						if (desiredAmount - bought >= o.amount) {
							canBuyWholeOffer = true;
							canBuy = o.amount;
							amountLeft = 0;
						} else {
							canBuyWholeOffer = false;

							canBuy = desiredAmount - bought;

							// amountLeft will always be greater than or equal to 1
							amountLeft = o.amount - canBuy;
						}

						cost = o.price * canBuy;

						// Check if player does not have enough money
						if (Main.economy.getBalance(player) < cost) {
							ChatUtils.sendError(player, "You do not have enough money!");
							return;
						}

						if(canBuyWholeOffer) {
							//If player can buy entire offer, delete the offer.
							db.doAsyncDeleteOffer(o.sellerUUID, item.toString(), new Callback<Void>() {
								@Override
								public void onSuccess(Void result) {

								}

								@Override
								public void onFailure(Throwable cause) {
									handleSqlError(cause, player);
								}
							});
						} else {
							//If the player can't buy the entire offer, update the offer to set how much is left.
							//Variable <amountLeft> will always be initialized if you reach this branch
							db.doAsyncUpdateOfferQuantity(o.sellerUUID, o.textID, amountLeft, new Callback<Void>() {
								@Override
								public void onSuccess(Void result) {

								}

								@Override
								public void onFailure(Throwable cause) {
									handleSqlError(cause, player);
								}
							});
						}

						// Increment bought and spent
						bought += canBuy;
						spent += cost;

						// Get the seller Player. However, the player might be offline
						OfflinePlayer seller = plugin.getServer().getOfflinePlayer(UUID.fromString(o.sellerUUID));

						// Send money from the buyer to the seller
						Main.economy.withdrawPlayer(player, cost);
						Main.economy.depositPlayer(seller, cost);

						// Send a message notifying the seller of a sale if they are online
						if (seller.isOnline()) {
							Player onlineSeller = (Player) seller;
							notifySeller(onlineSeller, player.getName(), canBuy, item, cost);
						}

						// Record the transaction
						Transaction t = new Transaction(o.sellerUUID, player.getUniqueId().toString(), o.textID, canBuy, cost);

						db.doAsyncLogTransaction(t, new Callback<Void>() {
							@Override
							public void onSuccess(Void result) {

							}

							@Override
							public void onFailure(Throwable cause) {
								handleSqlError(cause, player);
							}
						});

						// Stop if the player has bought enough items
						if (bought >= desiredAmount) {
							break;
						}
					}

					// Create a new ItemStack with the item purchased, and the amount bought
					ItemStack purchasedItem = new ItemStack(item, bought);

					// Add the items to the players inventory, and prepare any items that could not be added
					HashMap<Integer, ItemStack> leftOver = player.getInventory().addItem(purchasedItem);

					// Drop all items not added to Inventory if there are any
					for(ItemStack leftOverItem : leftOver.values()) {
						player.getWorld().dropItem(player.getLocation(), leftOverItem);
					}

					// Tell the buyer how much he has bought with a certain amount of money spent
					ChatUtils.sendSuccess(player, "Managed to buy " + ChatUtils.formatAmount(bought) + " "
							+ ChatUtils.formatItem(item) + " for " + ChatUtils.formatPrice(spent));
				}

				@Override
				public void onFailure(Throwable cause) {
					if(cause instanceof OffersNotFoundException) {
						ChatUtils.sendError(player, "There is no " + ChatUtils.formatItem(item) + " for sale.");
						return;
					}

					handleSqlError(cause, player);
				}
			});

			// Command has completed successfully
			return true;
		}

		return false;
	}

	private void notifySeller(Player seller, String buyerName, int amount, Material item, double cost) {
		ChatUtils.sendSuccess(seller, ChatUtils.formatSeller(buyerName) + " just bought "
						+ ChatUtils.formatAmount(amount) + " " + ChatUtils.formatItem(item)
						+ " for " + ChatUtils.formatPrice(cost));
	}

	private void handleSqlError(Throwable cause, Player player) {
		cause.printStackTrace();
		ChatUtils.sendError(player, "An SQLException occured. Please alert admins. vShop shutting down.");
		plugin.disablePlugin();
	}
}
