package me.arifBanai.vShop.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import me.arifBanai.vShop.Main;

public class Sales implements CommandExecutor {

	protected Main plugin;

	public Sales(Main instance) {
		plugin = instance;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().equalsIgnoreCase("sales")) {
			// TODO Implement /sales <playerName>
		}
		return false;
	}

}
