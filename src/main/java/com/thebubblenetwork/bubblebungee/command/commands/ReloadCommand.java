package com.thebubblenetwork.bubblebungee.command.commands;


import com.thebubblenetwork.api.global.plugin.updater.Updatetask;
import com.thebubblenetwork.bubblebungee.BubbleBungee;
import com.thebubblenetwork.bubblebungee.BungeePlugman;
import com.thebubblenetwork.bubblebungee.command.CommandException;
import com.thebubblenetwork.bubblebungee.command.SimpleCommand;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Plugin;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

public class ReloadCommand extends SimpleCommand{
    private BungeePlugman plugman;

    public ReloadCommand(String prefix,BungeePlugman plugman) {
        super(prefix + "reload", "bungeeplugman.reloadall", prefix + "rl");
        this.plugman = plugman;
    }

    public String Iexecute(final CommandSender sender, String[] args) throws CommandException {
        if(Updatetask.instance != null)throw new CommandException("Cannot reload while updatetask is running!",this);
        plugman.getServer().getScheduler().runAsync(BubbleBungee.getInstance().getPlugin(), new Runnable() {
            public void run() {
                final Collection<Plugin> pluginCollection = plugman.getPlugins();
                new Thread(){
                    public void run() {
                        Set<Plugin> plugins = new HashSet<>();
                        plugins.addAll(pluginCollection);
                        for(Plugin plugin:plugins){
                            String name = plugin.getDescription().getName();
                            String version = plugin.getDescription().getVersion();
                            File file = plugin.getFile();
                            try {
                                plugman.unload(plugin);
                            } catch (Throwable throwable) {
                                plugman.getServer().getLogger().log(Level.WARNING,"An error occurred while unloading " + name + " v" + version + " (" + file.getPath() + ")");
                            }
                        }
                        plugman.reset_toLoad();
                        plugman.getPluginManager().detectPlugins(new File("modules"));
                        plugman.getPluginManager().detectPlugins(new File("plugins"));
                        plugman.getPluginManager().loadPlugins();
                        plugman.getPluginManager().enablePlugins();
                        sender.sendMessage(TextComponent.fromLegacyText(ChatColor.GREEN + "Reload Complete"));
                    }
                }.start();
            }
        });
        return ChatColor.GREEN + "Reloading plugins...";
    }
}
