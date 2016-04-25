package com.thebubblenetwork.bubblebungee.command.commands;

import com.thebubblenetwork.bubblebungee.BubbleBungee;
import com.thebubblenetwork.bubblebungee.command.CommandException;
import com.thebubblenetwork.bubblebungee.command.SimpleCommand;
import com.thebubblenetwork.bubblebungee.servermanager.BubbleServer;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

/**
 * The Bubble Network 2016
 * BubbleBungee
 * 17/04/2016 {09:41}
 * Created April 2016
 */
public class LobbyCommand extends SimpleCommand{
    private static BubbleBungee instance = BubbleBungee.getInstance();

    public LobbyCommand() {
        super("lobby", null, "/lobby", "hub");
    }

    public BaseComponent[] Iexecute(CommandSender sender, String[] args) throws CommandException {
        if(sender instanceof ProxiedPlayer){
            ProxiedPlayer player = (ProxiedPlayer)sender;
            BubbleServer server = instance.getManager().getServer(player.getServer().getInfo());
            if(server.getType().getName().equals("Lobby")){
                throw new CommandException("You are already in a lobby server", this);
            }

        }
        else throw new CommandException("Must be a player to do this", this);
        return null;
    }
}
