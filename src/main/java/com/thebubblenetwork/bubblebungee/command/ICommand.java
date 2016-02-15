package com.thebubblenetwork.bubblebungee.command;

import net.md_5.bungee.api.CommandSender;

public interface ICommand {
    String Iexecute(CommandSender sender,String[] args) throws CommandException;
    String getUsage();
    String getIPermission();
    String getName();
    String[] getAliases();
}
