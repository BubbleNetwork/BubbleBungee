package com.thebubblenetwork.bubblebungee.command.commands;

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

/**
 * Created by User on 18/02/2016.
 */
public class PartyCommand extends BaseCommand{
    public static ProxiedPlayer notConsole(CommandSender sender){
        if(sender instanceof ProxiedPlayer){
            return (ProxiedPlayer)sender;
        }
        throw new IllegalArgumentException("You must be a player to do this");
    }

    public PartyCommand() {
        super("party", null, new ImmutableSet.Builder<ICommand>()
                .add(new SubCommand("invite",null,"invite <player>","add") {
                    public BaseComponent[] Iexecute(CommandSender sender, String[] args) throws CommandException {
                        ProxiedPlayer player = notConsole(sender);
                        if(args.length == 0)throw invalidUsage();
                        ProxiedBubblePlayer bubblePlayer = ProxiedBubblePlayer.getObject(player.getUniqueId());
                        String targetname = args[0];
                        ProxiedBubblePlayer bubbleTarget = ProxiedBubblePlayer.getObject(targetname);
                        if(bubbleTarget == null){
                            throw new CommandException("Player not found",this);
                        }
                        Party p = bubblePlayer.getParty();
                        if(p == null){
                            p = new Party(player);
                            bubblePlayer.setParty(p);
                        }
                        else if(!p.isLeader(player)){
                            throw new CommandException("You are not leader of this party",this);
                        }
                        if(bubbleTarget.getParty() != null && bubbleTarget.getParty().isMember(bubbleTarget.getPlayer()) && !(bubbleTarget.getParty().isLeader(bubblePlayer.getPlayer()) && bubbleTarget.getParty().getMembers().size() < 2)){
                            throw new CommandException("This player is already in a party",this);
                        }
                        p.invite(bubbleTarget.getPlayer(),bubblePlayer.getNickName() + " invited " + bubbleTarget.getNickName() + " to the party");
                        TextComponent invitemessage = new TextComponent("You were invited to " + bubblePlayer.getNickName() + "'s party");
                        invitemessage.setColor(ChatColor.GOLD);
                        invitemessage.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,TextComponent.fromLegacyText(ChatColor.RED + "Click to join")));
                        invitemessage.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,"/party join " + bubblePlayer.getName()));
                        invitemessage.setUnderlined(true);
                        bubbleTarget.getPlayer().sendMessage(ChatMessageType.CHAT,Party.prefix,invitemessage);
                        return new ImmutableSet.Builder<BaseComponent>().add(Party.prefix).add(TextComponent.fromLegacyText(ChatColor.GOLD + "You invited " + bubbleTarget.getNickName() + " to your party")).build().toArray(new BaseComponent[0]);
                    }
                })
                .add(new SubCommand("deinvite",null,"deinvite <player>","cancel","cancelinvite") {
                    public BaseComponent[] Iexecute(CommandSender sender, String[] args) throws CommandException {
                        ProxiedPlayer player = notConsole(sender);
                        if(args.length == 0)throw invalidUsage();
                        ProxiedBubblePlayer bubblePlayer = ProxiedBubblePlayer.getObject(player.getUniqueId());
                        String targetname = args[0];
                        ProxiedBubblePlayer bubbleTarget = ProxiedBubblePlayer.getObject(targetname);
                        if(bubbleTarget == null){
                            throw new CommandException("Player not found",this);
                        }
                        Party p = bubblePlayer.getParty();
                        if(p == null || !p.isMember(player) || (p.isLeader(player) && p.getMembers().size() < 2)){
                            bubblePlayer.setParty(null);
                            throw new CommandException("You aren't in a party",this);
                        }
                        if(!p.isLeader(player)){
                            throw new CommandException("You are not leader of this party",this);
                        }
                        if(bubbleTarget.getParty() != null && bubbleTarget.getParty().isMember(bubbleTarget.getPlayer()) && !(bubbleTarget.getParty().isLeader(bubblePlayer.getPlayer()) && bubbleTarget.getParty().getMembers().size() < 2)){
                            throw new CommandException("This player is already in a party",this);
                        }
                        p.invite(bubbleTarget.getPlayer(),bubblePlayer.getNickName() + " invited " + bubbleTarget.getNickName() + " to the party");
                        TextComponent invitemessage = new TextComponent("You were invited to " + bubblePlayer.getNickName() + "'s party");
                        invitemessage.setColor(ChatColor.GOLD);
                        invitemessage.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,TextComponent.fromLegacyText(ChatColor.RED + "Click to join")));
                        invitemessage.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,"/party join " + bubblePlayer.getName()));
                        invitemessage.setUnderlined(true);
                        bubbleTarget.getPlayer().sendMessage(ChatMessageType.CHAT,Party.prefix,invitemessage);
                        return new ImmutableSet.Builder<BaseComponent>().add(Party.prefix).add(TextComponent.fromLegacyText(ChatColor.GOLD + "You invited " + bubbleTarget.getNickName() + " to your party")).build().toArray(new BaseComponent[0]);
                    }
                })
                .add(new SubCommand("kick",null,"kick <player>","remove") {
                    public BaseComponent[] Iexecute(CommandSender sender, String[] args) throws CommandException {
                        ProxiedPlayer player = notConsole(sender);
                        if(args.length == 0)throw invalidUsage();
                        ProxiedBubblePlayer bubblePlayer = ProxiedBubblePlayer.getObject(player.getUniqueId());
                        String targetname = args[0];
                        ProxiedBubblePlayer bubbleTarget = ProxiedBubblePlayer.getObject(targetname);
                        if(bubbleTarget == null){
                            throw new CommandException("Player not found",this);
                        }
                        Party p = bubblePlayer.getParty();
                        if(p == null || !p.isMember(player) || (p.isLeader(player) && p.getMembers().size() < 2)){
                            bubblePlayer.setParty(null);
                            throw new CommandException("You aren't in a party",this);
                        }
                        if(!p.isLeader(player)){
                            throw new CommandException("You are not leader of this party",this);
                        }
                        if(bubbleTarget.getParty() != p || !p.isMember(bubbleTarget.getPlayer())){
                            throw new CommandException("This player is not a member of your party",this);
                        }
                        p.removeMember(bubbleTarget.getPlayer(),bubblePlayer.getNickName() + " kicked " + bubbleTarget.getNickName() + " from the party");
                        bubbleTarget.setParty(null);
                        return new ImmutableSet.Builder<BaseComponent>().add(Party.prefix).add(TextComponent.fromLegacyText(ChatColor.GOLD + "You kicked " + bubbleTarget.getNickName())).build().toArray(new BaseComponent[0]);
                    }
                })
                .add(new SubCommand("disband",null,"disband","delete") {
                    public BaseComponent[] Iexecute(CommandSender sender, String[] args) throws CommandException {
                        ProxiedPlayer player = notConsole(sender);
                        ProxiedBubblePlayer bubblePlayer = ProxiedBubblePlayer.getObject(player.getUniqueId());
                        Party p = bubblePlayer.getParty();
                        if(p == null){
                            p = new Party(player);
                            bubblePlayer.setParty(p);
                        }
                        if(!p.isLeader(player)){
                            throw new CommandException("You are not leader of this party",this);
                        }
                        p.disband(bubblePlayer.getNickName() + " disbanded the party");
                        return new ImmutableSet.Builder<BaseComponent>().add(Party.prefix).add(TextComponent.fromLegacyText(ChatColor.GOLD + "You successfully disbanded the party")).build().toArray(new BaseComponent[0]);
                    }
                })
                .add(new SubCommand("leave",null,"leave","quit") {
                    public BaseComponent[] Iexecute(CommandSender sender, String[] args) throws CommandException {
                        ProxiedPlayer player = notConsole(sender);
                        ProxiedBubblePlayer bubblePlayer = ProxiedBubblePlayer.getObject(player.getUniqueId());
                        Party p = bubblePlayer.getParty();
                        if(p == null){
                            throw new CommandException("You aren't in a party",this);
                        }
                        if(!p.isMember(player)){
                            throw new CommandException("You are not a member of this party",this);
                        }
                        if(p.isLeader(player)){
                            if(p.getMembers().size() < 2){
                                bubblePlayer.setParty(null);
                                throw new CommandException("You are not in a party",this);
                            }
                            throw new CommandException("You are leader of this party, use /party disband",this);
                        }
                        bubblePlayer.setParty(null);
                        p.removeMember(player, bubblePlayer.getNickName() + " left the party");
                        return new ImmutableSet.Builder<BaseComponent>().add(Party.prefix).add(TextComponent.fromLegacyText(ChatColor.GOLD + "You successfully left the party")).build().toArray(new BaseComponent[0]);
                    }
                })
                .build(),
                "parties","letsparty");
    }
}
