package com.thebubblenetwork.bubblebungee.player;

import com.thebubblenetwork.api.global.data.InvalidBaseException;
import com.thebubblenetwork.api.global.data.PlayerData;
import com.thebubblenetwork.api.global.player.BubblePlayer;
import com.thebubblenetwork.bubblebungee.BubbleBungee;
import com.thebubblenetwork.bubblebungee.party.Party;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.sql.SQLException;
import java.util.Map;
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

public class ProxiedBubblePlayer extends BubblePlayer<ProxiedPlayer>{

    public static ProxiedBubblePlayer getObject(UUID u) {
        return (ProxiedBubblePlayer) getPlayerObjectMap().get(u);
    }

    public static ProxiedBubblePlayer getObject(String name) {
        for (BubblePlayer player : getPlayerObjectMap().values()) {
            if (player instanceof ProxiedBubblePlayer) {
                ProxiedBubblePlayer proxiedBubblePlayer = (ProxiedBubblePlayer) player;
                try {
                    if (proxiedBubblePlayer.getName().equalsIgnoreCase(name)) {
                        return proxiedBubblePlayer;
                    }
                } catch (UnsupportedOperationException ex) {
                }
            }
        }
        for (BubblePlayer player : getPlayerObjectMap().values()) {
            if (player instanceof ProxiedBubblePlayer) {
                ProxiedBubblePlayer proxiedBubblePlayer = (ProxiedBubblePlayer) player;
                try {
                    if (proxiedBubblePlayer.getNickName() != null && proxiedBubblePlayer.getNickName().equalsIgnoreCase(name)) {
                        return proxiedBubblePlayer;
                    }
                } catch (UnsupportedOperationException ex) {
                }
            }
        }
        return null;
    }

    private String name;
    private Party party = null;

    public ProxiedBubblePlayer(UUID u, PlayerData data) {
        super(u, data.getRaw());
    }

    public ProxiedBubblePlayer(UUID u, Map<String, String> data){
        super(u, data);
    }

    public String getName() {
        if (name == null) {
            if (getPlayer() != null) {
                setName(getPlayer().getName());
            } else {
                try {
                    setName(getData().getString(PlayerData.NAME));
                } catch (InvalidBaseException e) {
                    throw new UnsupportedOperationException(e);
                }
            }
        }
        return name;
    }

    public void setName(String name) {
        this.name = name;
        getData().set(PlayerData.NAME, name);
    }

    public boolean isOnline(){
        return getPlayerObjectMap().containsKey(getUUID());
    }

    @Override
    protected void update() {
        BubbleBungee.getInstance().updatePlayer(this);
    }

    protected void save() {
        try {
            getData().save(PlayerData.table, "uuid", getUUID());
        } catch (SQLException | ClassNotFoundException e) {
            BubbleBungee.getInstance().logSevere(e.getMessage());
            BubbleBungee.getInstance().logSevere("Could not save data of " + getName());
        }
    }

    public Party getParty() {
        return party;
    }

    public void setParty(Party party) {
        if (this.party != null) {
            if (this.party.isLeader(getPlayer())) {
                this.party.disband(getNickName() + " disbanded the party");
            } else if (this.party.isMember(getPlayer())) {
                this.party.removeMember(getPlayer(), getNickName() + " left the party");
            } else if (this.party.isInvited(getPlayer())) {
                this.party.cancelInvite(getPlayer(), getNickName() + " denied the invite");
            }
        }
        this.party = party;
    }
}
