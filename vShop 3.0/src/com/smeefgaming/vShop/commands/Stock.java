package com.smeefgaming.vShop.commands;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.smeefgaming.vShop.Main;
import com.smeefgaming.vShop.objects.Offer;
import com.smeefgaming.vShop.utils.ChatUtils;
import com.smeefgaming.vShop.utils.NumberUtils;

public class Stock implements CommandExecutor {

	protected Main plugin;

	public Stock(final Main instance) {
		plugin = instance;
	}

	@Override
	/*
	 * This command will display a list of offers made by a select seller The format
	 * of the command is /stock <playerName> [pageNumber]
	 */
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().equalsIgnoreCase("stock")) {
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

			// Check if the command is formatted properly
			if (args.length > 0 && !(args.length > 2)) {
				/*
				 * Get the UUID of the seller in question from the seller's in-game name. This
				 * is deprecated because player's can change their in-game name. This can be
				 * fixed by simply logging the player's UUID and name upon joining and checking
				 * if the player has changed their name everytime they join the server
				 */
				// TODO Create a UUID-Name logging system
				//String sellerUUID = plugin.getServer().getOfflinePlayer(args[0]).getUniqueId().toString();
				String sellerUUID = null;
				try {
					sellerUUID = plugin.getIDLogger().getUUIDByName(args[0]);
				} catch (SQLException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
				if(sellerUUID == null) {
					ChatUtils.sendError(player, "Player " + args[0] + " not found.");
					return false;
				}

				// Prepare list of offers
				List<Offer> offers = new ArrayList<Offer>();

				// Find offers made by <sellerUUID>
				try {
					offers = plugin.getSQL().searchBySeller(sellerUUID);
				} catch (SQLException | ClassNotFoundException e) {
					e.printStackTrace();
					ChatUtils.sendError(player, "An SQLException occured. Please alert admins. vShop shutting down.");
					plugin.disablePlugin();
				}

				// Check if offers is empty
				if (offers.size() == 0) {
					ChatUtils.sendError(player, "This player isn't selling anything.");
					return false;
				}

				// If [pageNumber] argument is provided, validate args[1] and set <start> to
				// args[1]
				int start = 1;
				if (args.length == 2) {
					start = NumberUtils.getInteger(args[1]);

					if (start <= 0) {
						ChatUtils.sendError(player, "The page number cannot be 0.");
						return false;
					}
				}

				// Prepare page formatting for chat window
				start = (start - 1) * 9;
				int page = start / 9 + 1;
				int pages = offers.size() / 9 + 1;
				if (page > pages) {
					start = 0;
					page = 1;
				}

				// Top border of page
				sender.sendMessage(ChatColor.DARK_GRAY + "---------------" + ChatColor.GRAY + "Page (" + ChatColor.RED
						+ page + ChatColor.GRAY + " of " + ChatColor.RED + pages + ChatColor.GRAY + ")"
						+ ChatColor.DARK_GRAY + "---------------");
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

				// Command completed successfully
				return true;
			} else {
				// The command is not formatted properly
				ChatUtils.sendError(player, "The proper usage is /stock <player> [page]");
				return false;
			}
		}
		return false;
	}

}
