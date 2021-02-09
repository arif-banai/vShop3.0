package me.arifbanai.vShop.utils;

/**
 * Represents the config file
 * Used by SnakeYAML to bind config data to instances of this class
 *
 * Fields must be typed exactly as in the .yml file
 * Getters and setters for every field is required
 *
 * @author Arif Banai
 * @version 1.0
 * @since 1.0 | Feb 4th, 2021
 */
public class Config {
    public boolean broadcastOffers;
    public boolean logTransactions;
    public boolean usingWebapp;
    public boolean usingRedis;
    public boolean usingSQLite;
    public Database db;

    public boolean isBroadcastOffers() {
        return broadcastOffers;
    }

    public void setBroadcastOffers(boolean broadcastOffers) {
        this.broadcastOffers = broadcastOffers;
    }

    public boolean isLogTransactions() {
        return logTransactions;
    }

    public void setLogTransactions(boolean logTransactions) {
        this.logTransactions = logTransactions;
    }

    public boolean isUsingWebapp() {
        return usingWebapp;
    }

    public void setUsingWebapp(boolean usingWebapp) {
        this.usingWebapp = usingWebapp;
    }

    public boolean isUsingRedis() {
        return usingRedis;
    }

    public void setUsingRedis(boolean usingRedis) {
        this.usingRedis = usingRedis;
    }

    public boolean isUsingSQLite() {
        return usingSQLite;
    }

    public void setUsingSQLite(boolean usingSQLite) {
        this.usingSQLite = usingSQLite;
    }

    public Database getDb() {
        return db;
    }

    public void setDb(Database db) {
        this.db = db;
    }
}
