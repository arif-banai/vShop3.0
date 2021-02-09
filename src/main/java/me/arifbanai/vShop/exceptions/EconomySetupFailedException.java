package me.arifbanai.vShop.exceptions;

public class EconomySetupFailedException extends Exception {
    public EconomySetupFailedException() {
        super("Problem depending Vault. Disabling...");
    }
}
