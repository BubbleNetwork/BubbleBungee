package com.thebubblenetwork.bubblebungee.command.commands;

import com.thebubblenetwork.api.global.website.NamelessAPI;
import com.thebubblenetwork.bubblebungee.BubbleBungee;
import com.thebubblenetwork.bubblebungee.command.CommandException;
import com.thebubblenetwork.bubblebungee.command.SimpleCommand;
import com.thebubblenetwork.bubblebungee.player.ProxiedBubblePlayer;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class RegisterCommand extends SimpleCommand{
    private static BubbleBungee bungee = BubbleBungee.getInstance();

    public RegisterCommand() {
        super("register", null, "/register <email> <emailconfirm>", "registersite","joinsite");
    }

    public BaseComponent[] Iexecute(CommandSender sender, String[] args) throws CommandException {
        if(args.length < 2)throw invalidUsage();
        if(sender instanceof ProxiedPlayer){
            ProxiedPlayer proxiedPlayer = (ProxiedPlayer)sender;
            ProxiedBubblePlayer player = ProxiedBubblePlayer.getObject(proxiedPlayer.getUniqueId());
            if(!player.getNamelessUser().isRegistered()){
                String email = args[0];
                String emailcopy = args[1];
                if(!email.equals(emailcopy))throw new CommandException("Emails are not the same!", this);
                if(!NamelessAPI.isEmail(email))throw new CommandException("Invalid email", this);
                if(bungee.getNameless().isRegisteredEmail(email))throw new CommandException("This email has already been registered", this);
                player.getNamelessUser().register(proxiedPlayer.getName(), email, proxiedPlayer.getAddress().getAddress().getHostName());
                return new ComponentBuilder("Congratulations you have registered ").color(ChatColor.BLUE).append("Click here to change your password").color(ChatColor.AQUA).underlined(true).event(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://www.thebubblenetwork.com/")).create();
            }
            throw new CommandException("You are already registered", this);
        }
        throw new CommandException("You must be a player to do this", this);
    }
}
