package me.arifbanai.vShop.utils;

import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class InventoryUtils {

	// Removes an amount of items from a players inventory
	public static void removeInventoryItems(Inventory inv, Material item, int amount) {
		ItemStack[] items = inv.getStorageContents();

		for (int i = 0; i < items.length; i++) {
			ItemStack temp = items[i];

			if (temp != null && temp.getType().equals(item)) {
				int newAmount = temp.getAmount() - amount;

				if (newAmount > 0) {
					temp.setAmount(newAmount);
					break;
				} else {
					if (amount == 0)
						break;
					
					items[i] = new ItemStack(Material.AIR);
					amount = -1 * newAmount;
				}
			}
		}

		inv.setStorageContents(items);
	}
}
