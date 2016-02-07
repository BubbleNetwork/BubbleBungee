package com.thebubblenetwork.bubblebungee.servermanager;

import com.thebubblenetwork.api.global.type.ServerType;
import com.thebubblenetwork.api.global.type.ServerTypeObject;
import com.thebubblenetwork.bubblebungee.IBubbleBungee;
import de.mickare.xserver.net.XServer;
import net.md_5.bungee.api.config.ServerInfo;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.Set;

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
        for(ServerType wrapper: ServerTypeObject.getTypes()){
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

    public BubbleServer load(XServer xserver,ServerInfo info){
        BubbleServer server = new BubbleServer(info,xserver);
        servers.add(server);
        return server;
    }

    public BubbleServer create(XServer server,ServerType wrapper,int id){
        InetSocketAddress address = new InetSocketAddress(server.getHost(),server.getPort());
        return BubbleServer.create(server,address,wrapper,id);
    }

    protected void register(BubbleServer server){
        if(!servers.contains(server))servers.add(server);
    }

    protected void remove(BubbleServer server){
        servers.remove(server);
    }

    public int getNewID(ServerType wrapper){
        int i = 0;
        for(BubbleServer server:servers){
            if(server.getType() == wrapper)i++;
        }
        return i+1;
    }

    public ServerType getNeeded(){
        try {
            return ServerTypeObject.getType("Lobby");
        } catch (Exception e) {
            getBungee().logSevere(e.getMessage());
        }
        return null;
    }

    public BubbleServer getServer(XServer xserver){
        for(BubbleServer server:servers)
            if(server.getServer() == xserver)return server;
        return null;
    }
}
