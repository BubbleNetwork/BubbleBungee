package com.thebubblenetwork.bubblebungee.command.commands;

import com.thebubblenetwork.api.global.ranks.Rank;
import com.thebubblenetwork.bubblebungee.BubbleBungee;
import com.thebubblenetwork.bubblebungee.command.CommandException;
import com.thebubblenetwork.bubblebungee.command.SimpleCommand;
import com.thebubblenetwork.bubblebungee.player.ProxiedBubblePlayer;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

import java.util.UUID;

/**
 * The Bubble Network 2016
 * BubbleBungee
 * 16/02/2016 {17:17}
 * Created February 2016
 */
public class WhoisCommand extends SimpleCommand {
    private static BubbleBungee instance = BubbleBungee.getInstance();
    public WhoisCommand() {
        super("whois", "rankmanager.whois", "/whois <player>", "show", "who");
    }

    public BaseComponent[] Iexecute(CommandSender sender, String[] args) throws CommandException {
        if (args.length == 0) {
            throw invalidUsage();
        }
        String playername = args[0];
        ProxiedBubblePlayer online = ProxiedBubblePlayer.getObject(playername);
        if (online == null) {
            UUID u = instance.getUUID(playername);
            if (u == null) {
                throw new CommandException("Player not found", this);
            }
            try {
                online = instance.getDataOffline(u);
            } catch (Exception e) {
                throw new CommandException("Player not found", this);
            }
        }
        HoverEvent hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(ChatColor.RED + "All player information about " + online.getName()));
        String info = ChatColor.GOLD + "Whois: " + ChatColor.RED + online.getName();
        info += "\n" + ChatColor.GOLD + "Nickname: " + online.getNickName();
        info += "\n" + ChatColor.GOLD + "Tokens: " + String.valueOf(online.getTokens());
        info += "\n" + ChatColor.GOLD + "Rank: " + online.getRank().getName();
        info += "\n" + ChatColor.GOLD + "Subranks: ";
        for (Rank r : online.getSubRanks()) {
            info += r.getName();
        }
        BaseComponent[] baseComponents = TextComponent.fromLegacyText(info);
        for (BaseComponent b : baseComponents) {
            b.setHoverEvent(hoverEvent);
        }
        return baseComponents;
    }
}
