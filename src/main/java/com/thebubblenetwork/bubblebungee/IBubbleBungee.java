package com.thebubblenetwork.bubblebungee;

import com.thebubblenetwork.api.global.data.PlayerData;
import com.thebubblenetwork.api.global.plugin.BubbleHub;
import com.thebubblenetwork.bubblebungee.servermanager.ServerManager;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.PluginDescription;

import java.io.File;
import java.sql.SQLException;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * The Bubble Network 2016
 * BubbleBungee
 * 09/01/2016 {11:06}
 * Created January 2016
 */

public interface IBubbleBungee extends BubbleHub<Plugin,ProxiedPlayer> {
    ServerManager getManager();
    P getPlugin();
    PlayerData loadData(UUID load) throws SQLException, ClassNotFoundException;
}
