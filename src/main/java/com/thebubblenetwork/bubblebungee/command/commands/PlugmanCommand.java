package com.thebubblenetwork.bubblebungee.command.commands;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableSet;
import com.thebubblenetwork.bubblebungee.BungeePlugman;
import com.thebubblenetwork.bubblebungee.command.BaseCommand;
import com.thebubblenetwork.bubblebungee.command.CommandException;
import com.thebubblenetwork.bubblebungee.command.ICommand;
import com.thebubblenetwork.bubblebungee.command.SubCommand;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Plugin;

import java.io.File;

public class PlugmanCommand extends BaseCommand {
    private BungeePlugman plugman;

    public PlugmanCommand(final BungeePlugman plugman) {
        super("bungeeplugman", "bungeeplugman.use",
                new ImmutableSet.Builder<ICommand>()
                        .add(new ReloadCommand("",plugman))
                        .add(new SubCommand("unload","bungeeplugman.unload","unload <plugin>") {
                            public BaseComponent[] Iexecute(CommandSender sender, String[] args) throws CommandException {
                                if(args.length == 0)throw invalidUsage();
                                String pluginname = args[0];
                                Plugin plugin = plugman.get(pluginname);
                                if(plugin == null)throw  new CommandException("Could not find a plugin with that name",this);
                                pluginname = plugin.getDescription().getName();
                                plugman.unload(plugin);
                                TextComponent c = new TextComponent("Unloaded " + pluginname + ", check console for errors");
                                c.setColor(ChatColor.GREEN);
                                c.setBold(true);
                                c.setUnderlined(true);
                                return new BaseComponent[]{c};
                            }
                        })
                        .add(new SubCommand("load","bungeeplugman.load","load <file>") {
                            public BaseComponent[] Iexecute(CommandSender sender, String[] args) throws CommandException {
                                if(args.length == 0)throw invalidUsage();
                                String filename = Joiner.on(" ").join(args);
                                if(!filename.endsWith(".jar"))throw new CommandException("File must be a jar",this);
                                String filepath = "plugins" + File.separator + filename;
                                File file = new File(filepath);
                                if(!file.isFile())throw new CommandException("File must be a jar",this);
                                Plugin plugin = plugman.load(file);
                                plugman.enable(plugin);
                                TextComponent c = new TextComponent("Plugin loaded, check console for errors");
                                c.setColor(ChatColor.GREEN);
                                c.setBold(true);
                                c.setUnderlined(true);
                                return new BaseComponent[]{c};
                            }
                        })
                        .build(),
                "bplugman","bpm","bplugin","bmanager");
        this.plugman = plugman;
    }

    public BungeePlugman getPlugman(){
        return plugman;
    }
}
