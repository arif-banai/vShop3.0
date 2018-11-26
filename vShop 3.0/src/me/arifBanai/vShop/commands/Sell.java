package me.arifBanai.vShop.commands;

import java.sql.SQLException;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;

import me.arifBanai.vShop.Main;
import me.arifBanai.vShop.objects.Offer;
import me.arifBanai.vShop.utils.ChatUtils;
import me.arifBanai.vShop.utils.InventoryUtils;
import me.arifBanai.vShop.utils.NumberUtils;

public class Sell implements CommandExecutor {

	private Main plugin;

	public Sell(final Main instance) {
		plugin = instance;
	}

	@Override
	/*
	 * This command will take an item a player has and attempt to place an amount of
	 * them in the database, with a price PER EACH item The command format is /sell
	 * <amount> <itemName/hand> <pricePerEach>
	 */
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().equalsIgnoreCase("sell")) {
			// Check if the sender is NOT and instance of Player
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
			if (args.length < 3) {
				ChatUtils.sendError(player, "The proper usage is /sell <amount> <item> <price>");
				return false;
			}

			int amount = NumberUtils.getInteger(args[0]);

			// Check if the amount is invalid
			if (amount <= 0) {
				ChatUtils.sendError(player, "Invalid amount");
				return false;
			}

			float price;

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

			// Check if the item is Matieral.AIR (aka nothing)
			if (item.equals(Material.AIR)) {
				ChatUtils.wrongItem(player, "AIR");
				return false;
			}

			// Check if player's inventory DOES NOT have at least <amount> of <item>
			if (!inv.contains(item, amount)) {
				ChatUtils.sendError(sender, "You do not have " + ChatUtils.formatAmount(NumberUtils.getInteger(args[0]))
						+ " " + ChatUtils.formatItem(item));
				return true;
			}

			// Remove the items to be sold from the seller's inventory
			InventoryUtils.removeInventoryItems(inv, item, amount);

			// Check if offer already exists in database
			// If so, then add <amount> and amount in given offer
			try {
				int existingAmount = 0;

				List<Offer> theOffers = plugin.getSQL().getSellerOffers(player.getUniqueId().toString(),
						item.toString());

				if (theOffers.size() > 0) {
					for (Offer o : theOffers)
						existingAmount += o.amount;

					plugin.getSQL().deleteOffer(player.getUniqueId().toString(), item.toString());

					amount += existingAmount;
				}

			} catch (ClassNotFoundException | SQLException e1) {
				e1.printStackTrace();
				ChatUtils.sendError(player, "An SQLException occured. Please alert admins. vShop shutting down.");
				plugin.disablePlugin();
			}

			// Create new offer
			Offer o = new Offer(player.getUniqueId().toString(), item.toString(), amount, price);

			// Attempt to add offer to the database
			try {
				plugin.getSQL().addOffer(o);
			} catch (SQLException | ClassNotFoundException e) {
				e.printStackTrace();
				ChatUtils.sendError(player, "An SQLException occured. Please alert admins. vShop shutting down.");
				plugin.disablePlugin();
			}

			// If enabled, the offer will be broadcast to all online players
			if (plugin.getConfigManager().broadcastOffers()) {
				ChatUtils.broadcastOffer(player.getName(), o.amount, o.textID, o.price);
			}

			// Command has completed successfully
			return true;
		}

		return false;
	}
}
