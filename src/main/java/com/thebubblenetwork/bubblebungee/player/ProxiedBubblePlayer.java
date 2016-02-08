package com.thebubblenetwork.bubblebungee.player;

import com.thebubblenetwork.api.global.data.PlayerData;
import com.thebubblenetwork.api.global.player.BubblePlayer;
import com.thebubblenetwork.api.global.player.BubblePlayerObject;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.UUID;

/**
 * Copyright Statement
 * ----------------------
 * Copyright (C) The Bubble Network, Inc - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Wrote by Jacob Evans <jacobevansminor@gmail.com>, 01 2016
 * <p/>
 * <p/>
 * Class information
 * ---------------------
 * Package: com.thebubblenetwork.bubblebungee.player
 * Date-created: 28/01/2016 16:33
 * Project: BubbleBungee
 */

public class ProxiedBubblePlayer extends BubblePlayerObject<ProxiedPlayer> implements BubblePlayer<ProxiedPlayer>{

    public static ProxiedBubblePlayer getObject(UUID u){
        return (ProxiedBubblePlayer) getPlayerObjectMap().get(u);
    }

    public static ProxiedBubblePlayer getObject(String name){
        for(BubblePlayer player:getPlayerObjectMap().values()){
            if(player instanceof ProxiedBubblePlayer){
                ProxiedBubblePlayer proxiedBubblePlayer = (ProxiedBubblePlayer)player;
                try{
                    if(proxiedBubblePlayer.getName().equalsIgnoreCase(name))return proxiedBubblePlayer;
                }
                catch (UnsupportedOperationException ex){
                }
            }
        }
        return null;
    }

    public ProxiedBubblePlayer(UUID u, PlayerData data) {
        super(u, data);
    }

    private String name;

    public void setName(String name){
        this.name = name;
    }

    public String getName(){
        if(name == null){
            if(getPlayer() != null)setName(getPlayer().getName());
            else throw new UnsupportedOperationException("Name is not set");
        }
        return name;
    }
}
