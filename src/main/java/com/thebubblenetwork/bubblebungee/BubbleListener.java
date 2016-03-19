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
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The Bubble Network 2016
 * BubbleBungee
 * 24/01/2016 {09:46}
 * Created January 2016
 */
public class BubbleListener implements Listener, PacketListener {
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
    private List<String> sample = Arrays.asList(ChatColor.AQUA + ChatColor.UNDERLINE.toString() + "BubbleNetwork", "", ChatColor.BLUE + "Site " + ChatColor.GRAY + ChatColor.ITALIC.toString() + "thebubblenetwork.com");
    private List<String> requestqueue = new ArrayList<>();

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
    public void onPlayerJoin(PostLoginEvent e) {
        ProxiedPlayer connection = e.getPlayer();
        try {
            PlayerData data = getBungee().loadData(connection.getUniqueId());
            ProxiedBubblePlayer player = new ProxiedBubblePlayer(connection.getUniqueId(), data);
            player.setName(connection.getName());
            ProxiedBubblePlayer.getPlayerObjectMap().put(connection.getUniqueId(), player);
            getBungee().logInfo("Loaded data: " + connection.getName());
        } catch (SQLException | ClassNotFoundException e1) {
            getBungee().logSevere(e1.getMessage());
        }
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
        if (connection.getVersion() != 47) {
            description = ChatColor.DARK_RED + "You need MC 1.8";
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
        ProxiedPlayer player = e.getPlayer();
        BubbleServer server = getBungee().getManager().getServer(e.getTarget().getName());
        if (server != null) {
            e.setTarget(server.getInfo());
            if (player.getServer() != null) {
                BubbleServer from = getBungee().getManager().getServer(player.getServer().getInfo().getName());
                if (from != null) {
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
                        if (System.currentTimeMillis() > original + max) {
                            getBungee().logSevere("Server timed out? " + server.getName());
                            e.setCancelled(true);
                            return;
                        }
                    }
                    getBungee().logInfo("Server change authorized for " + player.getName());
                }
            } else {
                //LOGIN ?
                ServerType LOBBY = ServerType.getType("Lobby");
                if (LOBBY == null) {
                    throw new IllegalArgumentException("Lobby type doesn't exist");
                }
                server = getBungee().getManager().getAvailble(LOBBY,1, true, true);
                if (server == null) {
                    server = getBungee().getManager().getAvailble(LOBBY,1, true, false);
                    if (server == null) {
                        e.setCancelled(true);
                        e.getPlayer().disconnect(TextComponent.fromLegacyText(ChatColor.RED + "No lobbies open at the moment"));
                        return;
                    }
                }
                player.setReconnectServer(server.getInfo());
                e.setTarget(server.getInfo());
            }
        } else {
            getBungee().logSevere(player.getName() + " tried to connect to an unregistered server");
            e.setCancelled(true);
            player.sendMessage(TextComponent.fromLegacyText(errormsg));
        }
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
            requestqueue.remove(response.getName().toLowerCase());
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
}
