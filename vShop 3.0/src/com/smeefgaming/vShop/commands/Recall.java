package com.smeefgaming.vShop.commands;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.smeefgaming.vShop.Main;
import com.smeefgaming.vShop.objects.Offer;
import com.smeefgaming.vShop.utils.ChatUtils;


public class Recall implements CommandExecutor {

	protected Main plugin;

	public Recall(final Main instance) {
		plugin = instance;
	}

	@Override
	/*
	 * This command will take offers made by the player for a certain item and
	 * remove them from the market and return them to the player The command format
	 * is /recall <item>
	 */
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().equalsIgnoreCase("recall")) {
			// Check if sender is NOT an instance of Player
			if (!(sender instanceof Player)) {
				ChatUtils.denyConsole(sender);
				return false;
			}

			Player player = (Player) sender;

			// Check if player DOES NOT have permission to use the command
			if (!player.hasPermission(cmd.getPermission())) {
				ChatUtils.noPermission(player);
				return false;
			}

			// Check if the command is NOT formatted properly
			if (args.length < 1) {
				ChatUtils.sendError(player, "The proper usage is /recall <item>");
				return false;
			}
			
			String itemLookup;
			
			if(args.length > 1) {
				itemLookup = args[0];
				
				for(int i = 1; i < args.length; i++) {
					itemLookup += " " + args[i];
				}
			} else {
				itemLookup = args[0];
			}

			// Perform item lookup using Vault.Items API
			Material item = Material.matchMaterial(itemLookup);

			// The above method returns null if it cannot find the item
			// The ID of the theItem will be 0 if the item is Material.AIR (aka nothing)
			if (item == null || item.equals(Material.AIR)) {
				ChatUtils.wrongItem(player, args[0]);
				return false;
			}

			// Keep track of items to be removed from the database
			int total = 0;

			// Prepare list of offers
			List<Offer> offers = new ArrayList<>();

			try {
				// Find offers made by player for certain item
				offers = plugin.getSQL().getSellerOffers(player.getUniqueId().toString(), item.toString());

				// Check if the player doesn't have the item for sale
				if (offers.size() == 0) {
					ChatUtils.sendError(player, "You don't have any " + ChatUtils.formatItem(item) + " for sale");
					return false;
				}

				// For all offers found made by the player for a certain item, increment total
				// by the amount of the offer
				for (Offer o : offers) {
					total += o.amount;
				}
			} catch (ClassNotFoundException | SQLException e1) {
				e1.printStackTrace();
				ChatUtils.sendError(player, "An SQLException occured. Please alert admins. vShop shutting down.");
				plugin.disablePlugin();
			}

			// The variable <total> must be greater than 0 at this point

			// Attempt to remove the offers made by the player for the item
			try {
				plugin.getSQL().deleteOffer(player.getUniqueId().toString(), item.toString());
			} catch (SQLException | ClassNotFoundException e) {
				e.printStackTrace();
				ChatUtils.sendError(player, "An SQLException occured. Please alert admins. vShop shutting down.");
				plugin.disablePlugin();
			}

			// Create a new ItemStack for the item recalled, with the total amount of items
			// removed
			ItemStack itemstack = new ItemStack(item, total);
			
			HashMap<Integer, ItemStack> leftOver = player.getInventory().addItem(itemstack);
			
			for(ItemStack leftOverItem : leftOver.values()) {
				player.getWorld().dropItem(player.getLocation(), leftOverItem);
			}

			// Tell the player what item was removed, and in what amount
			ChatUtils.sendSuccess(sender,
					"Removed " + ChatUtils.formatAmount(total) + " " + ChatUtils.formatItem(item));

			// Command completed successfully
			return true;
		}
		return false;
	}

}
