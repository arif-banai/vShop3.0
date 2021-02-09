package me.arifbanai.vShop.commands;

import me.arifbanai.vShop.VShop;
import me.arifbanai.vShop.exceptions.OffersNotFoundException;
import me.arifbanai.vShop.interfaces.VShopCallback;
import me.arifbanai.vShop.managers.QueryManager;
import me.arifbanai.vShop.objects.Offer;
import me.arifbanai.vShop.utils.ChatUtils;
import me.arifbanai.vShop.utils.CommandUtils;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;


public class Recall implements CommandExecutor {

	private final VShop plugin;
	private final QueryManager queryManager;

	public Recall(final VShop instance, final QueryManager queryManager) {
		plugin = instance;
		this.queryManager = queryManager;
	}

	@Override
	/*
	 * This command will take offers made by the player for a certain item and
	 * remove them from the market and return them to the player The command format
	 * is /recall <item>
	 */
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().equalsIgnoreCase("recall")) {
			Player player;

			if(CommandUtils.isPlayerWithPerms(sender, cmd)) {
				player = (Player) sender;
			} else {
				return false;
			}

			// Check if the command is NOT formatted properly
			if (args.length < 1) {
				ChatUtils.sendError(player, "The proper usage is /recall <item>");
				return false;
			}

			StringBuilder itemLookup = new StringBuilder(args[0]);

			if (args.length > 1) {
				for (int i = 1; i < args.length; i++) {
					itemLookup.append(" ").append(args[i]);
				}
			}

			// Perform item lookup using Vault.Items API
			Material item = Material.matchMaterial(itemLookup.toString());

			// The above method returns null if it cannot find the item
			// The ID of the theItem will be 0 if the item is Material.AIR (aka nothing)
			if (item == null || item.equals(Material.AIR)) {
				ChatUtils.wrongItem(player, args[0]);
				return false;
			}

			//Get offer from the database
			queryManager.doAsyncGetOfferBySellerForItem(player.getUniqueId().toString(), item.toString(), new VShopCallback<List<Offer>>() {
				@Override
				public void onSuccess(List<Offer> result) {

					// total = sum of amount for each offer
					int total = 0;
					for (Offer o : result) {
						total += o.amount;
					}

					final int finalTotal = total;

					//Attempt to remove the offers made by the player for the item
					queryManager.doAsyncDeleteOffer(player.getUniqueId().toString(), item.toString(), new VShopCallback<Void>() {
						@Override
						public void onSuccess(Void result) {
							// Create a new ItemStack for the item recalled, with the total amount of items removed
							ItemStack itemstack = new ItemStack(item, finalTotal);

							HashMap<Integer, ItemStack> leftOver = player.getInventory().addItem(itemstack);

							for (ItemStack leftOverItem : leftOver.values()) {
								player.getWorld().dropItem(player.getLocation(), leftOverItem);
							}

							// Tell the player what item was removed, and in what amount
							ChatUtils.sendSuccess(sender,
									"Removed " + ChatUtils.formatAmount(finalTotal)
											+ " " + ChatUtils.formatItem(item));
						}

						@Override
						public void onFailure(Exception cause) {
							handleFatalError(cause, player);
						}
					});
				}

				@Override
				public void onFailure(Exception cause) {
					if (cause instanceof OffersNotFoundException) {
						ChatUtils.sendError(player, "You don't have any " + ChatUtils.formatItem(item) + " for sale");
						return;
					}

					handleFatalError(cause, player);
				}
			});

			// Command completed successfully
			return true;
		}
		return false;
	}

	private void handleFatalError(Exception cause, Player player) {
		ChatUtils.sendQueryError(player);
		plugin.handleUnexpectedException(cause);
	}
}
