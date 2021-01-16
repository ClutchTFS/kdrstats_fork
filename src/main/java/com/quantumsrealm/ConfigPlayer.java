package com.quantumsrealm;

public class ConfigPlayer {
    public String id;
    public int kills;
    public int deaths;

    public ConfigPlayer(String id, int kills, int deaths) {
        this.id = id;
        this.kills = kills;
        this.deaths = deaths;
    }

    public ConfigPlayer(String id) {
        this.id = id;
    }
}