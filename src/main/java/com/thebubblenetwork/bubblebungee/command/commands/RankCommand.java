package com.thebubblenetwork.bubblebungee.command.commands;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableSet;
import com.thebubblenetwork.api.global.data.InvalidBaseException;
import com.thebubblenetwork.api.global.data.PlayerData;
import com.thebubblenetwork.api.global.data.RankData;
import com.thebubblenetwork.api.global.java.ArgTrimmer;
import com.thebubblenetwork.api.global.ranks.Rank;
import com.thebubblenetwork.api.global.sql.SQLConnection;
import com.thebubblenetwork.api.global.sql.SQLUtil;
import com.thebubblenetwork.bubblebungee.BubbleBungee;
import com.thebubblenetwork.bubblebungee.command.BaseCommand;
import com.thebubblenetwork.bubblebungee.command.CommandException;
import com.thebubblenetwork.bubblebungee.command.ICommand;
import com.thebubblenetwork.bubblebungee.command.SubCommand;
import com.thebubblenetwork.bubblebungee.player.ProxiedBubblePlayer;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;


/**
 * The Bubble Network 2016
 * BubbleBungee
 * 15/02/2016 {18:26}
 * Created February 2016
 */
public class RankCommand extends BaseCommand{
    private static UUID getUUID(String name){
        ProxiedPlayer player;
        if((player = ProxyServer.getInstance().getPlayer(name)) != null)return player.getUniqueId();
        SQLConnection connection = BubbleBungee.getInstance().getConnection();
        final String s = "`key`=\"" + PlayerData.NAME +"\" AND `value`=\"" + name + "\"";
        final String s2 = "`key`=\"" + PlayerData.NICKNAME +"\" AND `value`=\"" + name + "\"";
        ResultSet set = null;
        try{
            set = SQLUtil.query(connection, PlayerData.table,"uuid",new SQLUtil.Where(null){
                @Override
                public String getWhere() {
                    return s;
                }
            });
            if(set.next()){
                return UUID.fromString(set.getString("uuid"));
            }
            set.close();
            set = SQLUtil.query(connection, PlayerData.table,"uuid", new SQLUtil.Where(null){
                @Override
                public String getWhere(){
                    return s2;
                }
            });
            if(set.next()){
                return UUID.fromString(set.getString("uuid"));
            }
        }
        catch (SQLException |ClassNotFoundException ex){
            return null;
        }
        finally {
            if(set != null){
                try{
                    set.close();
                }
                catch (Exception ex){

                }
            }
        }
        return null;
    }

    public RankCommand() {
        super("rankmanager", "rankmanager.use",
                new ImmutableSet.Builder<ICommand>()
                        .add(new SubCommand("list","rankmanager.list","list","listranks","groups","ranks") {
                            public String Iexecute(CommandSender sender, String[] args) throws CommandException {
                                String s = ChatColor.GOLD + "Ranks: ";
                                for(Rank r:Rank.getRanks()){
                                    s += "\n" + r.getName();
                                }
                                return s;
                            }
                        })
                        .add(new SubCommand("setprefix","rankmanager.prefix","setprefix <rank> <prefix>","prefix") {
                            public String Iexecute(CommandSender sender, String[] args) throws CommandException {
                                if(args.length < 2)throw new CommandException("Invalid usage: " + getUsage(),this);
                                String rankname = args[0];
                                Rank r = Rank.getRank(rankname);
                                if(r == null){
                                    Rank.loadRank(rankname,new HashMap());
                                    r = Rank.getRank(rankname);
                                    if(r == null)throw new CommandException("Rank not found",this);
                                }
                                String prefix = Joiner.on(" ").join(new ArgTrimmer<>(String.class,args).trim(1));
                                r.setPrefix(prefix);
                                saveRank(r);
                                return ChatColor.GOLD + "Successfuly set the prefix of \'" + r.getName() + "\' to \'" + ChatColor.translateAlternateColorCodes('&',prefix) + ChatColor.GOLD + "\'";
                            }
                        })
                        .add(new SubCommand("setsuffix","rankmanager.suffix","setsuffix <rank> <suffix>","suffix") {
                            public String Iexecute(CommandSender sender, String[] args) throws CommandException {
                                if(args.length < 2)throw new CommandException("Invalid usage: " + getUsage(),this);
                                String rankname = args[0];
                                Rank r = Rank.getRank(rankname);
                                if(r == null){
                                    Rank.loadRank(rankname,new HashMap());
                                    r = Rank.getRank(rankname);
                                    if(r == null)throw new CommandException("Rank not found",this);
                                }
                                String suffix = Joiner.on(" ").join(new ArgTrimmer<>(String.class,args).trim(1));
                                r.setSuffix(suffix);
                                saveRank(r);
                                return ChatColor.GOLD + "Successfuly set the suffix of \'" + r.getName() + "\' to \'" + ChatColor.translateAlternateColorCodes('&',suffix) + ChatColor.GOLD + "\'";
                            }
                        })
                        .add(new SubCommand("setinheritance","rankmanager.inheritance","setinheritance <rank> <inheritance/none>","inheritance") {
                            public String Iexecute(CommandSender sender, String[] args) throws CommandException {
                                if(args.length < 2)throw new CommandException("Invalid usage: " + getUsage(),this);
                                String rankname = args[0];
                                Rank r = Rank.getRank(rankname);
                                if(r == null){
                                    Rank.loadRank(rankname,new HashMap());
                                    r = Rank.getRank(rankname);
                                    if(r == null)throw new CommandException("Rank not found",this);
                                }
                                String toname = args[1];
                                Rank toset;
                                if(toname.equalsIgnoreCase("none")){
                                    toname = "none";
                                    toset = null;
                                }
                                else if((toset = Rank.getRank(toname)) == null)throw new CommandException("Inheritance rank does not exist",this);
                                else toname = toset.getName();
                                r.setInheritance(toset);
                                saveRank(r);
                                return ChatColor.GOLD + "Successfuly set the inheritance of \'" + r.getName() + "\' to \'" + toname + "\'";
                            }
                        })
                        .add(new SubCommand("add","rankmanager.addpermission","add <rank> <permission>","addpermission") {
                            public String Iexecute(CommandSender sender, String[] args) throws CommandException {
                                if(args.length < 2)throw new CommandException("Invalid usage: " + getUsage(),this);
                                String rankname = args[0];
                                Rank r = Rank.getRank(rankname);
                                if(r == null){
                                    Rank.loadRank(rankname,new HashMap());
                                    r = Rank.getRank(rankname);
                                    if(r == null)throw new CommandException("Rank not found",this);
                                }
                                String permission = args[1].toLowerCase();
                                boolean haspermission = true;
                                if(permission.startsWith("-")){
                                    haspermission = false;
                                    permission = permission.substring(1);
                                }
                                if(permission.equalsIgnoreCase(RankData.PREFIX) || permission.equalsIgnoreCase(RankData.SUFFIX) || permission.equalsIgnoreCase(RankData.INHERITANCE)) {
                                    throw new CommandException("Invalid permission", this);
                                }
                                r.getData().set(permission,haspermission);
                                saveRank(r);
                                return ChatColor.GOLD + "Successfuly set \'" + permission + " (" + String.valueOf(haspermission) + ") \' on rank \'" + r.getName() + "\'";
                            }
                        })
                        .add(new SubCommand("remove","rankmanager.removepermission","remove <rank> <permission>","removepermission") {
                            public String Iexecute(CommandSender sender, String[] args) throws CommandException {
                                if(args.length < 2)throw new CommandException("Invalid usage: " + getUsage(),this);
                                String rankname = args[0];
                                Rank r = Rank.getRank(rankname);
                                if(r == null){
                                    throw new CommandException("Invalid rank",this);
                                }
                                String permission = args[1].toLowerCase();
                                if(permission.startsWith("-")){
                                    permission = permission.substring(1);
                                }
                                if(permission.equalsIgnoreCase(RankData.PREFIX) || permission.equalsIgnoreCase(RankData.SUFFIX) || permission.equalsIgnoreCase(RankData.INHERITANCE)) {
                                    throw new CommandException("Invalid permission", this);
                                }
                                try {
                                    r.getData().getBoolean(permission);
                                } catch (InvalidBaseException e) {
                                    throw new CommandException("This rank does not have that permission",this);
                                }
                                r.getData().getRaw().remove(permission);
                                saveRank(r);
                                return ChatColor.GOLD + "Successfuly removed \'" + permission + "\' on rank \'" + r.getName() + "\'";
                            }
                        })
                        .add(new SubCommand("info","rankmanager.info","info <rank>","information") {
                            public String Iexecute(CommandSender sender, String[] args) throws CommandException {
                                if(args.length == 0)throw new CommandException("Invalid usage",this);
                                String rankname = args[0];
                                Rank r = Rank.getRank(rankname);
                                if(r == null){
                                    throw new CommandException("Could not find a rank with this name",this);
                                }
                                String information = ChatColor.GOLD + "Rank information: " + ChatColor.RED + r.getName();
                                information += "\n" + ChatColor.GOLD + "Prefix: \'" + ChatColor.translateAlternateColorCodes('&',r.getPrefix()) + ChatColor.GOLD + "\'";
                                information += "\n" + ChatColor.GOLD + "Suffix: \'" + ChatColor.translateAlternateColorCodes('&',r.getSuffix()) + ChatColor.GOLD + "\'";
                                information += "\n" + ChatColor.GOLD + "Inheritance: " + (r.getInheritance() != null ? r.getInheritance().getName() : "None");
                                information += "\n" + ChatColor.GOLD + "Permissions: ";
                                for(String key:r.getData().getRaw().keySet()){
                                    if(key.equalsIgnoreCase(RankData.PREFIX) || key.equalsIgnoreCase(RankData.SUFFIX) || key.equalsIgnoreCase(RankData.INHERITANCE))continue;
                                    boolean value;
                                    try {
                                        value = r.getData().getBoolean(key);
                                    } catch (InvalidBaseException e) {
                                        continue;
                                    }
                                    information += "\n" + ChatColor.GOLD + " - " + key + " (" + String.valueOf(value) + ")";
                                }
                                return information;
                            }
                        })
                        .add(new SubCommand("whois","rankmanager.whois","whois <player>","show","who") {
                            public String Iexecute(CommandSender sender, String[] args) throws CommandException {
                                if(args.length == 0)throw new CommandException("Invalid usage",this);
                                String playername = args[0];
                                ProxiedBubblePlayer online = ProxiedBubblePlayer.getObject(playername);
                                if(online == null){
                                    UUID u = getUUID(playername);
                                    if(u == null)throw new CommandException("Player not found",this);
                                    try {
                                        online = new ProxiedBubblePlayer(u,BubbleBungee.getInstance().loadData(u));
                                    } catch (Exception e) {
                                        throw new CommandException("Player not found",this);
                                    }
                                }
                                String info = ChatColor.GOLD + "Whois: " + ChatColor.RED + online.getName();
                                info += "\n" + ChatColor.GOLD + "Nickname: " + online.getNickName();
                                info += "\n" + ChatColor.GOLD + "Tokens: " + String.valueOf(online.getTokens());
                                info += "\n" + ChatColor.GOLD + "Rank: " + online.getRank().getName();
                                info += "\n" + ChatColor.GOLD + "Subranks: ";
                                for(Rank r:online.getSubRanks()) info += r.getName();
                                return info;
                            }
                        })
                        .add(new SubCommand("setrank","rankmanager.setrank","setrank <player> <rank>","setgroup") {
                            public String Iexecute(CommandSender sender, String[] args) throws CommandException {
                                if(args.length < 2)throw new CommandException("Invalid usage",this);
                                String playername = args[0];
                                ProxiedBubblePlayer online = ProxiedBubblePlayer.getObject(playername);
                                boolean forcesave = false;
                                if(online == null){
                                    forcesave = true;
                                    UUID u = getUUID(playername);
                                    if(u == null)throw new CommandException("Player not found",this);
                                    try {
                                        online = new ProxiedBubblePlayer(u,BubbleBungee.getInstance().loadData(u));
                                    } catch (Exception e) {
                                        throw new CommandException("Player not found",this);
                                    }
                                }
                                String rankname = args[1];
                                Rank r = Rank.getRank(rankname);
                                if(r == null){
                                    throw new CommandException("Rank not found",this);
                                }
                                online.setRank(r);
                                if(forcesave)online.save();
                                return ChatColor.GOLD + "Set the rank of \'" + online.getNickName() + "\' to \'" + r.getName() + "\'";
                            }
                        })
                        .add(new SubCommand("addrank","rankmanager.addrank","addrank <player> <rank>","addgroup") {
                            public String Iexecute(CommandSender sender, String[] args) throws CommandException {
                                if(args.length < 2)throw new CommandException("Invalid usage",this);
                                String playername = args[0];
                                ProxiedBubblePlayer online = ProxiedBubblePlayer.getObject(playername);
                                boolean forcesave = false;
                                if(online == null){
                                    forcesave = true;
                                    UUID u = getUUID(playername);
                                    if(u == null)throw new CommandException("Player not found",this);
                                    try {
                                        online = new ProxiedBubblePlayer(u,BubbleBungee.getInstance().loadData(u));
                                    } catch (Exception e) {
                                        throw new CommandException("Player not found",this);
                                    }
                                }
                                String rankname = args[1];
                                Rank r = Rank.getRank(rankname);
                                if(r == null){
                                    throw new CommandException("Rank not found",this);
                                }
                                List<Rank> rankList = new ArrayList<>();
                                rankList.addAll(Arrays.asList(online.getSubRanks()));
                                if(rankList.contains(r))throw new CommandException("This player already has that subrank",this);
                                rankList.add(r);
                                online.setSubRanks(rankList);
                                if(forcesave)online.save();
                                return ChatColor.GOLD + "Added the subrank of \'" + online.getNickName() + "\' to \'" + r.getName() + "\'";
                            }
                        })
                        .add(new SubCommand("removerank","rankmanager.removerank","removerank <player> <rank>","removegroup") {
                            public String Iexecute(CommandSender sender, String[] args) throws CommandException {
                                if(args.length < 2)throw new CommandException("Invalid usage",this);
                                String playername = args[0];
                                ProxiedBubblePlayer online = ProxiedBubblePlayer.getObject(playername);
                                boolean forcesave = false;
                                if(online == null){
                                    forcesave = true;
                                    UUID u = getUUID(playername);
                                    if(u == null)throw new CommandException("Player not found",this);
                                    try {
                                        online = new ProxiedBubblePlayer(u,BubbleBungee.getInstance().loadData(u));
                                    } catch (Exception e) {
                                        throw new CommandException("Player not found",this);
                                    }
                                }
                                String rankname = args[1];
                                Rank r = Rank.getRank(rankname);
                                if(r == null){
                                    throw new CommandException("Rank not found",this);
                                }
                                List<Rank> rankList = new ArrayList<>();
                                rankList.addAll(Arrays.asList(online.getSubRanks()));
                                if(!rankList.contains(r))throw new CommandException("This player does not have that subrank",this);
                                rankList.remove(r);
                                online.setSubRanks(rankList);
                                if(forcesave)online.save();
                                return ChatColor.GOLD + "Removed the subrank of \'" + online.getNickName() + "\' from \'" + r.getName() + "\'";
                            }
                        })
                        .build()
                ,"groupmanager","pex","permissionsex","ranks","rmanager");
    }

    private static void saveRank(Rank r){
        BubbleBungee.getInstance().updateRank(r);
    }
}
