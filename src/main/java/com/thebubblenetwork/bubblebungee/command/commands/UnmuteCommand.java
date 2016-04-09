package com.thebubblenetwork.bubblebungee.command.commands;

import com.thebubblenetwork.bubblebungee.BubbleBungee;
import com.thebubblenetwork.bubblebungee.command.CommandException;
import com.thebubblenetwork.bubblebungee.command.SimpleCommand;
import com.thebubblenetwork.bubblebungee.player.ProxiedBubblePlayer;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;

import java.util.UUID;

/**
 * The Bubble Network 2016
 * BubbleBungee
 * 09/04/2016 {12:46}
 * Created April 2016
 */
public class UnmuteCommand extends SimpleCommand{
    private static BubbleBungee instance = BubbleBungee.getInstance();

    public UnmuteCommand() {
        super("unmute", "mutecommand.use", "/unmute <player>");
    }

    public BaseComponent[] Iexecute(CommandSender sender, String[] args) throws CommandException {
        if(args.length < 1){
            throw invalidUsage();
        }
        ProxiedBubblePlayer player = ProxiedBubblePlayer.getObject(args[0]);
        if(player == null){
            UUID u = instance.getUUID(args[0]);
            if(u == null) {
                throw new CommandException("Player not found", this);
            }
            try{
                player = instance.getBubblePlayer(u);
            }
            catch (Exception ex){
                throw new CommandException("Player not found", this);
            }
        }
        if(!player.isBanned()) {
            throw new CommandException("Player is not banned", this);
        }
        player.unmute(false);
        return TextComponent.fromLegacyText(ChatColor.GOLD + "Successfully unmuted \'" + player.getName() + " (" + player.getNickName() + ChatColor.GOLD + ")\'");
    }
}
