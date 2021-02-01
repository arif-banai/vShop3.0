package me.arifbanai.vShop.utils;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandUtils {

    public static boolean isPlayerWithPerms(CommandSender commandSender, Command cmd) {
        // Check if the sender is NOT an instance of Player
        if (!(commandSender instanceof Player)) {
            ChatUtils.denyConsole(commandSender);
            return false;
        }

        Player player = (Player) commandSender;

        // Check if the player DOES NOT have permission to use the command
        if (!player.hasPermission(cmd.getPermission())) {
            ChatUtils.noPermission(player);
            return false;
        }

        return true;
    }

}
