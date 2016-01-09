package com.thebubblenetwork.bubblebungee.servermanager;

import com.thebubblenetwork.bubblebungee.IBubbleBungee;

/**
 * The Bubble Network 2016
 * BubbleBungee
 * 09/01/2016 {11:04}
 * Created January 2016
 */

public class ServerManager {
    private IBubbleBungee bungee;

    protected IBubbleBungee getBungee(){
        return bungee;
    }

    public ServerManager(IBubbleBungee bungee){
        this.bungee = bungee;
    }
}
