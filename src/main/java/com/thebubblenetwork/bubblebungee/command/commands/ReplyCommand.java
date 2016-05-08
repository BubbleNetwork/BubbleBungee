package com.thebubblenetwork.bubblebungee.command.commands;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.thebubblenetwork.api.global.java.ArgTrimmer;
import com.thebubblenetwork.bubblebungee.BubbleBungee;
import com.thebubblenetwork.bubblebungee.command.CommandException;
import com.thebubblenetwork.bubblebungee.command.SimpleCommand;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ReplyCommand extends SimpleCommand{
    private static BubbleBungee instance = BubbleBungee.getInstance();

    public static Map<String, String> REPLYMAP = new HashMap<>();

    private MessageCommand messageCommand = new MessageCommand();

    public ReplyCommand() {
        super("reply", null, "/reply <message>", "r");
    }

    @Override
    public BaseComponent[] Iexecute(CommandSender sender, String[] args) throws CommandException {
        if(args.length == 0) {
            throw invalidUsage();
        }
        if(!REPLYMAP.containsKey(sender.getName())){
            throw new CommandException("You have no one to reply to!", this);
        }
        int arglength = args.length;
        return messageCommand.Iexecute(sender, new ImmutableList.Builder<String>().add(REPLYMAP.get(sender.getName())).addAll(Arrays.asList(args)).build().toArray(new String[arglength + 1]));
    }
}
