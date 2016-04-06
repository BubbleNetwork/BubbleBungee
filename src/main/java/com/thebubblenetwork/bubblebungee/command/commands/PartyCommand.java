package com.thebubblenetwork.bubblebungee.command.commands;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableSet;
import com.thebubblenetwork.bubblebungee.command.BaseCommand;
import com.thebubblenetwork.bubblebungee.command.CommandException;
import com.thebubblenetwork.bubblebungee.command.ICommand;
import com.thebubblenetwork.bubblebungee.command.SubCommand;
import com.thebubblenetwork.bubblebungee.party.Party;
import com.thebubblenetwork.bubblebungee.player.ProxiedBubblePlayer;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Created by User on 18/02/2016.
 */
public class PartyCommand extends BaseCommand {
    public static ProxiedPlayer notConsole(CommandSender sender) {
        if (sender instanceof ProxiedPlayer) {
            return (ProxiedPlayer) sender;
        }
        throw new IllegalArgumentException("You must be a player to do this");
    }

    public static boolean isPartyBoolean(ProxiedPlayer player, Party p) {
        return p != null && p.isMember(player) && !(p.isLeader(player) && p.getMembers().size() < 2 && p.getInvited().size() < 1);
    }

    public static void isParty(ProxiedPlayer player, Party p) {
        if (!isPartyBoolean(player, p)) {
            throw new IllegalArgumentException("Not in a party");
        }
    }

    public static String getPartyInfo(Party p) {
        String info = ChatColor.GOLD + "Info";
        info += "\n Members: " + String.valueOf(p.getMembers().size());
        ProxiedBubblePlayer leader = ProxiedBubblePlayer.getObject(p.getLeader());
        info += "\n (Leader) " + (leader == null ? "Unknown" : leader.getNickName());
        for (UUID u : p.getMembers()) {
            if (u != p.getLeader()) {
                ProxiedBubblePlayer target = ProxiedBubblePlayer.getObject(u);
                info += ", " + (target == null ? "Unknown" : target.getNickName());
            }
        }
        info += "\n Invited: " + String.valueOf(p.getInvited().size());
        Set<String> invitelist = new HashSet<>();
        for (UUID u : p.getInvited()) {
            ProxiedBubblePlayer target = ProxiedBubblePlayer.getObject(u);
            invitelist.add(target == null ? "Unknown" : target.getNickName());
        }
        info += "\n" + Joiner.on(", ").join(invitelist);
        return info;
    }

    public PartyCommand() {
        super("party", null, new ImmutableSet.Builder<ICommand>().add(new SubCommand("invite", null, "invite <player>", "add", "create") {
            public BaseComponent[] Iexecute(CommandSender sender, String[] args) throws CommandException {
                ProxiedPlayer player = notConsole(sender);
                if (args.length == 0) {
                    throw invalidUsage();
                }
                ProxiedBubblePlayer bubblePlayer = ProxiedBubblePlayer.getObject(player.getUniqueId());
                String targetname = args[0];
                ProxiedBubblePlayer bubbleTarget = ProxiedBubblePlayer.getObject(targetname);
                if (bubbleTarget == null) {
                    throw new CommandException("Player not found", this);
                }
                Party p = bubblePlayer.getParty();
                if (p == null) {
                    p = new Party(player);
                    bubblePlayer.setParty(p);
                } else if (!p.isLeader(player)) {
                    throw new CommandException("You are not leader of this party", this);
                }
                if (isPartyBoolean(bubbleTarget.getPlayer(), bubbleTarget.getParty())) {
                    throw new CommandException("This player is already in a party", this);
                }
                p.invite(bubbleTarget.getPlayer(), bubblePlayer.getNickName() + " invited " + bubbleTarget.getNickName() + " to the party");
                TextComponent invitemessage = new TextComponent("You were invited to " + bubblePlayer.getNickName() + "'s party");
                invitemessage.setColor(ChatColor.GOLD);
                invitemessage.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(ChatColor.RED + "Click to join")));
                invitemessage.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/party join " + bubblePlayer.getName()));
                invitemessage.setUnderlined(true);
                bubbleTarget.getPlayer().sendMessage(ChatMessageType.CHAT, Party.prefix, invitemessage);
                return new ImmutableSet.Builder<BaseComponent>().add(Party.prefix).add(TextComponent.fromLegacyText(ChatColor.GOLD + "You invited " + bubbleTarget.getNickName() + " to your party")).build().toArray(new BaseComponent[0]);
            }
        }).add(new SubCommand("deinvite", null, "deinvite <player>", "cancel", "cancelinvite") {
            public BaseComponent[] Iexecute(CommandSender sender, String[] args) throws CommandException {
                ProxiedPlayer player = notConsole(sender);
                if (args.length == 0) {
                    throw invalidUsage();
                }
                ProxiedBubblePlayer bubblePlayer = ProxiedBubblePlayer.getObject(player.getUniqueId());
                String targetname = args[0];
                ProxiedBubblePlayer bubbleTarget = ProxiedBubblePlayer.getObject(targetname);
                if (bubbleTarget == null) {
                    throw new CommandException("Player not found", this);
                }
                Party p = bubblePlayer.getParty();
                isParty(player, p);
                if (!p.isLeader(player)) {
                    throw new CommandException("You are not leader of this party", this);
                }
                if (bubbleTarget.getParty() != null && bubbleTarget.getParty().isMember(bubbleTarget.getPlayer()) && !(bubbleTarget.getParty().isLeader(bubblePlayer.getPlayer()) && bubbleTarget.getParty().getMembers().size() < 2)) {
                    throw new CommandException("This player is already in a party", this);
                }
                p.invite(bubbleTarget.getPlayer(), bubblePlayer.getNickName() + " invited " + bubbleTarget.getNickName() + " to the party");
                TextComponent invitemessage = new TextComponent("You were invited to " + bubblePlayer.getNickName() + "'s party");
                invitemessage.setColor(ChatColor.GOLD);
                invitemessage.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(ChatColor.RED + "Click to join")));
                invitemessage.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/party join " + bubblePlayer.getName()));
                invitemessage.setUnderlined(true);
                bubbleTarget.getPlayer().sendMessage(ChatMessageType.CHAT, Party.prefix, invitemessage);
                return new ImmutableSet.Builder<BaseComponent>().add(Party.prefix).add(TextComponent.fromLegacyText(ChatColor.GOLD + "You invited " + bubbleTarget.getNickName() + " to your party")).build().toArray(new BaseComponent[0]);
            }
        }).add(new SubCommand("kick", null, "kick <player>", "remove") {
            public BaseComponent[] Iexecute(CommandSender sender, String[] args) throws CommandException {
                ProxiedPlayer player = notConsole(sender);
                if (args.length == 0) {
                    throw invalidUsage();
                }
                ProxiedBubblePlayer bubblePlayer = ProxiedBubblePlayer.getObject(player.getUniqueId());
                String targetname = args[0];
                ProxiedBubblePlayer bubbleTarget = ProxiedBubblePlayer.getObject(targetname);
                if (bubbleTarget == null) {
                    throw new CommandException("Player not found", this);
                }
                Party p = bubblePlayer.getParty();
                isParty(player, p);
                if (!p.isLeader(player)) {
                    throw new CommandException("You are not leader of this party", this);
                }
                if (bubbleTarget.getParty() != p || !p.isMember(bubbleTarget.getPlayer())) {
                    throw new CommandException("This player is not a member of your party", this);
                }
                p.removeMember(bubbleTarget.getPlayer(), bubblePlayer.getNickName() + " kicked " + bubbleTarget.getNickName() + " from the party");
                bubbleTarget.setParty(null);
                return new ImmutableSet.Builder<BaseComponent>().add(Party.prefix).add(TextComponent.fromLegacyText(ChatColor.GOLD + "You kicked " + bubbleTarget.getNickName())).build().toArray(new BaseComponent[0]);
            }
        }).add(new SubCommand("disband", null, "disband", "delete") {
            public BaseComponent[] Iexecute(CommandSender sender, String[] args) throws CommandException {
                ProxiedPlayer player = notConsole(sender);
                ProxiedBubblePlayer bubblePlayer = ProxiedBubblePlayer.getObject(player.getUniqueId());
                Party p = bubblePlayer.getParty();
                isParty(player, p);
                if (!p.isLeader(player)) {
                    throw new CommandException("You are not leader of this party", this);
                }
                p.disband(bubblePlayer.getNickName() + " disbanded the party");
                return new ImmutableSet.Builder<BaseComponent>().add(Party.prefix).add(TextComponent.fromLegacyText(ChatColor.GOLD + "You successfully disbanded the party")).build().toArray(new BaseComponent[0]);
            }
        }).add(new SubCommand("leave", null, "leave", "quit") {
            public BaseComponent[] Iexecute(CommandSender sender, String[] args) throws CommandException {
                ProxiedPlayer player = notConsole(sender);
                ProxiedBubblePlayer bubblePlayer = ProxiedBubblePlayer.getObject(player.getUniqueId());
                Party p = bubblePlayer.getParty();
                isParty(player, p);
                if (!p.isMember(player)) {
                    throw new CommandException("You are not a member of this party", this);
                }
                if (p.isLeader(player)) {
                    throw new CommandException("You are leader of this party, use /party disband", this);
                }
                bubblePlayer.setParty(null);
                p.removeMember(player, bubblePlayer.getNickName() + " left the party");
                return new ImmutableSet.Builder<BaseComponent>().add(Party.prefix).add(TextComponent.fromLegacyText(ChatColor.GOLD + "You successfully left the party")).build().toArray(new BaseComponent[0]);
            }
        }).add(new SubCommand("join", null, "join <party>") {
            public BaseComponent[] Iexecute(CommandSender sender, String[] args) throws CommandException {
                ProxiedPlayer player = notConsole(sender);
                if (args.length == 0) {
                    throw invalidUsage();
                }
                ProxiedBubblePlayer bubblePlayer = ProxiedBubblePlayer.getObject(player.getUniqueId());
                Party p = bubblePlayer.getParty();
                if(isPartyBoolean(player, p))throw new CommandException("You are already in a party", this);
                ProxiedBubblePlayer target = ProxiedBubblePlayer.getObject(args[0]);
                if (target == null) {
                    throw new CommandException("Player not found", this);
                }
                p = target.getParty();
                if (p == null) {
                    throw new CommandException("Party not found", this);
                }
                if (!p.isInvited(player)) {
                    p.broadcast(TextComponent.fromLegacyText(ChatColor.GOLD + bubblePlayer.getNickName() + " tried to join"));
                    throw new CommandException("You're not invited to this party", this);
                }
                bubblePlayer.setParty(p);
                p.addMember(player, bubblePlayer.getNickName() + " joined the party");
                ProxiedBubblePlayer leader = ProxiedBubblePlayer.getObject(target.getUUID());
                if (leader == null) {
                    leader = target;
                }
                return new ImmutableSet.Builder<BaseComponent>().add(Party.prefix).add(TextComponent.fromLegacyText(ChatColor.GOLD + "You successfully joined the party of " + leader.getNickName())).build().toArray(new BaseComponent[0]);
            }
        }).add(new SubCommand("info", null, "info [other]", "list", "show", "who") {
            public BaseComponent[] Iexecute(CommandSender sender, String[] args) throws CommandException {
                if (sender instanceof ProxiedPlayer && args.length == 0) {
                    ProxiedPlayer player = (ProxiedPlayer) sender;
                    ProxiedBubblePlayer bubblePlayer = ProxiedBubblePlayer.getObject(player.getUniqueId());
                    Party p = bubblePlayer.getParty();
                    isParty(player, p);
                    String info = getPartyInfo(p);
                    return new ImmutableSet.Builder<BaseComponent>().add(Party.prefix).add(TextComponent.fromLegacyText(info)).build().toArray(new BaseComponent[0]);
                }
                if (args.length == 0) {
                    throw invalidUsage();
                }
                ProxiedBubblePlayer player = ProxiedBubblePlayer.getObject(args[0]);
                if (player == null) {
                    throw new CommandException("Player not found", this);
                }
                Party p = player.getParty();
                isParty(player.getPlayer(), p);
                String info = getPartyInfo(p);
                return new ImmutableSet.Builder<BaseComponent>().add(Party.prefix).add(TextComponent.fromLegacyText(info)).build().toArray(new BaseComponent[0]);
            }
        }).build(), "parties", "letsparty");
    }
}
