package com.thebubblenetwork.bubblebungee.command.commands;

import com.thebubblenetwork.api.global.data.PlayerData;
import com.thebubblenetwork.api.global.sql.SQLConnection;
import com.thebubblenetwork.api.global.sql.SQLUtil;
import com.thebubblenetwork.bubblebungee.BubbleBungee;
import com.thebubblenetwork.bubblebungee.command.CommandException;
import com.thebubblenetwork.bubblebungee.command.SimpleCommand;
import com.thebubblenetwork.bubblebungee.player.ProxiedBubblePlayer;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.logging.Level;

/**
 * The Bubble Network 2016
 * BubbleBungee
 * 16/02/2016 {21:16}
 * Created February 2016
 */
public class TokenCommand extends SimpleCommand {
    private static BubbleBungee instance = BubbleBungee.getInstance();

    public TokenCommand() {
        super("tokens", null, "/tokens [other]", "token", "gettokens");
    }

    public BaseComponent[] Iexecute(CommandSender sender, String[] args) throws CommandException {
        TextComponent c;
        TextComponent amt;
        if (args.length == 0 && sender instanceof ProxiedPlayer) {
            ProxiedPlayer proxiedPlayer = (ProxiedPlayer) sender;
            ProxiedBubblePlayer bubblePlayer = ProxiedBubblePlayer.getObject(proxiedPlayer.getUniqueId());
            if (bubblePlayer == null) {
                throw new CommandException("Your not online!", this);
            }
            c = new TextComponent("Your tokens: ");
            c.setColor(ChatColor.GOLD);
            c.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(ChatColor.GOLD + "The current amount of tokens you have")));
            amt = new TextComponent(String.valueOf(bubblePlayer.getTokens()));
            amt.setColor(ChatColor.RED);
        } else {
            if (args.length == 0) {
                throw invalidUsage();
            }
            String other = args[0];
            ProxiedBubblePlayer target = ProxiedBubblePlayer.getObject(other);
            if (target == null) {
                UUID u = instance.getUUID(other);
                if (u == null) {
                    throw new CommandException("Player not found", this);
                }
                try {
                    target = instance.getBubblePlayer(u);
                } catch (Exception e) {
                    throw new CommandException("Player not found", this);
                }
            }
            c = new TextComponent(target.getNickName() + "\'s tokens: ");
            c.setColor(ChatColor.GOLD);
            c.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(ChatColor.GOLD + "The current amount of tokens " + target.getNickName() + " has")));
            amt = new TextComponent(String.valueOf(target.getTokens()));
            amt.setColor(ChatColor.RED);
        }
        return new BaseComponent[]{c, amt};
    }
}
