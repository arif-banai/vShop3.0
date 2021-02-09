package me.arifbanai.vShop.commands;

import me.arifbanai.vShop.VShop;
import me.arifbanai.vShop.exceptions.OffersNotFoundException;
import me.arifbanai.vShop.interfaces.VShopCallback;
import me.arifbanai.vShop.managers.QueryManager;
import me.arifbanai.vShop.objects.Offer;
import me.arifbanai.vShop.objects.Transaction;
import me.arifbanai.vShop.utils.ChatUtils;
import me.arifbanai.vShop.utils.CommandUtils;
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

	private final VShop plugin;
	private final QueryManager queryManager;

	public Buy(final VShop instance, final QueryManager queryManager) {
		plugin = instance;
		this.queryManager = queryManager;
	}

	@Override
	/*
	 * This command is used to buy a certain amount of an item, with a max
	 * price to pay PER ITEM The command format is /buy <amount> <item> <maxPrice>
	 */
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().equalsIgnoreCase("buy")) {
			Player player;

			if(CommandUtils.isPlayerWithPerms(sender, cmd)) {
				player = (Player) sender;
			} else {
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
			
			StringBuilder builder = new StringBuilder(args[1]);
			
			for(int i = 2; i < args.length - 1; i++) {
				builder.append(" ").append(args[i]);
			}
			
			Material item = Material.matchMaterial(builder.toString());
			
			if (item == null || item.equals(Material.AIR)) {
				ChatUtils.wrongItem(player, args[1]);
				return false;
			}

			final String itemID = item.toString();

			queryManager.doAsyncGetItemOffers(itemID, maxPrice, new VShopCallback<List<Offer>>() {
				@Override
				public void onSuccess(List<Offer> result) {

					// Used to keep track of how much the buyer has bought in total
					int bought = 0;
					// Used to keep track of how much the buyer has spent in total
					double spent = 0;

					for (Offer o : result) {

						//The offers returned are always equal to or below max price

						// Skip if the seller is the player attempting to purchase
						// This should only ever be true exactly ONCE, no need to optimize SQL query here
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
						if (VShop.economy.getBalance(player) < cost) {
							ChatUtils.sendError(player, "You do not have enough money!");
							return;
						}

						if(canBuyWholeOffer) {
							//If player can buy entire offer, delete the offer.
							queryManager.doAsyncDeleteOffer(o.sellerUUID, item.toString(), new VShopCallback<Void>() {
								@Override
								public void onSuccess(Void result) {
									//Deleted the offer
								}

								@Override
								public void onFailure(Exception cause) {
									handleFatalError(cause, player);
								}
							});
						} else {
							//If the player can't buy the entire offer, update the offer to set how much is left.
							//Variable <amountLeft> will always be initialized if you reach this branch
							queryManager.doAsyncUpdateOfferQuantity(o.sellerUUID, o.textID, amountLeft, new VShopCallback<Void>() {
								@Override
								public void onSuccess(Void result) {
									//Offer quantity updated
								}

								@Override
								public void onFailure(Exception cause) {
									handleFatalError(cause, player);
								}
							});
						}

						// Increment bought and spent
						bought += canBuy;
						spent += cost;

						// Get the seller Player. However, the player might be offline
						OfflinePlayer seller = plugin.getServer().getOfflinePlayer(UUID.fromString(o.sellerUUID));

						// Send money from the buyer to the seller
						VShop.economy.withdrawPlayer(player, cost);
						VShop.economy.depositPlayer(seller, cost);

						// Send a message notifying the seller of a sale if they are online
						if (seller.isOnline()) {
							Player onlineSeller = (Player) seller;
							ChatUtils.notifySeller(onlineSeller, player.getName(), canBuy, item, cost);
						}

						// Record the transaction if enabled in the config
						if(plugin.getConfig().getBoolean("log-transactions", false)) {
							Transaction t = new Transaction(o.sellerUUID, player.getUniqueId().toString(), o.textID, canBuy, cost);

							queryManager.doAsyncLogTransaction(t, new VShopCallback<Void>() {
								@Override
								public void onSuccess(Void result) {
									//Transaction logged
								}

								@Override
								public void onFailure(Exception cause) {
									handleFatalError(cause, player);
								}
							});
						}

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
				public void onFailure(Exception cause) {
					if(cause instanceof OffersNotFoundException) {
						ChatUtils.sendError(player, "There is no " + ChatUtils.formatItem(item) + " for sale.");
						return;
					}

					handleFatalError(cause, player);
				}
			});

			// Command has completed successfully
			return true;
		}

		return false;
	}

	private void handleFatalError(Exception cause, Player player) {
		ChatUtils.sendQueryError(player);
		plugin.handleUnexpectedException(cause);
	}

}
