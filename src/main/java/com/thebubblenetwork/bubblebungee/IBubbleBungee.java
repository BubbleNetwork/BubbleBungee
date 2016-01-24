package com.thebubblenetwork.bubblebungee;

import com.thebubblenetwork.bubblebungee.file.PropertiesFile;
import com.thebubblenetwork.bubblebungee.servermanager.ServerManager;
import com.thebubblenetwork.bubblebungee.sql.SQLConnection;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.PluginDescription;

import java.io.File;
import java.io.InputStream;
import java.util.logging.Logger;

/**
 * The Bubble Network 2016
 * BubbleBungee
 * 09/01/2016 {11:06}
 * Created January 2016
 */

public interface IBubbleBungee{
    String getVersion();
    Plugin getPlugin();
    ServerManager getManager();
    PluginDescription getDescription();
    ProxyServer getProxy();
    File getFile();
    Logger getLogger();
    File getDataFolder();
    InputStream getResourceAsStream(String name);
    SQLConnection getConnection();
    PropertiesFile getProperties();
    String getDatabaseName();
    String getDatabaseAddress();
    String getDatabasePort();
    String getDatabaseUser();
    String getDatabasePassword();
    void onDisable();
    void onEnable();
    void onLoad();
}
