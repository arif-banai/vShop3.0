package me.arifbanai.vShop.utils;

/**
 * Represents the database list in the config file
 * Used by SnakeYAML to bind config data to instances of this class
 *
 * Fields must be typed exactly as in the .yml file
 * Getters and setters for every field is required
 *
 * @author Arif Banai
 * @version 1.0
 * @since 1.0 | Feb 4th, 2021
 */
public class Database {
    String host;
    String port;
    String schema;
    String username;
    String password;
    String dialect;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDialect() {
        return dialect;
    }

    public void setDialect(String dialect) {
        this.dialect = dialect;
    }
}
