package com.thebubblenetwork.bubblebungee;

import com.thebubblenetwork.api.global.data.PlayerData;
import com.thebubblenetwork.api.global.player.BubblePlayer;
import com.thebubblenetwork.api.global.plugin.BubbleHub;
import com.thebubblenetwork.api.global.ranks.Rank;
import com.thebubblenetwork.bubblebungee.servermanager.ServerManager;
import net.md_5.bungee.api.plugin.Plugin;

import java.sql.SQLException;
import java.util.UUID;

/**
 * The Bubble Network 2016
 * BubbleBungee
 * 09/01/2016 {11:06}
 * Created January 2016
 */

public interface IBubbleBungee extends BubbleHub<Plugin> {
    ServerManager getManager();
    P getPlugin();
    PlayerData loadData(UUID load) throws SQLException, ClassNotFoundException;
    void updateRank(Rank r);
    void updatePlayer(BubblePlayer p);
    BubbleListener getListener();
    BungeePlugman getPlugman();
}
