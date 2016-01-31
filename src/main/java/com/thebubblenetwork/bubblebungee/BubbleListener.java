package com.thebubblenetwork.bubblebungee;

import com.thebubblenetwork.api.global.bubblepackets.PacketInfo;
import com.thebubblenetwork.api.global.bubblepackets.PacketListener;
import com.thebubblenetwork.api.global.bubblepackets.messaging.IPluginMessage;
import com.thebubblenetwork.api.global.bubblepackets.messaging.messages.handshake.AssignMessage;
import com.thebubblenetwork.api.global.bubblepackets.messaging.messages.handshake.PlayerCountUpdate;
import com.thebubblenetwork.api.global.bubblepackets.messaging.messages.handshake.RankDataUpdate;
import com.thebubblenetwork.api.global.bubblepackets.messaging.messages.request.PlayerDataRequest;
import com.thebubblenetwork.api.global.bubblepackets.messaging.messages.response.PlayerDataResponse;
import com.thebubblenetwork.api.global.player.BubblePlayer;
import com.thebubblenetwork.api.global.ranks.Rank;
import com.thebubblenetwork.api.global.type.ServerType;
import com.thebubblenetwork.bubblebungee.player.ProxiedBubblePlayer;
import com.thebubblenetwork.bubblebungee.servermanager.BubbleServer;
import de.mickare.xserver.net.XServer;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PermissionCheckEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.PreLoginEvent;
import net.md_5.bungee.api.event.ProxyPingEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The Bubble Network 2016
 * BubbleBungee
 * 24/01/2016 {09:46}
 * Created January 2016
 */
public class BubbleListener implements Listener,PacketListener{
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

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onAuthorization(PermissionCheckEvent e){
        CommandSender sender = e.getSender();
        if(sender instanceof ProxiedPlayer){
            ProxiedPlayer player = (ProxiedPlayer)sender;
            BubblePlayer<ProxiedPlayer> bubblePlayer = ProxiedBubblePlayer.getObject(player.getUniqueId());
            e.setHasPermission(bubblePlayer.isAuthorized(e.getPermission()));
        }
        else e.setHasPermission(true);
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
        else if(bungee.getPlugin().getProxy().getOnlineCount() > MAXLIMIT){
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

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPreJoin(PreLoginEvent e){
        e.setCancelled(false);
    }

    @EventHandler
    public void onPostJoin(PostLoginEvent e){
        ProxiedPlayer p = e.getPlayer();
        p.connect(getBungee().getManager().getServer("Lobby1").getInfo());
        p.setReconnectServer(getBungee().getManager().getServer("Lobby1").getInfo());
    }

    public void onMessage(PacketInfo info, IPluginMessage message) {
        if(message instanceof PlayerDataRequest){
            XServer server = info.getServer();
            PlayerDataRequest rq = (PlayerDataRequest) message;
            BubblePlayer<ProxiedPlayer> player = ProxiedBubblePlayer.getObject(rq.getUUID());
            if(player != null) {
                PlayerDataResponse response = new PlayerDataResponse(rq.getUUID(), player.getData().getRaw());
                sendPacketSafe(server,response);
                getBungee().logInfo("Sent player data request to " + server.getHost());
            }
            else getBungee().logSevere("Received request without player on bungee " + rq.getUUID() + " (" + server.getName() + ")");
        }
        else if(message instanceof AssignMessage){
            AssignMessage assignMessage = (AssignMessage)message;
            getBungee().getManager().create(info.getServer(),assignMessage.getWrapperType(),assignMessage.getId());
            getBungee().logInfo("Created server " + info.getServer().getHost() + " to " + assignMessage.getWrapperType().getName() + String.valueOf(assignMessage.getId()));
            for(Rank r:Rank.getRanks().values()){
                sendPacketSafe(info.getServer(),new RankDataUpdate(r.getName(),r.getData().getRaw()));
            }
        }
        else if(message instanceof PlayerCountUpdate){
            PlayerCountUpdate countUpdate = (PlayerCountUpdate)message;
            BubbleServer server = getBungee().getManager().getServer(info.getServer());
            if(server != null){
                server.setPlayercount(countUpdate.getOnline());
            }
            else getBungee().logSevere("Server not found when receiving incoming player update " + info.getServer().getHost());
        }
        else if(message instanceof PlayerDataResponse){
            PlayerDataResponse response = (PlayerDataResponse)message;
            BubblePlayer<ProxiedPlayer> player = ProxiedBubblePlayer.getObject(response.getUUID());
            player.setData(response.getData());
        }
        else{
            getBungee().logSevere("Could not accept packet - " + message.getType().getName());
        }
    }

    public void onConnect(PacketInfo info) {
        ServerType type = getBungee().getManager().getNeeded();
        int id = getBungee().getManager().getNewID(type);
        AssignMessage message = new AssignMessage(id,type);
        sendPacketSafe(info.getServer(),message);
        getBungee().logInfo("Sending assign message to " + info.getServer().getHost());
    }

    public void onDisconnect(PacketInfo info) {
        BubbleServer server = getBungee().getManager().getServer(info.getServer());
        if(server != null)server.remove();
        getBungee().logInfo("Server - " + info.getServer().getHost() + " - Disconnected");
    }

    public void sendPacketSafe(XServer server,IPluginMessage message){
        try {
            getBungee().getPacketHub().sendMessage(server,message);
        } catch (IOException e) {
            getBungee().logSevere(e.getMessage());
        }
    }
}
