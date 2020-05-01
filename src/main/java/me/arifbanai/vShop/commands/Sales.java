package me.arifbanai.vShop.commands;

import me.arifbanai.vShop.VShop;
import me.arifbanai.vShop.objects.Transaction;
import me.arifbanai.vShop.utils.ChatUtils;
import me.arifbanai.vShop.utils.NumberUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Sales implements CommandExecutor {

	// TODO Bring this command back

	/**
	 * Executes the given command, returning its success.
	 * <br>
	 * If false is returned, then the "usage" plugin.yml entry for this command
	 * (if defined) will be sent to the player.
	 *
	 * @param sender  Source of the command
	 * @param command Command which was executed
	 * @param label   Alias of the command which was used
	 * @param args    Passed command arguments
	 * @return true if a valid command, otherwise false
	 */
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		return false;
	}



	/*

	private VShop plugin;

	public Sales(final VShop instance) {
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
					//TODO Uncomment when bringing back Sales command (this causes an error)
					player.sendMessage(ChatUtils.formatYourTransaction( plugin.getIDLogger().getNameByUUID(t.buyerUUID),
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


	 */


}
