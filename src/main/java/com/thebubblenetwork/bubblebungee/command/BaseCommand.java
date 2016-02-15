package com.thebubblenetwork.bubblebungee.command;

import com.thebubblenetwork.api.global.java.ArgTrimmer;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;

import java.util.Set;
import java.util.logging.Level;

public class BaseCommand extends Command implements ICommand{
    private Set<ICommand> subcommands;
    private String permissionstring;
    private String usage;
    private CommandException invalidusage;

    public BaseCommand(String name, String permission, Set<ICommand> subcommands, String ... aliases) {
        super(name,null,aliases);
        this.permissionstring = permission;
        this.usage = "/" + name + " <arg>";
        this.subcommands = subcommands;
        String s = "";
        for(ICommand command:subcommands){
            s += "\n" + getUsage().replace("<arg>",command.getName());
        }
        invalidusage = new CommandException("Invalid usage: " + getUsage() + s,this);
    }

    public void execute(CommandSender commandSender, String[] strings) {
        try {
            commandSender.sendMessage(TextComponent.fromLegacyText(Iexecute(commandSender,strings)));
        } catch (CommandException e) {
            commandSender.sendMessage(e.getResponse());
        } catch (IllegalArgumentException e){
            commandSender.sendMessage(new CommandException(e.getMessage(),this).getResponse());
        } catch (Throwable ex){
            commandSender.sendMessage(TextComponent.fromLegacyText(
                    ChatColor.RED + "An internal " + ex.getClass().getSimpleName() + " has occurred\n" + ChatColor.RED + ex.getMessage()
            ));
            ProxyServer.getInstance().getLogger().log(Level.WARNING,"An error occurred whilst executing " + getClass().getName(),ex);
        }
    }

    public String Iexecute(CommandSender commandSender, String[] strings) throws CommandException{
        if(getIPermission() != null && !commandSender.hasPermission(getIPermission())){
            throw new CommandException("You do not have permission for this command",this);
        }
        if(strings.length == 0){
            throw invalidusage;
        }
        String firstarg = strings[0];
        ICommand command = getCommand(firstarg);
        if(command != null){
            if(!commandSender.hasPermission(command.getIPermission()))throw  new CommandException("You do not have permission for this command",command);
            return command.Iexecute(commandSender,new ArgTrimmer<>(String.class,strings).trim(1));

        }
        throw invalidusage;
    }

    protected ICommand getCommand(String firstarg){
        for(ICommand command:subcommands){
            if(command.getName().equalsIgnoreCase(firstarg)){
                return command;
            }
        }
        for(ICommand command:subcommands){
            for(String alias:command.getAliases()){
                if(alias.equalsIgnoreCase(firstarg)){
                    return command;
                }
            }
        }
        return null;
    }

    public String getUsage() {
        return usage;
    }

    public String getIPermission(){
        return permissionstring;
    }
}
