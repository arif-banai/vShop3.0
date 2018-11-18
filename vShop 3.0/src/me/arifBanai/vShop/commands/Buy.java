package me.arifBanai.vShop.commands;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.arifBanai.vShop.Main;
import me.arifBanai.vShop.objects.Offer;
import me.arifBanai.vShop.objects.Transaction;
import me.arifBanai.vShop.utils.ChatUtils;
import me.arifBanai.vShop.utils.NumberUtils;


public class Buy implements CommandExecutor {

	protected Main plugin;

	public Buy(final Main instance) {
		plugin = instance;
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

			int amount = NumberUtils.getInteger(args[0]);

			// Check if the amount is invalid
			if (amount <= 0) {
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

			String textID = item.toString();

			List<Offer> offers = new ArrayList<>();

			// Check if there are offers in the database, if not, it will return an empty
			// ArrayList
			try {
				offers = plugin.getSQL().getItemOffers(textID);
			} catch (SQLException | ClassNotFoundException e) {
				e.printStackTrace();
				ChatUtils.sendError(player, "An SQLException occured. Please alert admins. vShop shutting down.");
				plugin.disablePlugin();
				return false;
			}

			// Check if the ArrayList of offers is empty
			if (offers.size() == 0) {
				ChatUtils.sendError(player, "There is no " + ChatUtils.formatItem(item) + " for sale.");
				return false;
			}

			// Used to keep track of how much the buyer has bought in total
			int bought = 0;
			// Used to keep track of how much the buyer has spent in total
			double spent = 0;

			for (Offer o : offers) {
				if (o.price <= maxPrice) {
					// Skip offer if the seller is the player attempting to purchase
					if (o.sellerUUID.equalsIgnoreCase(player.getUniqueId().toString())) {
						continue;
					}

					// If the amount that can be purchased is greater than or equal to the current
					// offer's amount
					int canBuy;
					double cost;
					
					if (amount - bought >= o.amount) {
						canBuy = o.amount;
						cost = o.price * canBuy;

						// TODO I think there is a better way of doing this check
						// Check if player has at least enough money
						if (Main.economy.getBalance(player) >= cost) {
							canBuy = Math.min((int) (Main.economy.getBalance(player) / o.price),o.amount);

							// If canBuy is less than 1, the player doesn't have enough money
							if (canBuy < 1) {
								ChatUtils.sendError(player, "Ran out of money!");
								break;
							}

							// Calculate the cost of buying the current offer
							cost = canBuy * o.price;
						}

						// Increment bought and spent
						bought += canBuy;
						spent += cost;

						// Because (amount - bought) >= o.amount, the offer will not have any items left
						// This means that the player bought the whole order of items
						// So we must delete the offer from the database

						try {
							plugin.getSQL().deleteOffer(o.sellerUUID, item.toString());
						} catch (SQLException | ClassNotFoundException e) {
							e.printStackTrace();
							ChatUtils.sendError(player,
									"An SQLException occured. Please alert admins. vShop shutting down.");
							plugin.disablePlugin();
							return false;
						}

						
					} else {
						// This executes if the offer has more items than can possibly be bought
						canBuy = amount - bought;
						cost = o.price * canBuy;

						if (Main.economy.getBalance(player) >= cost) {
							canBuy = Math.min((int) (Main.economy.getBalance(player) / o.price),o.amount);
							cost = canBuy * o.price;

							if (canBuy < 1) {
								ChatUtils.sendError(player, "Ran out of money!");
								break;
							}
						}

						bought += canBuy;
						spent += cost;

						// amountLeft will always be greater than or equal to 1
						int amountLeft = o.amount - canBuy;

						try {
							plugin.getSQL().updateQuantity(o.sellerUUID, o.textID, amountLeft);
						} catch (SQLException | ClassNotFoundException e) {
							e.printStackTrace();
							ChatUtils.sendError(player,
									"An SQLException occured. Please alert admins. vShop shutting down.");
							plugin.disablePlugin();
							return false;
						}

					}
					
					// Get the seller Player, however, the player might be offline, so create
					// OfflinePlayer
					OfflinePlayer seller = plugin.getServer().getOfflinePlayer(UUID.fromString(o.sellerUUID));

					// Send money from the buyer to the seller
					Main.economy.withdrawPlayer(player, cost);
					Main.economy.depositPlayer(seller, cost);

					if (seller.isOnline()) {
						
						Player onlineSeller = (Player) seller;
						
						ChatUtils.sendSuccess(onlineSeller,
								ChatUtils.formatSeller(player.getName()) + " just bought "
										+ ChatUtils.formatAmount(canBuy) + " " + ChatUtils.formatItem(item)
										+ " for " + ChatUtils.formatPrice(cost));
					}

					Transaction t = new Transaction(o.sellerUUID, player.getUniqueId().toString(), o.textID, canBuy, cost);
					
					try {
						plugin.getSQL().logTransaction(t);
					} catch (SQLException | ClassNotFoundException e) {
						e.printStackTrace();
						ChatUtils.sendError(player,
								"An SQLException occured. Please alert admins. vShop shutting down.");
						plugin.disablePlugin();
						return false;
					}

					if (bought >= amount) {
						break;
					}
				}
			}

			// Create a new ItemStack with the item purchased, and the amount bought
			ItemStack purchasedItem = new ItemStack(item, bought);

			HashMap<Integer, ItemStack> leftOver = player.getInventory().addItem(purchasedItem);
			
			for(ItemStack leftOverItem : leftOver.values()) {
				player.getWorld().dropItem(player.getLocation(), leftOverItem);
			}

			// Tell the buyer how much he has bought with a certain amount of money spent
			ChatUtils.sendSuccess(player, "Managed to buy " + ChatUtils.formatAmount(bought) + " "
					+ ChatUtils.formatItem(item) + " for " + ChatUtils.formatPrice(spent));

			// Command has completed successfully
			return true;
		}

		return false;
	}
	

}
