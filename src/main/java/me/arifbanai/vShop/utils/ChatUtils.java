package me.arifbanai.vShop.utils;

import me.arifbanai.vShop.objects.Offer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Objects;

import static org.bukkit.ChatColor.*;

public class ChatUtils {

	private static final String prefixDatabase = "vShop_";

	private static final String prefix = GRAY + "[" + GREEN + "vShop" + GRAY + "] " + RESET;

	public static String getDatabasePrefix() {
		return prefixDatabase;
	}

	public static String formatSeller(String seller) {
		return RED + seller + RESET;
	}

	public static String formatAmount(int amount) {
		return GOLD + Integer.toString(amount) + RESET;
	}

	public static String formatItem(Material item) {
		return BLUE + item.toString() + RESET;
	}

	public static String formatPrice(double cost) {
		return YELLOW + Double.toString(cost) + RESET;
	}

	public static String formatBuyer(String buyer) {
		return AQUA + buyer + RESET;
	}

	public static String formatOffer(String name, int amount, String textID, double price) {
		return formatSeller(name) + ": " + formatAmount(amount) + " "
				+ formatItem(Objects.requireNonNull(Material.getMaterial(textID))) + " for " + formatPrice(price) + " each.";
	}
	
	public static String formatYourTransaction(String buyerName, String textID, int amount, double price) {
		return formatSeller("You") + " sold " + formatAmount(amount) + " " + formatItem(Objects.requireNonNull(Material.getMaterial(textID))) + " to " + formatBuyer(buyerName) + " for " + formatPrice(price);
	}

	public static void sendSuccess(CommandSender sender, String msg) {
		sender.sendMessage(prefix + GREEN + msg);
	}

	public static void sendError(CommandSender sender, String msg) {
		sender.sendMessage(prefix + RED + msg);
	}

	public static void sendGlobal(String message) {
		Bukkit.getServer().broadcastMessage(prefix + message);
	}

	public static void denyConsole(CommandSender sender) {
		sendError(sender, "You must be in-game to do this.");
	}

	public static void noPermission(CommandSender sender) {
		sendError(sender, "You do not have permission to do this.");
	}

	public static void wrongItem(CommandSender sender, String itemName) {
		sendError(sender, "What is \"" + itemName + "\" ?");
	}

	public static void notifySeller(Player seller, String buyerName, int amount, Material item, double cost) {
		ChatUtils.sendSuccess(seller, ChatUtils.formatSeller(buyerName) + " just bought "
				+ ChatUtils.formatAmount(amount) + " " + ChatUtils.formatItem(item)
				+ " for " + ChatUtils.formatPrice(cost));
	}

	public static void broadcastOffer(String name, int amount, String textID, double price) {
		sendGlobal(formatOffer(name, amount, textID, price));
	}

	public static void broadcastOffer(String name, Offer o) {
		broadcastOffer(name, o.amount, o.textID, o.price);
	}

	public static void sendQueryError(Player player) {
		sendError(player, "An Exception occurred while querying. Please alert admins. vShop shutting down.");
	}
}
