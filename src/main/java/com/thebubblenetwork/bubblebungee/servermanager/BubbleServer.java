package com.thebubblenetwork.bubblebungee.servermanager;

import com.thebubblenetwork.api.global.type.ServerType;
import com.thebubblenetwork.bubblebungee.BubbleBungee;
import de.mickare.xserver.net.XServer;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;

import java.net.InetSocketAddress;

/**
 * The Bubble Network 2016
 * BubbleBungee
 * 23/01/2016 {14:58}
 * Created January 2016
 */
public class BubbleServer{
    protected static BubbleServer create(XServer xserver,String name, InetSocketAddress address,String motd){
        ServerInfo info = ProxyServer.getInstance().constructServerInfo(name,address,motd,false);
        BubbleServer server =  new BubbleServer(info,xserver);
        ProxyServer.getInstance().getServers().put(name,info);
        BubbleBungee.getInstance().getManager().register(server);
        return server;
    }

    protected static BubbleServer create(XServer xserver, InetSocketAddress address, ServerType wrapper, int id){
        String name = wrapper.getPrefix() + String.valueOf(id);
        ServerInfo info = ProxyServer.getInstance().constructServerInfo(name,address,"",false);
        BubbleServer server =  new BubbleServer(info,xserver,wrapper,id);
        ProxyServer.getInstance().getServers().put(name,info);
        BubbleBungee.getInstance().getManager().register(server);
        return server;
    }

    private ServerInfo info;
    private XServer server;
    private int playercount = 0;
    private int id;
    private ServerType type;
    private boolean joinable = false;

    private BubbleServer(ServerInfo info,XServer server,ServerType wrapper,int id){
        this.id = id;
        this.info = info;
        type = wrapper;
        this.server = server;
    }

    protected BubbleServer(ServerInfo info,XServer server){
        this.info = info;
        this.server = server;
        try{
            type = BubbleBungee.getInstance().getManager().getType(getInfo());
            id = BubbleBungee.getInstance().getManager().getID(getInfo(),type);
        }
        catch (Exception ex){
            ex.printStackTrace();
            remove();
        }
    }

    public String getName() {
        return info.getName();
    }

    public ServerInfo getInfo(){
        return info;
    }

    public void remove(){
        if(getInfo() != null) {
            ProxyServer.getInstance().getServers().remove(getName());
            BubbleBungee.getInstance().getManager().remove(this);
        }
    }

    public int getMaxplayercount() {
        return getType().getMaxPlayers();
    }

    public int getPlayercount() {
        return playercount;
    }

    public void setPlayercount(int playercount) {
        this.playercount = playercount;
    }

    public boolean isJoinable(){
        return joinable;
    }

    public void setJoinable(boolean joinable){
        this.joinable = joinable;
    }

    protected void setInfo(ServerInfo info){
        if(info.getAddress() != getInfo().getAddress())throw new IllegalArgumentException("Address may not change");
        if(info.getName().equalsIgnoreCase(getName())){
            this.info = info;
            ProxyServer.getInstance().getServers().put(getName(),getInfo());
        }
    }

    public int getId() {
        return id;
    }

    public void setId(int id) throws Exception{
        this.id = id;
        setInfo(ProxyServer.getInstance().constructServerInfo(getType().getPrefix() + String.valueOf(id),getInfo().getAddress(),getInfo().getMotd(),false));
    }

    public ServerType getType(){
        return type;
    }

    public void setType(ServerType type) throws Exception{
        this.type = type;
        setInfo(ProxyServer.getInstance().constructServerInfo(type.getPrefix() + String.valueOf(getId()),getInfo().getAddress(),getInfo().getMotd(),false));
    }

    public XServer getServer(){
        return server;
    }

    @Override
    public boolean equals(Object o){
        if(getName() != null) {
            if (o instanceof BubbleServer) {
                BubbleServer server = (BubbleServer) o;
                return getName().equalsIgnoreCase(server.getName());
            }
            if (o instanceof String) {
                String s = (String) o;
                return getName().equalsIgnoreCase(s);
            }
            if (o instanceof ServerInfo) {
                ServerInfo info = (ServerInfo) o;
                return getName().equalsIgnoreCase(info.getName());
            }
        }
        return false;
    }
}
