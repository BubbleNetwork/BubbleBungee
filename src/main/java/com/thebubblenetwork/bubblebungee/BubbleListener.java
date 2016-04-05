package com.thebubblenetwork.bubblebungee;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.thebubblenetwork.api.global.bubblepackets.PacketInfo;
import com.thebubblenetwork.api.global.bubblepackets.PacketListener;
import com.thebubblenetwork.api.global.bubblepackets.messaging.IPluginMessage;
import com.thebubblenetwork.api.global.bubblepackets.messaging.messages.handshake.AssignMessage;
import com.thebubblenetwork.api.global.bubblepackets.messaging.messages.handshake.JoinableUpdate;
import com.thebubblenetwork.api.global.bubblepackets.messaging.messages.handshake.PlayerCountUpdate;
import com.thebubblenetwork.api.global.bubblepackets.messaging.messages.handshake.RankDataUpdate;
import com.thebubblenetwork.api.global.bubblepackets.messaging.messages.request.*;
import com.thebubblenetwork.api.global.bubblepackets.messaging.messages.response.AntiCheatViolationMessage;
import com.thebubblenetwork.api.global.bubblepackets.messaging.messages.response.PlayerDataResponse;
import com.thebubblenetwork.api.global.bubblepackets.messaging.messages.response.ServerListResponse;
import com.thebubblenetwork.api.global.data.InvalidBaseException;
import com.thebubblenetwork.api.global.data.PlayerData;
import com.thebubblenetwork.api.global.ranks.Rank;
import com.thebubblenetwork.api.global.type.ServerType;
import com.thebubblenetwork.bubblebungee.party.Party;
import com.thebubblenetwork.bubblebungee.player.ProxiedBubblePlayer;
import com.thebubblenetwork.bubblebungee.servermanager.BubbleServer;
import de.mickare.xserver.net.XServer;
import net.md_5.bungee.api.*;
import net.md_5.bungee.api.chat.*;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.*;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

import java.io.IOException;
import java.net.ConnectException;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

/**
 * The Bubble Network 2016
 * BubbleBungee
 * 24/01/2016 {09:46}
 * Created January 2016
 */
public class BubbleListener implements Listener, PacketListener, ReconnectHandler {
    public static final String BANMSG = ChatColor.RED + "You have been {0} from BubbleNetwork\n\nReason: " + ChatColor.WHITE + "{1}\n" + ChatColor.RED + "Expires: " + ChatColor.WHITE + "{2}\n" + ChatColor.RED + "{0} By: " + ChatColor.WHITE + "{3}\n\n" + ChatColor.RED + "You may appeal at thebubblenetwork.com";

    private static final String spacer = "\n";
    private static final int MAXLIMIT = 5000;

    public static char getLastColor(String s) {
        for (int i = s.length() - 2; i >= 0; i--) {
            if (s.charAt(i) == ChatColor.COLOR_CHAR) {
                char c = s.charAt(i + 1);
                de.mickare.xserver.util.ChatColor color = de.mickare.xserver.util.ChatColor.getByChar(c);
                if (color.isColor()) {
                    return c;
                }
            }
        }
        return de.mickare.xserver.util.ChatColor.RESET.getChar();
    }

    public final DateFormat format = new SimpleDateFormat("hh:mm:ss");
    private final String errormsg = ChatColor.RED + "An internal error has occurred please report this to @ExtendObject";
    private Map<UUID, Runnable> toExecute = new HashMap<>();
    private BubbleBungee bungee;
    private String line1 = ChatColor.AQUA + ChatColor.BOLD.toString() + "BubbleNetwork" + spacer;
    private String line2 = ChatColor.BLUE + " Come and join the fun!";
    private List<String> sample = Arrays.asList(ChatColor.AQUA + ChatColor.UNDERLINE.toString() + "BubbleNetwork", "", ChatColor.BLUE + "Site " + ChatColor.GRAY + ChatColor.ITALIC.toString() + "thebubblenetwork.com",ChatColor.BLUE + "TeamSpeak " + ChatColor.GRAY + ChatColor.ITALIC.toString() + "ts.thebubblenetwork.com","",ChatColor.BLUE + "Follow us on twitter " + ChatColor.GRAY + "@bubblenetworkmc");
    private Map<UUID, ProxiedBubblePlayer> prequeed = new HashMap<>();

    public BubbleListener(BubbleBungee bungee) {
        this.bungee = bungee;
    }

    protected BubbleBungee getBungee() {
        return bungee;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onAuthorization(PermissionCheckEvent e) {
        CommandSender sender = e.getSender();
        if (sender instanceof ProxiedPlayer) {
            ProxiedPlayer player = (ProxiedPlayer) sender;
            ProxiedBubblePlayer bubblePlayer = ProxiedBubblePlayer.getObject(player.getUniqueId());
            e.setHasPermission(bubblePlayer.isAuthorized(e.getPermission()));
        } else {
            e.setHasPermission(true);
        }
    }

    @EventHandler
    public void onPlayerChat(ChatEvent e) {
        if (e.getSender() instanceof ProxiedPlayer && !e.getMessage().startsWith("/")) {
            ProxiedPlayer proxiedPlayer = (ProxiedPlayer) e.getSender();
            ProxiedBubblePlayer proxiedBubblePlayer = ProxiedBubblePlayer.getObject(proxiedPlayer.getUniqueId());

            Rank rank = proxiedBubblePlayer.getRank();
            BaseComponent[] prefix = TextComponent.fromLegacyText(rank.getPrefix());

            withHover(prefix, new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(rank.getPrefix() + "\n\nName: " + ChatColor.GRAY + rank.getName() + (rank.isAuthorized("donator") ? "\n" + ChatColor.GOLD + "Donator" : "") + (rank.isAuthorized("staff") ? "\n" + ChatColor.RED + "Staff" : "") + (rank.isAuthorized("owner") ? "\n" + ChatColor.DARK_RED + "Owner" : "") + (rank.isDefault() ? "\n" + ChatColor.GRAY + "Default Rank" : ""))));

            if (rank.isAuthorized("donator")) {
                try {
                    String s = rank.getData().getString("donation-link");
                    withClick(prefix, new ClickEvent(ClickEvent.Action.OPEN_URL, s));
                } catch (InvalidBaseException ex) {
                }
            }

            BaseComponent[] suffix = TextComponent.fromLegacyText(rank.getSuffix());
            BaseComponent[] message = TextComponent.fromLegacyText(e.getMessage());

            String ranks = rank.getName();
            for (Rank r : proxiedBubblePlayer.getSubRanks()) {
                ranks += ", " + r.getName();
            }

            HoverEvent clickhover = new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText("Name: " + ChatColor.GRAY + proxiedBubblePlayer.getName() +
                    "\nSent at " + ChatColor.GRAY + format.format(new Date()) +
                    "\nRank: " + ChatColor.GRAY + ranks));
            withHover(message, clickhover);

            TextComponent name = new TextComponent(proxiedBubblePlayer.getNickName());
            name.setColor(ChatColor.getByChar(getLastColor(TextComponent.toLegacyText(prefix))));
            name.setHoverEvent(clickhover);
            name.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/msg " + proxiedBubblePlayer.getNickName() + " "));

            TextComponent space = new TextComponent(" ");

            BaseComponent[] fullmsg = new ImmutableList.Builder<BaseComponent>().add(prefix).add(space).add(name).add(suffix).add(space).add(message).build().toArray(new BaseComponent[0]);

            for (ProxiedPlayer target : proxiedPlayer.getServer().getInfo().getPlayers()) {
                target.sendMessage(ChatMessageType.CHAT, fullmsg);
            }
            e.setCancelled(true);
        }
    }

    private void withHover(BaseComponent[] components, HoverEvent event) {
        for (BaseComponent component : components) {
            component.setHoverEvent(event);
        }
    }

    private void withClick(BaseComponent[] components, ClickEvent event) {
        for (BaseComponent component : components) {
            component.setClickEvent(event);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerPreJoinEarly(LoginEvent e){
        PendingConnection connection = e.getConnection();
        if(!connection.isOnlineMode()) {
            e.setCancelled(true);
            e.setCancelReason("Your account is not authenticated");
        }
        else if(connection.getUniqueId() == null || connection.getName() == null){
            e.setCancelled(true);
            e.setCancelReason("Error in authentication");
        }
        else if(!e.isCancelled()) {
            PlayerData data;
            try {
                data = getBungee().loadData(connection.getUniqueId());
            } catch (Exception e1) {
                e.setCancelled(true);
                e.setCancelReason(ChatColor.RED + "Woops! " + e1.getClass().getName() + ": " + e1.getMessage());
                getBungee().getLogger().log(Level.WARNING, "Failed to load PlayerData: " + connection.getName());
                return;
            }
            ProxiedBubblePlayer player = new ProxiedBubblePlayer(connection.getUniqueId(), data);
            player.setName(connection.getName());
            if(player.isBanned()){
                e.setCancelled(true);
                String bantimer;
                if(player.getUnbanDate() != null) {
                    bantimer = "";
                    Date unbanby = new Date(player.getUnbanDate().getTime() - System.currentTimeMillis());
                    if(unbanby.getYear() != 0){
                        bantimer += unbanby.getYear() + " " + "year" + (unbanby.getYear() != 1 ? "s" : "") + " ";
                    }
                    if(unbanby.getMonth() != 0){
                        bantimer += unbanby.getMonth() + " " + "month" + (unbanby.getMonth() != 1 ? "s" : "") + " ";
                    }
                    if(unbanby.getYear() != 0){
                        bantimer += unbanby.getDay() + " " + "day" + (unbanby.getDay() != 1 ? "s" : "") + " ";
                    }
                    if(unbanby.getMinutes() != 0){
                        bantimer += unbanby.getYear() + " " + "minute" + (unbanby.getMinutes() != 1 ? "s" : "") + " ";
                    }
                    if(unbanby.getYear() != 0){
                        bantimer += unbanby.getSeconds() + " " + "second" + (unbanby.getSeconds() != 1 ? "s" : "") + " ";
                    }
                }
                else bantimer = "never";
                e.setCancelReason(String.format(BANMSG,"Banned",player.getBanReason(),bantimer,player.getBannedBy()));
            }
            else if (getBungee().isLockdown() && !player.isAuthorized("lockdown.bypass")) {
                e.setCancelled(true);
                e.setCancelReason(getBungee().getLockdownmsg());
            }
            else if(!e.isCancelled()) {
                prequeed.put(connection.getUniqueId(), player);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerPreJoinLate(LoginEvent e){
        ProxiedBubblePlayer player = prequeed.remove(e.getConnection().getUniqueId());
        if(!e.isCancelled()){
            ProxiedBubblePlayer.getPlayerObjectMap().put(player.getUUID(), player);
        }
        else e.setCancelReason(ChatColor.BLUE + ChatColor.BOLD.toString() + "[" + ChatColor.AQUA + "BubbleNetwork" + ChatColor.BLUE + ChatColor.BOLD.toString() + "]\n\n" + ChatColor.RESET + e.getCancelReason());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onQuit(PlayerDisconnectEvent e) {
        ProxiedBubblePlayer player = (ProxiedBubblePlayer) ProxiedBubblePlayer.getPlayerObjectMap().remove(e.getPlayer().getUniqueId());
        player.setParty(null);
        player.save();
    }

    @EventHandler
    public void onPing(ProxyPingEvent e) {
        PendingConnection connection = e.getConnection();
        ServerPing ping = e.getResponse();
        ServerPing.Players players = ping.getPlayers();
        ping.setVersion(new ServerPing.Protocol("Please use 1.8", 47));
        List<ServerPing.PlayerInfo> sample = new ArrayList<>();
        for (String s : this.sample) {
            sample.add(new ServerPing.PlayerInfo(s, s));
        }
        String description = ChatColor.AQUA + "Welcome!";
        if (connection.getVersion() != 47 && connection.getVersion() < 107) {
            description = ChatColor.DARK_RED + "You are not on the correct version";
            ping.setVersion(new ServerPing.Protocol("Bubble 1.8.X/1.9.X",-1));
        } else if(!connection.isOnlineMode()) {
            description = ChatColor.DARK_RED + "";
        } else if (bungee.getPlugin().getProxy().getOnlineCount() > MAXLIMIT) {
            description += ChatColor.RED + "Donate to join when full";
            ping.setVersion(new ServerPing.Protocol("Server Full", -1));
        } else {
            players.setMax(MAXLIMIT);
            description += line2;
        }
        players.setSample(sample.toArray(new ServerPing.PlayerInfo[0]));
        ping.setDescription(line1 + description);
        ping.setPlayers(players);
        e.setResponse(ping);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPreServerChange(ServerConnectEvent e) {
        BubbleServer server = getBungee().getManager().getServer(e.getTarget().getName());
        if(server != null){
            e.setTarget(server.getInfo());
        }
        else e.setCancelled(true);
    }


    @EventHandler
    public void onServerKick(ServerKickEvent e){
        BubbleServer lobbyServer = getLobby(e.getPlayer());
        if(lobbyServer != null) {
            e.setCancelServer(lobbyServer.getInfo());
        }
    }

    public BubbleServer getLobby(ProxiedPlayer player){
        //Player login
        ServerType LOBBY = ServerType.getType("Lobby");
        if (LOBBY == null) {
            throw new IllegalArgumentException("Lobby type doesn't exist");
        }
        BubbleServer server = getBungee().getManager().getAvailble(LOBBY,1, true, true);
        if (server == null) {
            server = getBungee().getManager().getAvailble(LOBBY,1, true, false);
        }
        return server;
    }


    public void onMessage(PacketInfo info, IPluginMessage message) {
        if (message instanceof PlayerDataRequest) {
            XServer server = info.getServer();
            PlayerDataRequest rq = (PlayerDataRequest) message;
            ProxiedBubblePlayer player = ProxiedBubblePlayer.getObject(rq.getName());
            if (player != null) {
                PlayerDataResponse response = new PlayerDataResponse(rq.getName(), player.getData().getRaw());
                sendPacketSafe(server, response);
                getBungee().logInfo("Sent player data request to " + server.getHost());
            } else {
                getBungee().logSevere("Received request without player on bungee " + rq.getName() + " (" + server.getName() + ")");
            }
        } else if (message instanceof AssignMessage) {
            AssignMessage assignMessage = (AssignMessage) message;
            getBungee().getManager().create(info.getServer(), assignMessage.getWrapperType(), assignMessage.getId());
            getBungee().logInfo("Created server " + info.getServer().getHost() + " to " + assignMessage.getWrapperType().getName() + String.valueOf(assignMessage.getId()));
            for (Rank r : Rank.getRanks()) {
                sendPacketSafe(info.getServer(), new RankDataUpdate(r.getName(), r.getData().getRaw()));
            }
        } else if (message instanceof PlayerCountUpdate) {
            PlayerCountUpdate countUpdate = (PlayerCountUpdate) message;
            BubbleServer server = getBungee().getManager().getServer(info.getServer());
            if (server != null) {
                server.setPlayercount(countUpdate.getOnline());
            } else {
                getBungee().logSevere("Server not found when receiving incoming player update " + info.getServer().getHost());
            }
        } else if (message instanceof PlayerDataResponse) {
            PlayerDataResponse response = (PlayerDataResponse) message;
            ProxiedBubblePlayer player = ProxiedBubblePlayer.getObject(response.getName());
            if (player != null) {
                player.setData(response.getData());
            } else {
                getBungee().logSevere("Received response without player on bungee " + response.getName() + " (" + info.getServer().getName() + ")");
            }
        } else if (message instanceof ServerShutdownRequest) {
            BubbleServer server = getBungee().getManager().getServer(info.getServer());
            if (server != null) {
                server.remove();
            } else {
                getBungee().logSevere("Failed to shutdown server " + info.getServer().getName());
            }
        } else if (message instanceof JoinableUpdate) {
            JoinableUpdate update = (JoinableUpdate) message;
            BubbleServer server = getBungee().getManager().getServer(info.getServer());
            if (server != null) {
                server.setJoinable(update.isJoinable());
            } else {
                getBungee().logSevere("Server not found when receiving incoming joinable update " + info.getServer().getName());
            }
        } else if (message instanceof PlayerMoveRequest) {
            PlayerMoveRequest request = (PlayerMoveRequest) message;
            ProxiedPlayer player = getBungee().getPlugin().getProxy().getPlayer(request.getName());
            if (player != null) {
                BubbleServer server = getBungee().getManager().getServer(request.getTo());
                if (server != null) {
                    player.connect(server.getInfo());
                    player.sendMessage(new ComponentBuilder("Sending you to ").color(ChatColor.RED).append(server.getType().getName() + "-" + server.getId()).color(ChatColor.YELLOW).event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(ChatColor.GRAY + server.getType().getName() + "\nID: " + server.getId() + "\nOnline: " + server.getPlayercount() + "/" + server.getMaxplayercount()))).create());
                } else {
                    getBungee().logSevere("Could not find bubbleserver " + request.getTo());
                }
            } else {
                getBungee().getLogger().log(Level.INFO,"Could not find player " + request.getName());
            }
        } else if (message instanceof PlayerMoveTypeRequest) {
            PlayerMoveTypeRequest request = (PlayerMoveTypeRequest) message;
            ProxiedPlayer player = getBungee().getPlugin().getProxy().getPlayer(request.getName());
            if (request.getServerType() != null && player != null) {
                ProxiedBubblePlayer bubblePlayer = ProxiedBubblePlayer.getObject(player.getUniqueId());
                int i = 1;
                Set<UUID> send = null;
                if(request.getServerType() != ServerType.getType("Lobby")){
                    Party p = bubblePlayer.getParty();
                    if(p != null && p.isMember(player)){
                        if(p.isLeader(player)){
                            i = p.getMembers().size();
                            send = p.getMembers();
                            send.remove(p.getLeader());
                        }
                        else {
                            player.sendMessage(new ComponentBuilder("You need to be party leader to travel to another server").color(ChatColor.BLUE).create());
                            return;
                        }
                    }
                }
                BubbleServer server = getBungee().getManager().getAvailble(request.getServerType(),i, true, false);
                if (server == null) {
                    server = getBungee().getManager().getAvailble(request.getServerType(),i, true, false);
                }

                if (server == null) {
                    if (!getBungee().getManager().getUnassigned().isEmpty()) {
                        PacketInfo serverinfo = Iterables.get(getBungee().getManager().getUnassigned(), 0);
                        getBungee().getManager().getUnassigned().remove(serverinfo);
                        sendPacketSafe(serverinfo.getServer(), new AssignMessage(getBungee().getManager().getNewID(request.getServerType()), request.getServerType()));
                        player.sendMessage(ChatMessageType.CHAT, new ComponentBuilder("Please wait a few seconds, a server is being created").color(ChatColor.BLUE).event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(ChatColor.BLUE + "This will take about 5 seconds"))).create());
                    } else {
                        player.sendMessage(new ComponentBuilder("No servers open at the moment").color(ChatColor.BLUE).event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(ChatColor.GOLD + "A server will be open soon"))).create());
                        getBungee().logSevere("Couldn't find any server types for " + request.getServerType().getName());
                    }
                } else {
                    if(send != null) {
                        for (UUID u : send) {
                            ProxiedPlayer other = getBungee().getPlugin().getProxy().getPlayer(u);
                            other.sendMessage(new ComponentBuilder("Connecting you to ").color(ChatColor.BLUE).append(server.getType().getName() + "-" + server.getId()).color(ChatColor.AQUA).event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(ChatColor.BLUE + server.getType().getName() + "\nID: " + server.getId() + "\nOnline: " + server.getPlayercount() + "/" + server.getMaxplayercount()))).create());
                            other.connect(server.getInfo());
                        }
                    }
                    player.sendMessage(new ComponentBuilder("Connecting you to ").color(ChatColor.BLUE).append(server.getType().getName() + "-" + server.getId()).color(ChatColor.AQUA).event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(ChatColor.BLUE + server.getType().getName() + "\nID: " + server.getId() + "\nOnline: " + server.getPlayercount() + "/" + server.getMaxplayercount()))).create());
                    player.connect(server.getInfo());
                    server.setPlayercount(server.getPlayercount() + i);
                }
            } else {
                getBungee().logSevere("Could proccess player request " + request.getName());
            }
        } else if (message instanceof ServerListRequest) {
            ServerListRequest request = (ServerListRequest) message;
            if (request.getServertype() != null) {
                Map<String, String> datamap = new HashMap<>();
                for (BubbleServer server : getBungee().getManager().getServers()) {
                    if (request.getServertype() == server.getType()) {
                        datamap.put(String.valueOf(server.getId()), String.valueOf(server.getPlayercount()));
                    }
                }
                sendPacketSafe(info.getServer(), new ServerListResponse(datamap, request.getServertype()));
            } else {
                getBungee().logSevere("Could not find servertype for request");
            }
        } else if(message instanceof AntiCheatViolationMessage){
            AntiCheatViolationMessage antiCheatViolationMessage = (AntiCheatViolationMessage)message;

        } else {
            getBungee().logSevere("Could not accept packet - " + message.getType().getName());
        }
    }

    public void onConnect(PacketInfo info) {
        ServerType type = null;
        try {
            type = getBungee().getManager().getNeeded();
        } catch (Throwable e) {
            getBungee().getManager().getUnassigned().add(info);
            getBungee().getPlugin().getLogger().log(Level.WARNING, "Could not find server, adding to unassigned", e);
        }
        int id = getBungee().getManager().getNewID(type);
        AssignMessage message = new AssignMessage(id, type);
        sendPacketSafe(info.getServer(), message);
        getBungee().logInfo("Sending assign message to " + info.getServer().getHost());
    }

    public void onDisconnect(PacketInfo info) {
        getBungee().getManager().removeUnassigned(info.getServer());
        BubbleServer server = getBungee().getManager().getServer(info.getServer());
        if (server != null) {
            server.remove();
        } else {
            //Probably already handled from the message
        }
    }

    public void sendPacketSafe(final XServer server, final IPluginMessage message) {
        final UUID u = UUID.randomUUID();
        Runnable r = new Runnable() {
            public void run() {
                toExecute.remove(u);
                try {
                    getBungee().getPacketHub().sendMessage(server, message);
                } catch (IOException e) {
                    getBungee().logSevere(e.getMessage());
                }
            }
        };
        toExecute.put(u, r);
        ProxyServer.getInstance().getScheduler().schedule(getBungee().getPlugin(), r, 1L, TimeUnit.MILLISECONDS);
    }

    public ServerInfo getServer(ProxiedPlayer proxiedPlayer) {
        getBungee().getLogger().log(Level.INFO, "Finding a Lobby for " + proxiedPlayer.getName());
        BubbleServer server = getLobby(proxiedPlayer);
        if(server != null){
            getBungee().getLogger().log(Level.INFO, "Sending {0} to {1}",new Object[]{proxiedPlayer.getName(), server.getName()});
            return server.getInfo();
        }

        proxiedPlayer.disconnect(TextComponent.fromLegacyText(ChatColor.RED + "Could not find a Lobby server for you"));
        throw new IllegalArgumentException("No Server found");
    }

    public void setServer(ProxiedPlayer proxiedPlayer) {
    }

    public void save() {

    }

    public void close() {

    }
}
