package me.arifBanai.vShop.commands;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.arifBanai.vShop.Main;

public class Test implements CommandExecutor {
	
	protected Main plugin;
	
	public Test(final Main instance) {
		plugin = instance;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().equalsIgnoreCase("test")) {
			if(sender instanceof Player) {
				Player player = (Player) sender;
				
				player.sendMessage(Material.OAK_PLANKS.toString());
			}
		}
		
		return false;
	}

}
