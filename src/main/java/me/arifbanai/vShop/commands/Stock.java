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
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.List;

public class Stock implements CommandExecutor {

	private VShop plugin;
	private QueryManager queryManager;
	private IDLogger idLogger;

	public Stock(final VShop instance, final QueryManager queryManager, final IDLogger idLogger) {
		plugin = instance;
		this.queryManager = queryManager;
		this.idLogger = idLogger;
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

				// If [pageNumber] argument is provided, validate args[1] and set <start> to
				// args[1]
				final int start;
				if (args.length == 2) {
					start = NumberUtils.getInteger(args[1]);

					if (start <= 0) {
						ChatUtils.sendError(player, "The page number cannot be 0.");
						return false;
					}
				} else {
					start = 1;
				}

				idLogger.doAsyncUUIDLookup(args[0], new IDLoggerCallback<String>() {
					@Override
					public void onSuccess(String result) {
						String sellerUUID = result;

						queryManager.doAsyncGetOffersBySeller(sellerUUID, new VShopCallback<List<Offer>>() {
							@Override
							public void onSuccess(List<Offer> result) {
								List<Offer> offers = result;

								// Prepare page formatting for chat window
								int newStart = (start - 1) * 9;
								int page = newStart / 9 + 1;
								int pages = offers.size() / 9 + 1;
								if (page > pages) {
									newStart = 0;
									page = 1;
								}

								// Top border of page
								sender.sendMessage(ChatColor.DARK_GRAY + "---------------" + ChatColor.GRAY + "Page (" + ChatColor.RED
										+ page + ChatColor.GRAY + " of " + ChatColor.RED + pages + ChatColor.GRAY + ")"
										+ ChatColor.DARK_GRAY + "---------------");
								for (int count = newStart; count < offers.size() && count < newStart + 9; count++) {
									Offer o = offers.get(count);
									sender.sendMessage(ChatUtils.formatOffer(args[0], o.amount, o.textID, o.price));
								}

							}

							@Override
							public void onFailure(Throwable cause) {
								if(cause instanceof OffersNotFoundException) {
									ChatUtils.sendError(player, "This player isn't selling anything.");
									return;
								}

								handleSqlError((SQLException) cause, player);
							}
						});
					}

					@Override
					public void onFailure(Throwable cause) {

						if(cause instanceof PlayerNotIDLoggedException) {
							ChatUtils.sendError(player, "Player " + args[0] + " not found.");
							return;
						}

						handleSqlError((SQLException) cause, player);
					}
				});

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

	private void handleSqlError(SQLException exception, Player player) {
		ChatUtils.sendError(player, "An SQLException occurred. Please alert admins. vShop shutting down.");
		plugin.handleUnexpectedException(exception);
	}
}