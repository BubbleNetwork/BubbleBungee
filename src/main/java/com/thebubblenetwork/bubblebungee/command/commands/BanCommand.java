package com.thebubblenetwork.bubblebungee.command.commands;

import com.thebubblenetwork.bubblebungee.command.CommandException;
import com.thebubblenetwork.bubblebungee.command.SimpleCommand;
import com.thebubblenetwork.bubblebungee.player.ProxiedBubblePlayer;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.BaseComponent;

public class BanCommand extends SimpleCommand{
    public BanCommand() {
        super("ban", "bancommand.use", "/ban <player>");
    }

    public BaseComponent[] Iexecute(CommandSender sender, String[] args) throws CommandException {
        if(args.length < 0){
            throw invalidUsage();
        }
        String targetstring = args[0];
        ProxiedBubblePlayer bubblePlayer = ProxiedBubblePlayer.getObject(targetstring);
        boolean save = false;
        if(bubblePlayer == null){
            save = true;
        }
        //TODO - finisg
        return null;
    }
}
