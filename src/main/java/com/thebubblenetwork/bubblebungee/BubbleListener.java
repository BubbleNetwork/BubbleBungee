package com.thebubblenetwork.bubblebungee;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.PreLoginEvent;
import net.md_5.bungee.api.event.ProxyPingEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The Bubble Network 2016
 * BubbleBungee
 * 24/01/2016 {09:46}
 * Created January 2016
 */
public class BubbleListener implements Listener{
    private IBubbleBungee bungee;
    private static final String spacer = "     ";
    private String line1 = spacer + ChatColor.AQUA + ChatColor.BOLD.toString() + "BubbleNetwork" + spacer;
    private String line2 = ChatColor.BLUE + "Come and join the fun!";
    private List<String> sample = Arrays.asList(
            ChatColor.AQUA + ChatColor.UNDERLINE.toString() + "BubbleNetwork",
            "",
            ChatColor.BLUE + "Site " + ChatColor.GRAY + ChatColor.ITALIC.toString() + "thebubblenetwork.com"
    );
    private static final int MAXLIMIT = 5000;

    public BubbleListener(IBubbleBungee bungee){
        this.bungee = bungee;
    }

    protected IBubbleBungee getBungee(){
        return bungee;
    }

    @EventHandler
    public void onPing(ProxyPingEvent e){
        PendingConnection connection = e.getConnection();
        ServerPing ping = e.getResponse();
        ServerPing.Players players = ping.getPlayers();
        ping.setVersion(new ServerPing.Protocol("Please use 1.8",47));
        List<ServerPing.PlayerInfo> sample = new ArrayList<>();
        for(String s:this.sample){
            sample.add(new ServerPing.PlayerInfo(s,s));
        }
        String description = ChatColor.AQUA + "Welcome!";
        if(connection.getVersion() != 47){
            description = ChatColor.DARK_RED + "You need MC 1.8";
        }
        else if(bungee.getProxy().getOnlineCount() > MAXLIMIT && !isExempt(connection)){
            description += ChatColor.RED + "Donate to join when full";
            ping.setVersion(new ServerPing.Protocol("Server Full",-1));
        }
        else{
            players.setMax(MAXLIMIT);
            description += line2;
        }
        players.setSample(sample.toArray(new ServerPing.PlayerInfo[0]));
        ping.setDescription(line1 + "\n" + description);
        ping.setPlayers(players);
        e.setResponse(ping);
    }

    @EventHandler
    public void onPreJoin(PreLoginEvent e){
        PendingConnection pendingConnection = e.getConnection();
        e.setCancelled(false);
    }

    @EventHandler
    public void onPostJoin(PostLoginEvent e){
        ProxiedPlayer p = e.getPlayer();
        p.connect(getBungee().getManager().getServer("Lobby1").getInfo());
        p.setReconnectServer(getBungee().getManager().getServer("Lobby1").getInfo());
    }

    public boolean isExempt(PendingConnection connection){
        return false;
    }
}
