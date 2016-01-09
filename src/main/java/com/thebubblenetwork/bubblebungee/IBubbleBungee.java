package com.thebubblenetwork.bubblebungee;

import com.thebubblenetwork.bubblebungee.servermanager.ServerManager;
import net.md_5.bungee.api.plugin.Plugin;

/**
 * The Bubble Network 2016
 * BubbleBungee
 * 09/01/2016 {11:06}
 * Created January 2016
 */

public interface IBubbleBungee{
    String getVersion();
    Plugin getPlugin();
    ServerManager getManager();
}
