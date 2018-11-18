package com.smeefgaming.vShop.commands;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.smeefgaming.vShop.Main;
import com.smeefgaming.vShop.objects.Offer;
import com.smeefgaming.vShop.utils.ChatUtils;
import com.smeefgaming.vShop.utils.NumberUtils;


public class Find implements CommandExecutor {

	protected Main plugin;

	public Find(final Main instance) {
		plugin = instance;
	}

	@Override
	/*
	 * This command will find all offers for a certain item in the database The
	 * command format is /find <item> [pageNumber]
	 */
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().equalsIgnoreCase("find")) {
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

			String itemLookup;
			boolean noPageArgument = false;
			
			// Check if the command is NOT formatted properly
			if (args.length < 1) {
				ChatUtils.sendError(player, "Proper usage is /find <item> [pageNumber]");
				return false;
			} else if(args.length == 1) {
				itemLookup = args[0];
				noPageArgument = true;
			} else {
				itemLookup = args[0];
				for(int i = 1; i < args.length - 1; i++) {
					itemLookup += " " + args[i];
				}
				
				if(NumberUtils.getInteger(args[args.length - 1]) < 0) {
					itemLookup += " " + args[args.length - 1];
					noPageArgument = true;
				}
			}

			// Perform item lookup using Vault.Items API
			Material item = Material.matchMaterial(itemLookup);

			// If item lookup failed, it will return null
			// If the item is nothing, the ID will be 0 (Material.AIR aka nothing)
			if (item == null || item.equals(Material.AIR)) {
				ChatUtils.wrongItem(player, itemLookup);
				return false;
			}
			
			// Prepare the page number, if args.length is less than 1, return false
			// OR if args.length >= 2, the pageNumber may be in the last argument
			// 		Use <noPageArgument> to check if the last argument is the pageNumber
			// If [page] is used, validate the input
			int page = 1;
			if (!noPageArgument) {
				page = NumberUtils.getInteger(args[args.length-1]);
				if (page <= 0) {
					ChatUtils.sendError(player, "The page number cannot equal to or less than 0.");
					return false;
				}
			}
			
			// Prepare the list of offers
			List<Offer> offers = new ArrayList<Offer>();
			try {
				// Find offers for this item in the database, returns empty ArrayList if none
				// found
				offers = plugin.getSQL().getItemOffers(item.toString());
			} catch (SQLException | ClassNotFoundException e) {
				e.printStackTrace();
				ChatUtils.sendError(player, "An SQLException occured. Please alert admins. vShop shutting down.");
				plugin.disablePlugin();
			}

			// Check if ArrayList of offers is empty
			if (offers == null || offers.size() == 0) {
				ChatUtils.sendError(player, "There is no " + ChatUtils.formatItem(item) + " for sale.");
				return false;
			}

			// Prepare the page format for the chat window in-game
			int start = (page - 1) * 9;
			int pages = offers.size() / 9 + 1;
			if (page > pages) {
				start = 0;
				page = 1;
			}

			// Top border of the page
			player.sendMessage(ChatColor.DARK_GRAY + "---------------" + ChatColor.GRAY + "Page (" + ChatColor.RED
					+ page + ChatColor.GRAY + " of " + ChatColor.RED + pages + ChatColor.GRAY + ")"
					+ ChatColor.DARK_GRAY + "---------------");

			// Start listing the offers
			
			for (int count = start; count < offers.size() && count < start + 9; count++) {
				
				Offer o = offers.get(count);
				
				// Format the offer and send to player
				try {
					sender.sendMessage(ChatUtils.formatOffer(plugin.getIDLogger().getNameByUUID(o.sellerUUID), o.amount, o.textID, o.price));
				} catch (Exception e) {
					e.printStackTrace();
					ChatUtils.sendError(player,
							"An SQLException occured. Please alert admins. vShop shutting down.");
					plugin.disablePlugin();
				}
				
			}

			// Command has completed successfully
			return true;
		}
		return false;
	}

}
