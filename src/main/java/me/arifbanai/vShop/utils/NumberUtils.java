package me.arifbanai.vShop.utils;

public class NumberUtils {
	public static int getInteger(String s) {
		try {
			return Integer.parseInt(s);
		} catch (NumberFormatException | NullPointerException e) {
			return -1;
		}
	}

	public static float getFloat(String s) {
		try {
			return Float.parseFloat(s);
		} catch (NumberFormatException | NullPointerException e) {
			return -1;
		}
	}

	public static short getShort(String s) {
		try {
			return Short.parseShort(s);
		} catch (NumberFormatException | NullPointerException e) {
			// -1 means "no durability" in Minecraft
			return -10;
		}
	}
}
