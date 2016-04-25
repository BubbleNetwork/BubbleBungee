package com.thebubblenetwork.bubblebungee.command.commands;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.thebubblenetwork.api.global.data.PlayerData;
import com.thebubblenetwork.api.global.sql.SQLConnection;
import com.thebubblenetwork.api.global.sql.SQLUtil;
import com.thebubblenetwork.bubblebungee.BubbleBungee;
import com.thebubblenetwork.bubblebungee.command.BaseCommand;
import com.thebubblenetwork.bubblebungee.command.CommandException;
import com.thebubblenetwork.bubblebungee.command.ICommand;
import com.thebubblenetwork.bubblebungee.command.SubCommand;
import com.thebubblenetwork.bubblebungee.player.ProxiedBubblePlayer;
import com.thebubblenetwork.bubblebungee.servermanager.BubbleServer;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class FriendCommand extends BaseCommand {
    private static BubbleBungee instance = BubbleBungee.getInstance();
    private static final String nowfriends = ChatColor.GOLD + "You are now friends with {0}", invitefriends = ChatColor.GOLD + "You sent a request to {0}", invitedfriends = "[Friend request from {0}]";

    private static void notSelf(ProxiedPlayer player1, ProxiedPlayer player2) {
        if (player1.getUniqueId() == player2.getUniqueId()) {
            throw new IllegalArgumentException("You may not befriend yourself");
        }
    }

    private static ProxiedPlayer notConsole(CommandSender sender) throws CommandException {
        if (!(sender instanceof ProxiedPlayer)) {
            throw new IllegalArgumentException("You must be a player to do this command");
        }
        return (ProxiedPlayer) sender;
    }

    private static ProxiedBubblePlayer isPlayer(ProxiedPlayer player) throws CommandException {
        ProxiedBubblePlayer bubblePlayer = ProxiedBubblePlayer.getObject(player.getUniqueId());
        if (bubblePlayer == null) {
            throw new IllegalArgumentException("Please wait a second");
        }
        return bubblePlayer;
    }

    private static List<UUID> newList(UUID[] uuids) {
        List<UUID> uuidList = new ArrayList<>();
        uuidList.addAll(Arrays.asList(uuids));
        return uuidList;
    }

    //TODO
    private static BaseComponent[] getPlayingInformation(UUID u) {
        ProxiedBubblePlayer player;
        if ((player = ProxiedBubblePlayer.getObject(u)) != null) {
            BaseComponent[] info = TextComponent.fromLegacyText(ChatColor.GOLD + player.getNickName() + ChatColor.GREEN + " (Online) ");
            ProxiedPlayer proxiedPlayer = player.getPlayer();
            if (proxiedPlayer != null) {
                BubbleServer using = BubbleBungee.getInstance().getManager().getServer(proxiedPlayer.getServer().getInfo());
                if (using != null) {
                    TextComponent playing = new TextComponent("In " + using.getType().getName());
                    playing.setUnderlined(true);
                    playing.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/server " + using.getName()));
                    playing.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(ChatColor.GOLD + "Click to join them!")));
                    playing.setColor(ChatColor.YELLOW);
                    info = new ImmutableSet.Builder<BaseComponent>().add(info).add(playing).build().toArray(new BaseComponent[0]);
                }
            }
            return info;
        }
        SQLConnection connection = BubbleBungee.getInstance().getConnection();
        final String s = "`uuid`=\"" + u + "\" AND `key`=\"" + PlayerData.NAME + "\"";
        ResultSet set = null;
        try {
            set = SQLUtil.query(connection, PlayerData.table, "value", new SQLUtil.Where(null) {
                @Override
                public String getWhere() {
                    return s;
                }
            });
            if (set.next()) {
                return TextComponent.fromLegacyText(ChatColor.GOLD + set.getString("value") + ChatColor.RED + " (Offline) ");
            }
        } catch (SQLException | ClassNotFoundException ex) {
        } finally {
            if (set != null) {
                try {
                    set.close();
                } catch (Exception ex) {

                }
            }
        }
        return null;
    }

    public FriendCommand() {
        super("friend", null, new ImmutableSet.Builder<ICommand>().add(new SubCommand("add", null, "add <player>", "rq") {
            public BaseComponent[] Iexecute(CommandSender sender, String[] args) throws CommandException {
                ProxiedPlayer playerPlayer = notConsole(sender);
                if (args.length == 0) {
                    throw invalidUsage();
                }
                //Player requesting
                ProxiedBubblePlayer player = isPlayer(playerPlayer);
                ProxiedPlayer targetPlayer = ProxyServer.getInstance().getPlayer(args[0]);
                if (targetPlayer == null) {
                    throw new CommandException("Player is not online", this);
                }
                notSelf(playerPlayer, targetPlayer);
                UUID playeruuid = playerPlayer.getUniqueId();
                UUID targetuuid = targetPlayer.getUniqueId();
                //Player being requested
                ProxiedBubblePlayer target = isPlayer(targetPlayer);
                List<UUID> playerFriends = newList(player.getFriends());
                if (playerFriends.contains(targetuuid)) {
                    throw new CommandException("You are already friends with this player", this);
                }
                List<UUID> targetIncoming = newList(target.getFriendIncomingRequests());
                if (targetIncoming.contains(playeruuid)) {
                    throw new CommandException("You have already sent a friend request to this player", this);
                }
                List<UUID> playerIncoming = newList(player.getFriendIncomingRequests());
                List<UUID> targetFriends = newList(target.getFriends());
                String returnstring;
                if (playerIncoming.contains(targetuuid)) {
                    targetIncoming.remove(playeruuid);
                    target.setFriendsIncomingRequests(targetIncoming);
                    playerIncoming.remove(targetuuid);
                    player.setFriendsIncomingRequests(playerIncoming);
                    targetFriends.add(playeruuid);
                    target.setFriends(targetFriends);
                    playerFriends.add(targetuuid);
                    player.setFriends(playerFriends);
                    TextComponent component = new TextComponent(nowfriends.replace("{0}", player.getNickName()));
                    component.setColor(ChatColor.GOLD);
                    component.setBold(true);
                    targetPlayer.sendMessage(component);
                    returnstring = nowfriends;
                } else {
                    TextComponent component = new TextComponent(invitedfriends.replace("{0}", player.getNickName()));
                    component.setColor(ChatColor.GOLD);
                    component.setUnderlined(true);
                    component.setBold(true);
                    component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(ChatColor.YELLOW + "Click to accept the friend request")));
                    component.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/friend add " + playerPlayer.getName()));
                    targetPlayer.sendMessage(component);
                    targetIncoming.add(playeruuid);
                    target.setFriendsIncomingRequests(targetIncoming);
                    returnstring = invitefriends;
                }
                return TextComponent.fromLegacyText(returnstring.replace("{0}", target.getNickName()));
            }
        }).add(new SubCommand("list", null, "list", "listfriends") {
            public BaseComponent[] Iexecute(CommandSender sender, String[] args) throws CommandException {
                ProxiedPlayer playerPlayer = notConsole(sender);
                ProxiedBubblePlayer bubblePlayer = isPlayer(playerPlayer);
                ImmutableList.Builder<BaseComponent> components = new ImmutableList.Builder<>();
                TextComponent friends = new TextComponent("Your friends:");
                friends.setColor(ChatColor.GOLD);
                friends.setBold(true);
                friends.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(ChatColor.GOLD + "A list of all your friends!")));
                components.add(friends);
                for (UUID u : bubblePlayer.getFriends()) {
                    BaseComponent[] name = FriendCommand.getPlayingInformation(u);
                    if (name != null) {
                        TextComponent newline = new TextComponent("\n - ");
                        newline.setColor(ChatColor.GOLD);
                        components.add(newline);
                        components.add(name);
                    }
                }
                return components.build().toArray(new BaseComponent[0]);
            }
        }).add(new SubCommand("remove", null, "remove <friend>", "removefriend") {
            public BaseComponent[] Iexecute(CommandSender sender, String[] args) throws CommandException {
                ProxiedPlayer proxiedPlayer = notConsole(sender);
                ProxiedBubblePlayer bubblePlayer = isPlayer(proxiedPlayer);
                if (args.length == 0) {
                    throw invalidUsage();
                }
                List<UUID> playerFriends = newList(bubblePlayer.getFriends());
                ProxiedBubblePlayer online = ProxiedBubblePlayer.getObject(args[0]);
                if (online == null) {
                    //Get the player manually
                    UUID u = instance.getUUID(args[0]);
                    if (u == null) {
                        throw new CommandException("Player not found", this);
                    }
                    try {
                        online = instance.getBubblePlayer(u);
                    } catch (Exception e) {
                        throw new CommandException("Player not found", this);
                    }
                }

                //Check whether the target is the sender
                if (online.getUUID() == proxiedPlayer.getUniqueId()) {
                    throw new CommandException("You are not friends with yourself", this);
                }
                //Check whether the target is contained within the player friend list
                if (!playerFriends.contains(online.getUUID())) {
                    throw new CommandException("You are not friends with this player", this);
                }
                online.getPlayer().sendMessage(TextComponent.fromLegacyText(ChatColor.GOLD + "You are no longer friends with " + bubblePlayer.getNickName()));
                //Remove the target from the senders friend list
                playerFriends.remove(online.getUUID());
                //Set the players friend list
                bubblePlayer.setFriends(playerFriends);
                //Get the friends of the target
                List<UUID> targetfriends = newList(online.getFriends());
                //Remove the sender from the targets friend list
                targetfriends.remove(proxiedPlayer.getUniqueId());
                //Set the targets friend list
                online.setFriends(targetfriends);
                return TextComponent.fromLegacyText(ChatColor.GOLD + "You are no longer friends with " + online.getNickName());
            }
        }).build(), "friends","friendrequest","addfriend","buddy","friendrq","requestfriend");
    }
}
