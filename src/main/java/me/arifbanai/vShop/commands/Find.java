package me.arifbanai.vShop.commands;

import me.arifbanai.idLogger.IDLogger;
import me.arifbanai.idLogger.exceptions.PlayerNotIDLoggedException;
import me.arifbanai.idLogger.interfaces.IDLoggerCallback;
import me.arifbanai.vShop.VShop;
import me.arifbanai.vShop.exceptions.OffersNotFoundException;
import me.arifbanai.vShop.interfaces.VShopCallback;
import me.arifbanai.vShop.managers.QueryManager;
import me.arifbanai.vShop.objects.Offer;
import me.arifbanai.vShop.utils.ChatUtils;
import me.arifbanai.vShop.utils.NumberUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.List;

public class Find implements CommandExecutor {

	private final VShop plugin;
	private final QueryManager queryManager;
	private final IDLogger idLogger;

	public Find(final VShop instance, final QueryManager queryManager, final IDLogger idLogger) {
		plugin = instance;
		this.queryManager = queryManager;
		this.idLogger = idLogger;
	}

	/**
	 * Find offers for some item
	 * TODO javadocs!
	 *
	 * @param sender
	 * @param cmd
	 * @param label
	 * @param args
	 * @return
	 */
	@Override
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

			// Perform item lookup against the Material enum
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
			final int page;
			if (!noPageArgument) {
				page = NumberUtils.getInteger(args[args.length-1]);
				if (page <= 0) {
					ChatUtils.sendError(player, "The page number cannot equal to or less than 0.");
					return false;
				}
			} else {
				page = 1;
			}

			queryManager.doAsyncGetItemOffers(item.toString(), new VShopCallback<List<Offer>>() {
				@Override
				public void onSuccess(List<Offer> result) {
					List<Offer> offersByItem = result;

					int pageNumber = page;

					// Prepare the page format for the chat window in-game
					int start = (pageNumber - 1) * 9;
					int pages = offersByItem.size() / 9 + 1;
					if (pageNumber > pages) {
						start = 0;
						pageNumber = 1;
					}

					// Top border of the page
					player.sendMessage(ChatColor.DARK_GRAY + "---------------" + ChatColor.GRAY + "Page (" + ChatColor.RED
							+ pageNumber + ChatColor.GRAY + " of " + ChatColor.RED + pages + ChatColor.GRAY + ")"
							+ ChatColor.DARK_GRAY + "---------------");

					// Start listing the offers
					for (int count = start; count < offersByItem.size() && count < start + 9; count++) {
						Offer o = offersByItem.get(count);

						idLogger.doAsyncNameLookup(o.sellerUUID, new IDLoggerCallback<String>() {
							@Override
							public void onSuccess(String result) {
								String sellerName = result;

								String offerMessage = ChatUtils.formatOffer(sellerName, o.amount, o.textID, o.price);
								player.sendMessage(offerMessage);
							}

							@Override
							public void onFailure(Throwable cause) {

								// USE THE RIGHT EXCEPTION (IDLogger exception, this is an IDLogger method)
								if(cause instanceof PlayerNotIDLoggedException) {
									ChatUtils.sendError(player, "IDLogger couldn't get the players name. Please alert admins");
								}

								handleSqlError((SQLException) cause, player);
							}
						});
					}
				}

				@Override
				public void onFailure(Throwable cause) {
					if(cause instanceof OffersNotFoundException) {
						ChatUtils.sendError(player, "There is no " + ChatUtils.formatItem(item) + " for sale.");
						return;
					}

					handleSqlError((SQLException) cause, player);
				}
			});

			// Command has completed successfully
			return true;
		}
		return false;
	}

	private void handleSqlError(SQLException exception, Player player) {
		ChatUtils.sendError(player, "An SQLException occured. Please alert admins. vShop shutting down.");
		plugin.handleUnexpectedException(exception);
	}
}
