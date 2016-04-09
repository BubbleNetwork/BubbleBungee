package com.thebubblenetwork.bubblebungee.command.commands;

import com.google.common.base.Joiner;
import com.thebubblenetwork.api.global.java.ArgTrimmer;
import com.thebubblenetwork.api.global.java.DateUTIL;
import com.thebubblenetwork.bubblebungee.BubbleBungee;
import com.thebubblenetwork.bubblebungee.command.CommandException;
import com.thebubblenetwork.bubblebungee.command.SimpleCommand;
import com.thebubblenetwork.bubblebungee.player.ProxiedBubblePlayer;
import de.mickare.xserver.util.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;

import java.util.Date;
import java.util.UUID;

public class MuteCommand extends SimpleCommand{
    private static BubbleBungee instance = BubbleBungee.getInstance();
    public MuteCommand() {
        super("mute", "muteommand.use", "/mute <player> [reason] [time]");
    }

    public BaseComponent[] Iexecute(CommandSender sender, String[] args) throws CommandException {
        if(args.length < 1){
            throw invalidUsage();
        }
        String targetstring = args[0];
        ProxiedBubblePlayer bubblePlayer = ProxiedBubblePlayer.getObject(targetstring);
        if(bubblePlayer == null) {
            UUID u = instance.getUUID(targetstring);
            if (u == null) {
                throw new CommandException("Player not found", this);
            }
            try {
                bubblePlayer = instance.getBubblePlayer(u);
            } catch (Exception e) {
                throw new CommandException("Player not found", this);
            }
        }
        String reason;
        Date date;
        if(args.length == 1){
            reason = "The ducktape has spoken";
            date = null;
        }
        else{
            String endproduct = Joiner.on(" ").join(new ArgTrimmer<>(String.class, args).trim(1));
            try {
                date = new Date(DateUTIL.parseDateDiff(endproduct, true));
            } catch (Exception e) {
                date = null;
            }
            reason = DateUTIL.removeTimePattern(endproduct);
        }
        bubblePlayer.mute(date, reason, sender.getName());
        return TextComponent.fromLegacyText(ChatColor.GOLD + "Successfully muted \'" + bubblePlayer.getName() + " (" + bubblePlayer.getNickName() + ChatColor.GOLD + ")\'");
    }
}
