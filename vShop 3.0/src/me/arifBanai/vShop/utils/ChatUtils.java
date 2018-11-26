package me.arifBanai.vShop.utils;

import static org.bukkit.ChatColor.AQUA;
import static org.bukkit.ChatColor.BLUE;
import static org.bukkit.ChatColor.GOLD;
import static org.bukkit.ChatColor.GRAY;
import static org.bukkit.ChatColor.GREEN;
import static org.bukkit.ChatColor.RED;
import static org.bukkit.ChatColor.RESET;
import static org.bukkit.ChatColor.YELLOW;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;

public class ChatUtils {

	private static String prefixDatabase = "vShop_";

	private static String prefix = GRAY + "[" + GREEN + "vShop" + GRAY + "] " + RESET;

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
				+ formatItem(Material.getMaterial(textID)) + " for " + formatPrice(price) + " each.";
	}
	
	public static String formatYourTransaction(String buyerName, String textID, int amount, double price) {
		return formatSeller("You") + " sold " + formatAmount(amount) + " " + formatItem(Material.getMaterial(textID)) + " to" + "\t" + formatBuyer(buyerName) + " for" + "\t" + formatPrice(price);
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

	public static void broadcastOffer(String name, int amount, String textID, double price) {
			sendGlobal(formatOffer(name, amount, textID, price));
	}

}
