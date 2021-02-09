package me.arifbanai.vShop.exceptions;

public class DataSourceSetupFailedException extends Exception{
    public DataSourceSetupFailedException() {
        super("Failed to initialize DataSourceManager.");
    }
}
