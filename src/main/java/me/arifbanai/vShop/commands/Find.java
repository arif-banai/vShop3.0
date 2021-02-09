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
import me.arifbanai.vShop.utils.CommandUtils;
import me.arifbanai.vShop.utils.NumberUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

import static me.arifbanai.vShop.utils.ChatUtils.formatOffer;

public class Find implements CommandExecutor {

	private final VShop plugin;
	private final QueryManager queryManager;
	private final IDLogger idLogger;

	public Find(final VShop instance, final QueryManager queryManager, final IDLogger idLogger) {
		plugin = instance;
		this.queryManager = queryManager;
		this.idLogger = idLogger;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().equalsIgnoreCase("find")) {
			Player player;

			if(CommandUtils.isPlayerWithPerms(sender, cmd)) {
				player = (Player) sender;
			} else {
				return false;
			}

			// Check if the command is NOT formatted properly
			if (args.length < 1) {
				ChatUtils.sendError(player, "Proper usage is /find <item> [pageNumber]");
				return false;
			}

			StringBuilder itemLookup = new StringBuilder(args[0]);
			boolean noPageArgument = false;
			
			if(args.length == 1) {
				noPageArgument = true;
			} else {
				itemLookup = new StringBuilder(args[0]);
				for(int i = 1; i < args.length - 1; i++) {
					itemLookup.append(" ").append(args[i]);
				}

				if(NumberUtils.getInteger(args[args.length - 1]) < 0) {
					itemLookup.append(" ").append(args[args.length - 1]);
					noPageArgument = true;
				}
			}

			// Perform item lookup against the Material enum
			Material item = Material.matchMaterial(itemLookup.toString());

			// If item lookup failed, it will return null
			// If the item is nothing, the ID will be 0 (Material.AIR aka nothing)
			if (item == null || item.equals(Material.AIR)) {
				ChatUtils.wrongItem(player, itemLookup.toString());
				return false;
			}

			// Item lookup is expecting 'hand' to be an INVALID argument
			// 	 as opposed to the Sell command
			
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

			queryManager.doAsyncGetAmountOfItemOffers(item.toString(), new VShopCallback<Integer>() {
				@Override
				public void onSuccess(Integer result) {
					// Prepare the page format for the chat window in-game
					int pages = result / 9 + 1;
					int pageNumber = page;
					int start;

					if (pageNumber > pages) {
						start = 0;
						pageNumber = 1;
					} else {
						start = (pageNumber - 1) * 9;
					}

					printResultsHeadline(player, pageNumber, pages);
					queryManager.doAsyncGetItemOffersOffset(item.toString(), start, new VShopCallback<List<Offer>>() {

						@Override
						public void onSuccess(List<Offer> result) {
							for(Offer o : result) {
								idLogger.doAsyncNameLookup(o.sellerUUID, new IDLoggerCallback<String>() {
									@Override
									public void onSuccess(String result) {
										String offerMessage = formatOffer(result, o.amount, o.textID, o.price);
										player.sendMessage(offerMessage);
									}

									@Override
									public void onFailure(Exception cause) {

										// USE THE RIGHT EXCEPTION (IDLogger exception, this is an IDLogger method)
										if(cause instanceof PlayerNotIDLoggedException) {
											ChatUtils.sendError(player, "IDLogger couldn't get the seller's name. Please alert admins");
										}

										handleFatalError(cause, player);
									}
								});
							}
						}

						@Override
						public void onFailure(Exception cause) {
							//At this point, there should always be offers to find in the table
							handleFatalError(cause, player);
						}
					});
				}

				@Override
				public void onFailure(Exception cause) {
					if(cause instanceof OffersNotFoundException) {
						ChatUtils.sendError(player, "There is no " + ChatUtils.formatItem(item) + " for sale.");
						return;
					}

					handleFatalError(cause, player);
				}
			});

			// Command has completed successfully
			return true;
		}
		return false;
	}

	private void printResultsHeadline(Player player, int pageNumber, int pages) {
		// Top border of the page
		player.sendMessage(ChatColor.DARK_GRAY + "---------------" + ChatColor.GRAY + "Page (" + ChatColor.RED
				+ pageNumber + ChatColor.GRAY + " of " + ChatColor.RED + pages + ChatColor.GRAY + ")"
				+ ChatColor.DARK_GRAY + "---------------");
	}

	private void handleFatalError(Exception cause, Player player) {
		ChatUtils.sendQueryError(player);
		plugin.handleUnexpectedException(cause);
	}
}
