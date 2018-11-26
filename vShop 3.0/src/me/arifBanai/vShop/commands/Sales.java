package me.arifBanai.vShop.commands;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.arifBanai.vShop.Main;
import me.arifBanai.vShop.objects.Transaction;
import me.arifBanai.vShop.utils.ChatUtils;
import me.arifBanai.vShop.utils.NumberUtils;

public class Sales implements CommandExecutor {

	protected Main plugin;

	public Sales(Main instance) {
		plugin = instance;
	}
	
	
	// /sales [pageNumber]
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().equalsIgnoreCase("sales")) {
			// TODO Implement /sales <playerName>
			
			if (!(sender instanceof Player)) {
				ChatUtils.denyConsole(sender);
				return false;
			}
			
			Player player = (Player) sender;
			
			int page = 1;
			
			if(args.length > 1) {
				ChatUtils.sendError(player, "Proper usage is /sales [pageNumber]");
				return false;
			} else if(args.length == 1) {
				page = NumberUtils.getInteger(args[0]);
				if (page <= 0) {
					ChatUtils.sendError(player, "The page number cannot equal to or less than 0.");
					return false;
				}
			}
			
			
			// Prepare the list of transactions
			List<Transaction> transactions = new ArrayList<Transaction>();
			
			try {
				// Find all transactions where the player is the seller
				transactions = plugin.getSQL().getTransactionsBySeller(player.getUniqueId().toString());
			} catch (SQLException | ClassNotFoundException e) {
				e.printStackTrace();
				ChatUtils.sendError(player, "An SQLException occured. Please alert admins. vShop shutting down.");
				plugin.disablePlugin();
			}
			
			
			// Check if ArrayList of transactions is empty
			if (transactions == null || transactions.size() == 0) {
				ChatUtils.sendError(player, "You have not sold anything.");
				return false;
			}

			// Prepare the page format for the chat window in-game
			int start = (page - 1) * 9;
			int pages = transactions.size() / 9 + 1;
			if (page > pages) {
				start = 0;
				page = 1;
			}
			
			// Top border of the page
			player.sendMessage(ChatColor.DARK_GRAY + "---------------" + ChatColor.GRAY + "Page (" + ChatColor.RED
					+ page + ChatColor.GRAY + " of " + ChatColor.RED + pages + ChatColor.GRAY + ")"
					+ ChatColor.DARK_GRAY + "---------------");

			// Start listing the transactions

			for (int count = start; count < transactions.size() && count < start + 9; count++) {

				Transaction t = transactions.get(count);

				// Format the transaction and send to player
				try {
					player.sendMessage(ChatUtils.formatYourTransaction( 
																	plugin.getIDLogger().getNameByUUID(t.buyerUUID), 
																	t.textID,
																	t.amount,
																	t.price));
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
