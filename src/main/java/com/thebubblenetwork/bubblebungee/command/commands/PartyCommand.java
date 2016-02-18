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
                        if(p == null || !p.isLeader(player)){
                            throw new CommandException("You are not leader of a party",this);
                        }
                        if(bubbleTarget.getParty() != null && bubbleTarget.getParty().isMember(bubbleTarget.getPlayer())){
                            throw new CommandException("This player is already in a party",this);
                        }
                        p.invite(bubbleTarget.getPlayer(),bubblePlayer.getNickName() + " invited " + bubbleTarget.getNickName() + " to the party");
                        TextComponent invitemessage = new TextComponent("You were invited to " + bubblePlayer.getNickName() + "'s party");
                        invitemessage.setColor(ChatColor.GOLD);
                        invitemessage.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,TextComponent.fromLegacyText(ChatColor.RED + "Click to join")));
                        invitemessage.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,"/party join " + bubblePlayer.getName()));
                        bubbleTarget.getPlayer().sendMessage(ChatMessageType.CHAT,Party.prefix,invitemessage);
                        return new ImmutableSet.Builder<BaseComponent>().add(Party.prefix).add(TextComponent.fromLegacyText(ChatColor.GOLD + "You invited " + bubbleTarget.getNickName() + " to your party")).build().toArray(new BaseComponent[0]);
                    }
                })
                .build(),
                "parties","letsparty");
    }
}
