package com.thebubblenetwork.bubblebungee.command;

import net.md_5.bungee.api.CommandSender;

public abstract class SubCommand implements ICommand{
    public static SubCommand asMirror(final ICommand command){
        return new SubCommand(command.getName(),command.getIPermission(),
                command instanceof SubCommand ? command.getUsage() : command.getUsage().substring(1)
                ,command.getAliases()) {
            public String Iexecute(CommandSender sender, String[] args) throws CommandException {
                return command.Iexecute(sender,args);
            }
        };
    }

    private String name,usage,permission;
    private String[] aliases;

    public SubCommand(String name, String permission, String usage,String... aliases) {
        this.name = name;
        this.aliases = aliases;
        this.usage = usage;
        this.permission = permission;
    }

    public abstract String Iexecute(CommandSender sender, String[] args) throws CommandException;

    public String getUsage() {
        return usage;
    }

    public String getIPermission() {
        return permission;
    }

    public String getName() {
        return name;
    }

    public String[] getAliases() {
        return aliases;
    }
}
