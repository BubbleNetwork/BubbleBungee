package com.thebubblenetwork.bubblebungee.command.commands;

import com.thebubblenetwork.bubblebungee.command.CommandException;
import com.thebubblenetwork.bubblebungee.command.SimpleCommand;
import com.thebubblenetwork.bubblebungee.player.ProxiedBubblePlayer;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

/**
 * The Bubble Network 2016
 * BubbleBungee
 * 16/02/2016 {21:16}
 * Created February 2016
 */
public class TokenCommand extends SimpleCommand{
    public TokenCommand() {
        super("tokens", null, "/tokens [other]","token","gettokens");
    }

    public BaseComponent[] Iexecute(CommandSender sender, String[] args) throws CommandException {
        TextComponent c;
        TextComponent amt;
        if(args.length == 0 && sender instanceof ProxiedPlayer){
            ProxiedPlayer proxiedPlayer = (ProxiedPlayer)sender;
            ProxiedBubblePlayer bubblePlayer = ProxiedBubblePlayer.getObject(proxiedPlayer.getUniqueId());
            if(bubblePlayer == null)throw new CommandException("Your not online!",this);
            c = new TextComponent("Your tokens: ");
            c.setColor(ChatColor.GOLD);
            c.setUnderlined(true);
            c.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,TextComponent.fromLegacyText(ChatColor.GOLD + "The current amount of tokens you have")));
            amt = new TextComponent(String.valueOf(bubblePlayer.getTokens()));
            amt.setColor(ChatColor.RED);
            amt.setBold(true);
        }
        else{
            if(args.length == 0)throw invalidUsage();
            String other = args[0];
            ProxiedBubblePlayer target = ProxiedBubblePlayer.getObject(other);
            if(target == null)throw new CommandException("Player not found",this);
            c = new TextComponent(target.getNickName() + "\'s tokens: ");
            c.setColor(ChatColor.GOLD);
            c.setUnderlined(true);
            c.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,TextComponent.fromLegacyText(ChatColor.GOLD + "The current amount of tokens " + target.getNickName() + " has")));
            amt = new TextComponent(String.valueOf(target.getTokens()));
            amt.setColor(ChatColor.RED);
            amt.setBold(true);
        }
        return new BaseComponent[]{c,amt};
    }
}
