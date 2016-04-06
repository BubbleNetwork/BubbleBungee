package com.thebubblenetwork.bubblebungee.command.commands;

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
 * Created by User on 18/02/2016.
 */
public class SetTokenCommand extends SimpleCommand {
    private static BubbleBungee instance = BubbleBungee.getInstance();

    public SetTokenCommand() {
        super("settokens", "token.settokens", "/settokens <player> <tokens>", "settoken");
    }

    public BaseComponent[] Iexecute(CommandSender sender, String[] args) throws CommandException {
        if (args.length < 2) {
            throw invalidUsage();
        }
        String name = args[0];
        ProxiedBubblePlayer online = ProxiedBubblePlayer.getObject(name);
        boolean forcesave = false;
        if (online == null) {
            forcesave = true;
            UUID u = instance.getUUID(name);
            if (u == null) {
                throw new CommandException("Player not found", this);
            }
            try {
                online = instance.getBubblePlayer(u);
            } catch (Exception e) {
                throw new CommandException("Player not found", this);
            }
        }
        String number = args[1];
        int i;
        try {
            i = Integer.parseInt(number);
        } catch (Exception ex) {
            throw new CommandException("Invalid number", this);
        }
        online.setTokens(i);
        BaseComponent[] components = TextComponent.fromLegacyText(ChatColor.GOLD + "Successfully set the tokens of \'" + online.getNickName() + "\' to \'" + String.valueOf(i) + "\'");
        HoverEvent event = new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(ChatColor.RED + "This was " + (!forcesave ? "not" : "") + " force saved"));
        for (BaseComponent c : components) {
            c.setHoverEvent(event);
        }
        return components;
    }
}
