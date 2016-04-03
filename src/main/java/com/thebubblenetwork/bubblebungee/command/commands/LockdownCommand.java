package com.thebubblenetwork.bubblebungee.command.commands;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableSet;
import com.thebubblenetwork.bubblebungee.BubbleBungee;
import com.thebubblenetwork.bubblebungee.command.BaseCommand;
import com.thebubblenetwork.bubblebungee.command.CommandException;
import com.thebubblenetwork.bubblebungee.command.ICommand;
import com.thebubblenetwork.bubblebungee.command.SubCommand;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;

/**
 * The Bubble Network 2016
 * BubbleBungee
 * 03/04/2016 {10:52}
 * Created April 2016
 */
public class LockdownCommand extends BaseCommand{
    private static BubbleBungee instance = BubbleBungee.getInstance();

    public LockdownCommand() {
        super("lockdown", "lockdown.use", new ImmutableSet.Builder<ICommand>()
                .add(new SubCommand("info",null,"info"){
                    public BaseComponent[] Iexecute(CommandSender sender, String[] args) throws CommandException {
                        return TextComponent.fromLegacyText(ChatColor.GOLD + "Lockdown: " + instance.isLockdown() + "\nMessage: " + instance.getLockdownmsg());
                    }
                })
                .add(new SubCommand("toggle","lockdown.toggle","toggle") {
                    public BaseComponent[] Iexecute(CommandSender sender, String[] args) throws CommandException {
                        instance.setLockdown(!instance.isLockdown());
                        return TextComponent.fromLegacyText(ChatColor.GREEN + "Lockdown is now " + (instance.isLockdown() ? "enabled" : "disabled"));
                    }
                })
                .add(new SubCommand("setmessage","lockdown.message","setmessage <message>","setmsg","msg","message") {
                    public BaseComponent[] Iexecute(CommandSender sender, String[] args) throws CommandException {
                        if(args.length < 1)throw invalidUsage();
                        String message = Joiner.on(" ").join(args);
                        instance.setLockdownmsg(message);
                        return TextComponent.fromLegacyText(ChatColor.GREEN + "Lockdown message set to \'" + message + ChatColor.GREEN + "\'");
                    }
                })
                .build()

        , "whitelist","blacklist","lock");
    }
}
