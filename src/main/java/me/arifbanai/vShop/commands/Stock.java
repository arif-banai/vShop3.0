package me.arifbanai.vShop.commands;

import jdk.nashorn.internal.codegen.CompilerConstants;
import me.arifbanai.vShop.Main;
import me.arifbanai.vShop.exceptions.OffersNotFoundException;
import me.arifbanai.vShop.exceptions.PlayerNotFoundException;
import me.arifbanai.vShop.interfaces.Callback;
import me.arifbanai.vShop.objects.Offer;
import me.arifbanai.vShop.utils.ChatUtils;
import me.arifbanai.vShop.utils.NumberUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Stock implements CommandExecutor {

	private Main plugin;

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

				doUUIDLookupAsync(args[0], new Callback<String>() {
					@Override
					public void onSuccess(String result) {
						String sellerUUID = result;

						doGetOffersBySellerAsync(sellerUUID, new Callback<List<Offer>>() {
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

								cause.printStackTrace();
								ChatUtils.sendError(player, "An SQLException occured. Please alert admins. vShop shutting down.");
								plugin.disablePlugin();
							}
						});
					}

					@Override
					public void onFailure(Throwable cause) {

						if(cause instanceof PlayerNotFoundException) {
							ChatUtils.sendError(player, "Player " + args[0] + " not found.");
							return;
						}

						cause.printStackTrace();
						ChatUtils.sendError(player, "An SQLException occured. Please alert admins. vShop shutting down.");
						plugin.disablePlugin();
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

	private void doUUIDLookupAsync(final String playerName, final Callback<String> callback) {
		Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
			@Override
			public void run() {
				try {
					final String result = plugin.getIDLogger().getUUIDByName(playerName);

					Bukkit.getScheduler().runTask(plugin, new Runnable() {
						@Override
						public void run() {

							if(result == null) {
								callback.onFailure(new PlayerNotFoundException());
								return;
							}

							callback.onSuccess(result);
						}
					});
				} catch (SQLException throwables) {
					callback.onFailure(throwables);
				}
			}
		});
	}

	private void doGetOffersBySellerAsync(final String playerUUID, final Callback<List<Offer>> callback) {
		Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
			@Override
			public void run() {
				try {
					final List<Offer> result = plugin.getSQL().searchBySeller(playerUUID);

					Bukkit.getScheduler().runTask(plugin, new Runnable() {
						@Override
						public void run() {

							if(result.size() == 0) {
								callback.onFailure(new OffersNotFoundException());
								return;
							}

							callback.onSuccess(result);
						}
					});
				} catch (SQLException | ClassNotFoundException throwables) {
					callback.onFailure(throwables);
				}
			}
		});
	}

}
