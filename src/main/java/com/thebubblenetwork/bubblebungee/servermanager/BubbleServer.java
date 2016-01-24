package com.thebubblenetwork.bubblebungee.servermanager;

import com.sun.xml.internal.messaging.saaj.util.ByteOutputStream;
import com.thebubblenetwork.bubblebungee.BubbleBungee;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * The Bubble Network 2016
 * BubbleBungee
 * 23/01/2016 {14:58}
 * Created January 2016
 */
public class BubbleServer{
    protected static BubbleServer create(String name, InetSocketAddress address,String motd){
        ServerInfo info = ProxyServer.getInstance().constructServerInfo(name,address,motd,false);
        BubbleServer server =  new BubbleServer(info);
        ProxyServer.getInstance().getServers().put(name,info);
        BubbleBungee.getInstance().getManager().register(server);
        return server;
    }

    private ServerInfo info;
    private int playercount = 0,maxplayercount = 0,minplayercount = 0;
    private int id;
    private ServerTypeWrapper type;

    protected BubbleServer(ServerInfo info){
        this.info = info;
        try{
            type = ServerType.getType(getInfo());
            id = ServerType.getID(getInfo(),type);
        }
        catch (Exception ex){
            ex.printStackTrace();
            remove();
            return;
        }
        ByteOutputStream stream = new ByteOutputStream();
        DataOutputStream out = new DataOutputStream(stream);
        try {
            out.writeUTF("Assign");
            out.write(getId());
        } catch (IOException e) {
            e.printStackTrace();
        }
        getInfo().sendData("BubbleServer",stream.getBytes());
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

    public int getMinplayercount() {
        return minplayercount;
    }

    public void setMinplayercount(int minplayercount) {
        this.minplayercount = minplayercount;
    }

    public int getMaxplayercount() {
        return maxplayercount;
    }

    public void setMaxplayercount(int maxplayercount) {
        this.maxplayercount = maxplayercount;
    }

    public int getPlayercount() {
        return playercount;
    }

    public void setPlayercount(int playercount) {
        this.playercount = playercount;
    }

    public void setInfo(ServerInfo info) {
        if(info.getName().equalsIgnoreCase(getName())){
            this.info = info;
            ProxyServer.getInstance().getServers().put(getName(),getInfo());
            try{
                type = ServerType.getType(getInfo());
                id = ServerType.getID(getInfo(),type);
            }
            catch (Exception ex){
                ex.printStackTrace();
                remove();
            }
        }
    }

    public int getId() {
        return id;
    }

    public void setId(int id){
        setInfo(ProxyServer.getInstance().constructServerInfo(getType().getPrefix() + String.valueOf(id),getInfo().getAddress(),getInfo().getMotd(),false));
    }

    public ServerTypeWrapper getType(){
        return type;
    }

    public void setType(ServerTypeWrapper type){
        setInfo(ProxyServer.getInstance().constructServerInfo(type.getPrefix() + String.valueOf(getId()),getInfo().getAddress(),getInfo().getMotd(),false));
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
