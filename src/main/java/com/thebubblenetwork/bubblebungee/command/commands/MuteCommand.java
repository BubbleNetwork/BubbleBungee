package com.thebubblenetwork.bubblebungee.command.commands;

import com.thebubblenetwork.api.global.data.PunishmentData;
import com.thebubblenetwork.api.global.sql.SQLConnection;
import com.thebubblenetwork.bubblebungee.BubbleBungee;
import com.thebubblenetwork.bubblebungee.command.CommandException;
import com.thebubblenetwork.bubblebungee.command.SimpleCommand;
import com.thebubblenetwork.bubblebungee.player.ProxiedBubblePlayer;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.BaseComponent;

public class MuteCommand extends SimpleCommand{
    public MuteCommand() {
        super("mute", "muteommand.use", "/mute <player> <reason> <length>");
    }

    public BaseComponent[] Iexecute(CommandSender sender, String[] args) throws CommandException {
        if(args.length < 0){
            throw invalidUsage();
        }
        String targetstring = args[0];
        ProxiedBubblePlayer bubblePlayer = ProxiedBubblePlayer.getObject(targetstring);
        boolean save = false;
        if(bubblePlayer != null){
            save = true;

            String reason = args[1];

            if (reason != null) {
                //reason is not null

                String length = args[2];
                if (length != null) {

                    SQLConnection connection = BubbleBungee.getInstance().getConnection();
                    final String s = "`uuid`=\"" + bubblePlayer.getUUID() + "\" AND `key`=\"" + PunishmentData.BANNED + "\"";



                }

            }

        }
        //TODO - finisg
        return null;
    }
}
