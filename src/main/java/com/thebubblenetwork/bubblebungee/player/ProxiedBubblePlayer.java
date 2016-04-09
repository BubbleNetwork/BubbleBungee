package com.thebubblenetwork.bubblebungee.player;

import com.sun.istack.internal.Nullable;
import com.thebubblenetwork.api.global.bubblepackets.messaging.MessageType;
import com.thebubblenetwork.api.global.data.InvalidBaseException;
import com.thebubblenetwork.api.global.data.PlayerData;
import com.thebubblenetwork.api.global.data.PunishmentData;
import com.thebubblenetwork.api.global.java.DateUTIL;
import com.thebubblenetwork.api.global.player.BubblePlayer;
import com.thebubblenetwork.bubblebungee.BubbleBungee;
import com.thebubblenetwork.bubblebungee.command.commands.PartyCommand;
import com.thebubblenetwork.bubblebungee.party.Party;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.sql.SQLException;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

import static com.thebubblenetwork.bubblebungee.BubbleListener.BANMSG;

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
    private PunishmentData punishmentData;

    public PunishmentData getPunishmentData() {
        return punishmentData;
    }

    public ProxiedBubblePlayer(UUID u, PlayerData data, PunishmentData punishmentData) {
        this(u, data.getRaw(), punishmentData);
    }

    public ProxiedBubblePlayer(UUID u, Map<String, String> data, PunishmentData punishmentData){
        super(u, data);
        this.punishmentData = punishmentData;
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
            getPunishmentData().save(PunishmentData.table, "uuid", getUUID());
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
            if (PartyCommand.isPartyBoolean(getPlayer(), this.party)) {
                if(this.party.isLeader(getPlayer())) {
                    this.party.disband(getNickName() + " disbanded the party");
                }
                else if (this.party.isMember(getPlayer())) {
                    this.party.removeMember(getPlayer(), getNickName() + " left the party");
                }
            }
        }
        this.party = party;
    }

    public void unban(boolean silent){
        getPunishmentData().remove(PunishmentData.BANNED);
        getPunishmentData().remove(PunishmentData.BANTIME);
        getPunishmentData().remove(PunishmentData.BANREASON);
        getPunishmentData().remove(PunishmentData.BANBY);
        finishChanges();
        if(!silent) {
            ProxyServer.getInstance().broadcast(new ComponentBuilder("[Ban] ")
                    .color(ChatColor.RED)
                    .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(ChatColor.RED + "This is a ban message")))
                    .append(getNickName() + " was unbanned")
                    .color(ChatColor.GOLD)
                    .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(ChatColor.GOLD + "This player was unbanned")))
                    .create()
            );
        }
    }

    public void ban(@Nullable Date unbanby, String reason, String by){
        getPunishmentData().set(PunishmentData.BANNED, true);
        if(unbanby != null)getPunishmentData().set(PunishmentData.BANTIME, unbanby.getTime());
        getPunishmentData().set(PunishmentData.BANREASON, reason);
        getPunishmentData().set(PunishmentData.BANBY, by);
        finishChanges();
        ProxyServer.getInstance().broadcast(new ComponentBuilder("[Ban] ")
                .color(ChatColor.RED)
                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(ChatColor.RED + "This is a ban message")))
                .append(getNickName() + " was banned")
                .color(ChatColor.GOLD)
                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(ChatColor.RED + "Reason: " + ChatColor.GOLD + reason + "\n" + ChatColor.RED + "By: " + ChatColor.GOLD +  by + "\n" + ChatColor.RED + "Expires: " + ChatColor.GOLD + (unbanby == null ? "never" : DateUTIL.formatDateDiff(unbanby.getTime())))))
                .create()
        );
        if(isOnline()){
            getPlayer().disconnect(TextComponent.fromLegacyText(String.format(BANMSG,reason,unbanby == null ? "never" : DateUTIL.formatDateDiff(unbanby.getTime()) ,by)));
        }
    }

    public boolean isBanned(){
        try{
            if(getPunishmentData().getBoolean(PunishmentData.BANNED)){
                if(getUnbanDate() != null && getUnbanDate().before(new Date())){
                    unban(true);
                }
                else return true;
            }
        }
        catch (InvalidBaseException ex){
        }
        return false;
    }

    public String getBanReason(){
        try{
            return getPunishmentData().getString(PunishmentData.BANREASON);
        }
        catch (InvalidBaseException e){
            return null;
        }
    }

    public String getBannedBy(){
        try{
            return getPunishmentData().getString(PunishmentData.BANBY);
        }
        catch (InvalidBaseException e){
            return null;
        }
    }

    public Date getUnbanDate(){
        try {
            return new Date(getPunishmentData().getNumber(PunishmentData.BANTIME).longValue());
        } catch (InvalidBaseException e) {
            return null;
        }
    }

    public void mute(@Nullable  Date unmuteby, String reason, String by) {
        getPunishmentData().set(PunishmentData.MUTED, true);
        if(unmuteby != null)getPunishmentData().set(PunishmentData.MUTETIME, unmuteby.getTime());
        getPunishmentData().set(PunishmentData.MUTEREASON, reason);
        getPunishmentData().set(PunishmentData.MUTEBY, by);
        finishChanges();
        ProxyServer.getInstance().broadcast(new ComponentBuilder("[Mute] ")
                .color(ChatColor.RED)
                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(ChatColor.RED + "This is a mute message")))
                .append(getNickName() + " was muted")
                .color(ChatColor.GOLD)
                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(ChatColor.RED + "Reason: " + ChatColor.GOLD + reason + "\n" + ChatColor.RED + "By: " + ChatColor.GOLD +  by + "\n" + ChatColor.RED + "Expires: " + ChatColor.GOLD + (unmuteby == null ? "never" : DateUTIL.formatDateDiff(unmuteby.getTime())))))
                .create()
        );
        if(isOnline()){
            getPlayer().sendMessage(new ComponentBuilder("[Mute] ")
                    .color(ChatColor.RED)
                    .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(ChatColor.RED + "This is a mute message")))
                    .append("You were muted")
                    .color(ChatColor.GOLD)
                    .create());
        }
    }

    public void unmute(boolean silent) {
        getPunishmentData().remove(PunishmentData.MUTED);
        getPunishmentData().remove(PunishmentData.MUTETIME);
        getPunishmentData().remove(PunishmentData.MUTEREASON);
        getPunishmentData().remove(PunishmentData.MUTEBY);
        finishChanges();
        if(!silent) {
            ProxyServer.getInstance().broadcast(new ComponentBuilder("[Mute] ")
                    .color(ChatColor.RED)
                    .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(ChatColor.RED + "This is a mute message")))
                    .append(getNickName() + " was unmuted")
                    .color(ChatColor.GOLD)
                    .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(ChatColor.GOLD + "This player was unmuted")))
                    .create()
            );
        }
        if(isOnline()){
            getPlayer().sendMessage(new ComponentBuilder("[Mute] ")
                    .color(ChatColor.RED)
                    .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(ChatColor.RED + "This is a mute message")))
                    .append("You are no longer muted")
                    .color(ChatColor.GOLD)
                    .create());
        }
    }

    public boolean isMuted() {
        try{
            if(getPunishmentData().getBoolean(PunishmentData.MUTED)){
                if(getUnmuteDate() != null && getUnmuteDate().before(new Date())){
                    unmute(true);
                }
                else return true;
            }
        }
        catch (InvalidBaseException ex){
        }
        return false;
    }

    public String getMuteReason(){
        try{
            return getPunishmentData().getString(PunishmentData.MUTEREASON);
        }
        catch (InvalidBaseException e){
            return null;
        }
    }

    public String getMutedBy(){
        try{
            return getPunishmentData().getString(PunishmentData.MUTEBY);
        }
        catch (InvalidBaseException e){
            return null;
        }
    }

    public Date getUnmuteDate(){
        try {
            return new Date(getPunishmentData().getNumber(PunishmentData.MUTETIME).longValue());
        } catch (InvalidBaseException e) {
            return null;
        }
    }

    public void setPunishmentData(Map<String, String> rawPunishmentData) {
        getPunishmentData().getRaw().clear();
        for (Map.Entry<String, String> e : rawPunishmentData.entrySet()) {
            getPunishmentData().getRaw().put(e.getKey(), e.getValue());
        }
    }
}
