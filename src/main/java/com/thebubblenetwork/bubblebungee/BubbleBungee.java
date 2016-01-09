package com.thebubblenetwork.bubblebungee;

import com.thebubblenetwork.bubblebungee.servermanager.ServerManager;
import net.md_5.bungee.api.plugin.Plugin;

/**
 * The Bubble Network 2016
 * BubbleBungee
 * 09/01/2016 {10:59}
 * Created January 2016
 */

public class BubbleBungee extends Plugin implements IBubbleBungee{

    private static IBubbleBungee instance;

    public static IBubbleBungee getInstance() {
        return instance;
    }

    public static void setInstance(IBubbleBungee instance) {
        BubbleBungee.instance = instance;
    }


    private ServerManager manager;

    public void onEnable(){
        setInstance(this);
        manager = new ServerManager(this);
    }

    public void onDisable(){
        setInstance(null);
        manager = null;
    }

    public ServerManager getManager(){
        return manager;
    }

    public String getVersion(){
        return getDescription().getVersion();
    }

    public BubbleBungee getPlugin(){
        return this;
    }
}
