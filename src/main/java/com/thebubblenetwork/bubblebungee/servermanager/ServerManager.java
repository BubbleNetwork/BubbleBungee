package com.thebubblenetwork.bubblebungee.servermanager;

import com.thebubblenetwork.api.global.type.ServerType;
import com.thebubblenetwork.bubblebungee.IBubbleBungee;
import de.mickare.xserver.net.XServer;
import net.md_5.bungee.api.config.ServerInfo;

import java.net.InetSocketAddress;
import java.util.*;

/**
 * The Bubble Network 2016
 * BubbleBungee
 * 09/01/2016 {11:04}
 * Created January 2016
 */

public class ServerManager {
    private IBubbleBungee bungee;

    private Set<BubbleServer> servers = new HashSet<>();

    public ServerManager(IBubbleBungee bungee){
        this.bungee = bungee;
    }

    protected IBubbleBungee getBungee(){
        return bungee;
    }

    public ServerType getType(ServerInfo info) throws Exception{
        for(ServerType wrapper: ServerType.getTypes()){
            if(info.getName().startsWith(wrapper.getPrefix()))return wrapper;
        }
        throw new Exception("No servertype found for " + info.getName());
    }

    public int getID(ServerInfo info,ServerType wrapper) throws Exception{
        try{
            return Integer.parseInt(info.getName().replace(wrapper.getPrefix(),""));
        }
        catch(NumberFormatException ex){
            throw new Exception("Could not format information",ex);
        }
    }

    public BubbleServer getServer(String name){
        for(BubbleServer server:servers)if(server.getName().equalsIgnoreCase(name))return server;
        return null;
    }

    public Set<BubbleServer> getServers(){
        return servers;
    }

    public BubbleServer load(XServer xserver,ServerInfo info){
        BubbleServer server = new BubbleServer(info,xserver);
        servers.add(server);
        return server;
    }

    public BubbleServer create(XServer server,ServerType wrapper,int id){
        InetSocketAddress address = new InetSocketAddress(server.getHost(),Integer.parseInt(server.getName())+10000);
        return BubbleServer.create(server,address,wrapper,id);
    }

    protected void register(BubbleServer server){
        if(!servers.contains(server))servers.add(server);
    }

    protected void remove(BubbleServer server){
        servers.remove(server);
    }

    public BubbleServer getAvailble(ServerType type,boolean joinable,boolean playercount){
        for(BubbleServer server:servers){
            if (type == server.getType() && (!joinable || server.isJoinable()) && (!playercount || server.getPlayercount() < server.getMaxplayercount())) {
                return server;
            }
        }
        return null;
    }


    public int getNewID(ServerType type){
        int i = 0;
        BubbleServer server;
        do{
            i++;
            server = getServer(type,i);
        }
        while(server != null);
        return i;
    }

    public ServerType getNeeded(){
        Map<ServerType,Integer> map = new HashMap<>();
        for(ServerType type:ServerType.getTypes()){
            map.put(type,0);
        }
        for(BubbleServer server:getServers()){
            map.put(server.getType(),map.get(server.getType())+1);
        }
        List<ServerType> needed = new ArrayList<>();
        List<ServerType> softneeded = new ArrayList<>();
        for(ServerType type:ServerType.getTypes()){
            int current = map.get(type);
            if(type.getLowlimit() > current){
                needed.add(type);
            }
            else if(type.getHighlimit() < current){
                softneeded.add(type);
            }
        }
        if(needed.size() > 0){
            Collections.shuffle(needed);
            return needed.get(0);
        }
        if(softneeded.size() > 0){
            Collections.shuffle(softneeded);
            return softneeded.get(0);
        }
        throw null;
    }

    public BubbleServer getServer(XServer xserver){
        for(BubbleServer server:servers)
            if(server.getServer() == xserver)return server;
        return null;
    }

    public BubbleServer getServer(ServerType type,int id){
        for(BubbleServer server:servers)
            if(server.getType() == type && server.getId() == id)return server;
        return null;
    }

    public BubbleServer getServer(ServerInfo info){
        for(BubbleServer server:servers){
            if(server.getInfo().getAddress() == info.getAddress())return server;
        }
        return null;
    }
}
