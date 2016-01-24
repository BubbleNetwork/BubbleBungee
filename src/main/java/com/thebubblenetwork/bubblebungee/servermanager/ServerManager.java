package com.thebubblenetwork.bubblebungee.servermanager;

import com.thebubblenetwork.bubblebungee.IBubbleBungee;
import net.md_5.bungee.api.config.ServerInfo;

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

    protected IBubbleBungee getBungee(){
        return bungee;
    }

    public BubbleServer getServer(String name){
        for(BubbleServer server:servers)if(server.getName().equalsIgnoreCase(name))return server;
        return null;
    }

    public BubbleServer load(ServerInfo info){
        BubbleServer server = new BubbleServer(info);
        servers.add(server);
        return server;
    }

    public BubbleServer create(String name, InetSocketAddress address,String motd){
        return BubbleServer.create(name,address,motd);
    }

    public BubbleServer create(String name,InetSocketAddress address){
        return create(name,address,"");
    }

    public BubbleServer create(String name,String address,int port,String motd){
        return BubbleServer.create(name,new InetSocketAddress(address,port),motd);
    }

    public BubbleServer create(String name,String address,int port){
        return create(name,address,port,"");
    }

    protected void register(BubbleServer server){
        if(!servers.contains(server))servers.add(server);
    }

    protected void remove(BubbleServer server){
        servers.remove(server);
    }

    public ServerManager(IBubbleBungee bungee){
        this.bungee = bungee;
    }
}
