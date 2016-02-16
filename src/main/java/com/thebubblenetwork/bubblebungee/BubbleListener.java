package com.thebubblenetwork.bubblebungee;

import com.google.common.collect.ImmutableList;
import com.thebubblenetwork.api.global.bubblepackets.PacketInfo;
import com.thebubblenetwork.api.global.bubblepackets.PacketListener;
import com.thebubblenetwork.api.global.bubblepackets.messaging.IPluginMessage;
import com.thebubblenetwork.api.global.bubblepackets.messaging.messages.handshake.AssignMessage;
import com.thebubblenetwork.api.global.bubblepackets.messaging.messages.handshake.JoinableUpdate;
import com.thebubblenetwork.api.global.bubblepackets.messaging.messages.handshake.PlayerCountUpdate;
import com.thebubblenetwork.api.global.bubblepackets.messaging.messages.handshake.RankDataUpdate;
import com.thebubblenetwork.api.global.bubblepackets.messaging.messages.request.PlayerDataRequest;
import com.thebubblenetwork.api.global.bubblepackets.messaging.messages.request.ServerShutdownRequest;
import com.thebubblenetwork.api.global.bubblepackets.messaging.messages.response.PlayerDataResponse;
import com.thebubblenetwork.api.global.data.InvalidBaseException;
import com.thebubblenetwork.api.global.data.PlayerData;
import com.thebubblenetwork.api.global.player.BubblePlayer;
import com.thebubblenetwork.api.global.plugin.BubbleHubObject;
import com.thebubblenetwork.api.global.ranks.Rank;
import com.thebubblenetwork.api.global.type.ServerType;
import com.thebubblenetwork.api.global.type.ServerTypeObject;
import com.thebubblenetwork.bubblebungee.player.ProxiedBubblePlayer;
import com.thebubblenetwork.bubblebungee.servermanager.BubbleServer;
import de.mickare.xserver.net.XServer;
import net.md_5.bungee.api.*;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.*;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

import java.io.IOException;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * The Bubble Network 2016
 * BubbleBungee
 * 24/01/2016 {09:46}
 * Created January 2016
 */
public class BubbleListener implements Listener,PacketListener{
    private IBubbleBungee bungee;
    private static final String spacer = "\n";
    private String line1 = ChatColor.AQUA + ChatColor.BOLD.toString() + "BubbleNetwork" + spacer;
    private String line2 = ChatColor.BLUE + " Come and join the fun!";
    private List<String> sample = Arrays.asList(
            ChatColor.AQUA + ChatColor.UNDERLINE.toString() + "BubbleNetwork",
            "",
            ChatColor.BLUE + "Site " + ChatColor.GRAY + ChatColor.ITALIC.toString() + "thebubblenetwork.com"
    );
    private final String errormsg = ChatColor.RED + "An internal error has occurred please report this to @ExtendObject";
    private final DateFormat format = new SimpleDateFormat("hh:mm:ss");

    private static final int MAXLIMIT = 5000;

    private List<String> requestqueue = new ArrayList<>();

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
            ProxiedBubblePlayer bubblePlayer = ProxiedBubblePlayer.getObject(player.getUniqueId());
            e.setHasPermission(bubblePlayer.isAuthorized(e.getPermission()));
        }
        else e.setHasPermission(true);
    }

    @EventHandler
    public void onPlayerChat(ChatEvent e){
        if(e.getSender() instanceof ProxiedPlayer && !e.getMessage().startsWith("/")){
            ProxiedPlayer proxiedPlayer = (ProxiedPlayer)e.getSender();
            ProxiedBubblePlayer proxiedBubblePlayer = ProxiedBubblePlayer.getObject(proxiedPlayer.getUniqueId());

            Rank rank = proxiedBubblePlayer.getRank();
            BaseComponent[] prefix = TextComponent.fromLegacyText(rank.getPrefix());

            withHover(prefix,new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                    TextComponent.fromLegacyText(
                            rank.getPrefix() + "\n\nName: " + ChatColor.GRAY + rank.getName()
                            + (rank.isAuthorized("donator") ? "\n" + ChatColor.GOLD + "Donator" : "")
                            + (rank.isAuthorized("staff") ? "\n" + ChatColor.RED + "Staff" : "")
                            + (rank.isAuthorized("owner") ? "\n" + ChatColor.DARK_RED + "Owner" : "")
                            + (rank.isDefault() ? "\n" + ChatColor.GRAY + "Default Rank" : "")
                    )));

            if(rank.isAuthorized("donator")){
                try{
                    String s = rank.getData().getString("donation-link");
                    withClick(prefix,new ClickEvent(ClickEvent.Action.OPEN_URL,s));
                }
                catch (InvalidBaseException ex){
                }
            }

            BaseComponent[] suffix = TextComponent.fromLegacyText(rank.getSuffix());
            BaseComponent[] message = TextComponent.fromLegacyText(e.getMessage());

            String ranks = rank.getName();
            for(Rank r:proxiedBubblePlayer.getSubRanks())ranks += ", " + r.getName();

            HoverEvent clickhover = new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                    TextComponent.fromLegacyText("Name: " + ChatColor.GRAY + proxiedBubblePlayer.getName() + "\nSent at " + ChatColor.GRAY + format.format(new Date()) +"\nRank: " + ChatColor.GRAY + ranks));
            withHover(message,clickhover);

            TextComponent name = new TextComponent(proxiedBubblePlayer.getNickName());
            name.setHoverEvent(clickhover);
            name.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND,"/msg " + proxiedPlayer.getName() + " "));

            BaseComponent[] fullmsg = new ImmutableList.Builder<BaseComponent>().add(prefix).add(name).add(suffix).add(message).build().toArray(new BaseComponent[0]);

            for(ProxiedPlayer target:proxiedPlayer.getServer().getInfo().getPlayers()){
                target.sendMessage(ChatMessageType.CHAT,fullmsg);
            }
            e.setCancelled(true);
        }
    }

    private void withHover(BaseComponent[] components,HoverEvent event){
        for(BaseComponent component:components)component.setHoverEvent(event);
    }

    private void withClick(BaseComponent[] components,ClickEvent event){
        for(BaseComponent component:components)component.setClickEvent(event);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPreJoin(PreLoginEvent e){
        /*
        if(!e.getConnection().isOnlineMode()){
            e.setCancelReason(ChatColor.RED + "Your account must be authenticated");
            e.setCancelled(true);
        }*/
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(PostLoginEvent e){
        ProxiedPlayer connection = e.getPlayer();
        try {
            PlayerData data = getBungee().loadData(connection.getUniqueId());
            ProxiedBubblePlayer player = new ProxiedBubblePlayer(connection.getUniqueId(),data);
            player.setName(connection.getName());
            ProxiedBubblePlayer.getPlayerObjectMap().put(connection.getUniqueId(),player);
            getBungee().logInfo("Loaded data: " + connection.getName());
        } catch (SQLException|ClassNotFoundException e1) {
            getBungee().logSevere(e1.getMessage());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onQuit(PlayerDisconnectEvent e){
        ProxiedBubblePlayer player = (ProxiedBubblePlayer) ProxiedBubblePlayer.getPlayerObjectMap().remove(e.getPlayer().getUniqueId());
        player.save();
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
        /*else if(!connection.isOnlineMode()){
            description = ChatColor.DARK_RED + "You must be authenticated";
            ping.setVersion(new ServerPing.Protocol("Authentication required",0));
        }*/
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

    @EventHandler
    public void onPostJoin(PostLoginEvent e){
        ProxiedPlayer p = e.getPlayer();
        BubbleServer server = getBungee().getManager().getAvailble(ServerTypeObject.getType("Lobby"));
        p.setReconnectServer(server.getInfo());
        p.connect(server.getInfo());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPreServerChange(ServerConnectEvent e){
        ProxiedPlayer player = e.getPlayer();
        BubbleServer server = getBungee().getManager().getServer(e.getTarget().getName());
        if(server != null) {
            e.setTarget(server.getInfo());
            if(player.getServer() != null) {
                BubbleServer from = getBungee().getManager().getServer(player.getServer().getInfo().getName());
                if(from != null) {
                    getBungee().logInfo("Requesting playerdata for " + player.getName());
                    sendPacketSafe(from.getServer(), new PlayerDataRequest(player.getName()));
                    String name = player.getName().toLowerCase();
                    requestqueue.add(name);
                    long max = TimeUnit.SECONDS.toMillis(10);
                    long original = System.currentTimeMillis();
                    while (requestqueue.contains(name)) {
                        try {
                            Thread.sleep(100L);
                        } catch (InterruptedException e1) {
                        }
                        if(System.currentTimeMillis() > original + max){
                            e.setCancelled(true);
                            getBungee().logSevere("Server timed out? " + server.getName());
                            break;
                        }
                    }
                    getBungee().logInfo("Server change authorized for " + player.getName());
                }
            }
        }
        else{
            getBungee().logSevere(player.getName() + " tried to connect to an unregistered server");
            e.setCancelled(true);
            player.sendMessage(TextComponent.fromLegacyText(errormsg));
        }
    }

    public void onMessage(PacketInfo info, IPluginMessage message) {
        if(message instanceof PlayerDataRequest){
            XServer server = info.getServer();
            PlayerDataRequest rq = (PlayerDataRequest) message;
            ProxiedBubblePlayer player = ProxiedBubblePlayer.getObject(rq.getName());
            if(player != null) {
                PlayerDataResponse response = new PlayerDataResponse(rq.getName(), player.getData().getRaw());
                sendPacketSafe(server,response);
                getBungee().logInfo("Sent player data request to " + server.getHost());
            }
            else getBungee().logSevere("Received request without player on bungee " + rq.getName() + " (" + server.getName() + ")");
        }
        else if(message instanceof AssignMessage){
            AssignMessage assignMessage = (AssignMessage)message;
            getBungee().getManager().create(info.getServer(),assignMessage.getWrapperType(),assignMessage.getId());
            getBungee().logInfo("Created server " + info.getServer().getHost() + " to " + assignMessage.getWrapperType().getName() + String.valueOf(assignMessage.getId()));
            for(Rank r:Rank.getRanks()){
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
            BubblePlayer<ProxiedPlayer> player = ProxiedBubblePlayer.getObject(response.getName());
            if(player != null) {
                player.setData(response.getData());
            }
            else getBungee().logSevere("Received response without player on bungee " + response.getName() + " (" + info.getServer().getName() +")");
            requestqueue.remove(response.getName().toLowerCase());
        }
        else if(message instanceof ServerShutdownRequest){
            BubbleServer server = getBungee().getManager().getServer(info.getServer());
            if(server != null){
                server.remove();
            }
            else getBungee().logSevere("Failed to shutdown server " + info.getServer().getName());
        }
        else if(message instanceof JoinableUpdate){
            JoinableUpdate update = (JoinableUpdate)message;
            BubbleServer server = getBungee().getManager().getServer(info.getServer());
            if(server != null){
                server.setJoinable(update.isJoinable());
            }
            else getBungee().logSevere("Server not found when receiving incoming joinable update " + info.getServer().getName());
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

    public void onDisconnect(PacketInfo info){
        getBungee().logInfo(info.getServer().getName() + " disconnect?");
    }

    public void sendPacketSafe(final XServer server,final IPluginMessage message){
        ((BubbleHubObject)getBungee()).runTaskLater(new Runnable(){
            @Override
            public void run() {
                try {
                    getBungee().getPacketHub().sendMessage(server,message);
                } catch (IOException e) {
                    getBungee().logSevere(e.getMessage());
                }
            }
        },1L, TimeUnit.MILLISECONDS);
    }
}
