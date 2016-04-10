package com.thebubblenetwork.bubblebungee.servermanager;

import com.thebubblenetwork.api.global.bubblepackets.PacketInfo;
import com.thebubblenetwork.api.global.type.ServerType;
import com.thebubblenetwork.bubblebungee.BubbleBungee;
import de.mickare.xserver.net.XServer;
import io.netty.util.internal.ConcurrentSet;
import net.md_5.bungee.api.Callback;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.config.ServerInfo;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

/**
 * The Bubble Network 2016
 * BubbleBungee
 * 09/01/2016 {11:04}
 * Created January 2016
 */

public class ServerManager implements Runnable{
    private BubbleBungee bungee;

    private List<BubbleServer> servers = Collections.synchronizedList(new ArrayList<BubbleServer>());
    private Set<PacketInfo> unassigned = Collections.synchronizedSet(new HashSet<PacketInfo>());

    public ServerManager(BubbleBungee bungee) {
        this.bungee = bungee;
        getBungee().getPlugin().getProxy().getScheduler().schedule(getBungee().getPlugin(),this,15L, TimeUnit.SECONDS);
    }

    public void run(){
        Iterator<BubbleServer> iterator = getServers().iterator();
        while(iterator.hasNext()){
            final BubbleServer current = iterator.next();
            if(!current.getServer().isConnected())iterator.remove();
            current.getInfo().ping(new Callback<ServerPing>() {
                public void done(ServerPing serverPing, Throwable throwable) {
                    if(serverPing == null)getBungee().getLogger().log(Level.SEVERE, current.getName() + " could not ping", throwable);
                }
            });
        }
    }

    protected BubbleBungee getBungee() {
        return bungee;
    }

    public ServerType getType(ServerInfo info) {
        for (ServerType wrapper : ServerType.getTypes()) {
            if (info.getName().startsWith(wrapper.getPrefix())) {
                return wrapper;
            }
        }
        throw new IllegalArgumentException("No servertype found for " + info.getName());
    }

    public int getID(ServerInfo info, ServerType wrapper) {
        try {
            return Integer.parseInt(info.getName().replace(wrapper.getPrefix(), ""));
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Could not format information", ex);
        }
    }

    public BubbleServer getServer(String name) {
        for (BubbleServer server : servers) {
            if (server.getName().equalsIgnoreCase(name)) {
                return server;
            }
        }
        return null;
    }

    public Set<PacketInfo> getUnassigned() {
        return unassigned;
    }

    public List<BubbleServer> getServers() {
        return servers;
    }

    public BubbleServer load(XServer xserver, ServerInfo info) {
        removeUnassigned(xserver);
        BubbleServer server = new BubbleServer(info, xserver);
        servers.add(server);
        return server;
    }

    public void removeUnassigned(XServer server) {
        Iterator<PacketInfo> infoIterator = getUnassigned().iterator();
        while (infoIterator.hasNext()) {
            PacketInfo packetInfo = infoIterator.next();
            if (packetInfo.getServer().getName().equals(server.getName())) {
                infoIterator.remove();
            }
        }
    }

    public BubbleServer create(XServer server, ServerType wrapper, int id) {
        removeUnassigned(server);
        InetSocketAddress address = new InetSocketAddress(getBungee().getBungeeeproperties().getString("ip"), server.getPort() + 10000);
        return BubbleServer.create(server, address, wrapper, id);
    }

    protected void register(BubbleServer server) {
        if (!servers.contains(server)) {
            servers.add(server);
        }
    }

    protected void remove(BubbleServer server) {
        servers.remove(server);
    }

    public BubbleServer getAvailble(ServerType type,int needed, boolean joinable, boolean playercount) {
        if (type == null) {
            throw new IllegalArgumentException("Type cannot be null");
        }
        for (BubbleServer server : getServers()) {
            if (server.getType() != null && type.getName().equals(server.getType().getName()) && (!joinable || server.isJoinable()) && (!playercount || server.getPlayercount() + needed <= server.getMaxplayercount())) {
                return server;
            }
        }
        return null;
    }


    public int getNewID(ServerType type) {
        Set<Integer> integerSet = new HashSet<>();
        for(BubbleServer server:servers){
            if(server.getType() == type){
                integerSet.add(server.getId());
            }
        }
        for(int i = 1;true;i ++){
            if(!integerSet.contains(i)){
                return i;
            }
        }
    }

    public ServerType getNeeded() throws UneededException {
        Map<String, Integer> map = new HashMap<>();
        for (ServerType type : ServerType.getTypes()) {
            map.put(type.getName(), 0);
        }
        for (BubbleServer server : getServers()) {
            if (server.getType() != null) {
                map.put(server.getType().getName(), map.get(server.getType().getName()) + 1);
            } else {
                getBungee().getPlugin().getLogger().log(Level.WARNING, "{0} doesn't have a server type!", new Object[]{server.getServer().getName()});
            }
        }
        List<ServerType> needed = new ArrayList<>();
        List<ServerType> softneeded = new ArrayList<>();
        for (ServerType type : ServerType.getTypes()) {
            int current = map.get(type.getName());
            if (type.getLowlimit() > current) {
                needed.add(type);
            }
            if (type.getHighlimit() > current) {
                softneeded.add(type);
            }
        }
        if (!needed.isEmpty()) {
            Collections.shuffle(needed);
            return needed.get(0);
        }
        if (!softneeded.isEmpty()) {
            Collections.shuffle(softneeded);
            return softneeded.get(0);
        }
        throw new UneededException();
    }

    public BubbleServer getServer(XServer xserver) {
        for (BubbleServer server : servers) {
            if (server.getServer().getName().equals(xserver.getName())) {
                return server;
            }
        }
        return null;
    }

    public BubbleServer getServer(ServerType type, int id) {
        for (BubbleServer server : servers) {
            if (server.getType() == type && server.getId() == id) {
                return server;
            }
        }
        return null;
    }

    public BubbleServer getServer(ServerInfo info) {
        for (BubbleServer server : servers) {
            if (server.getInfo().getAddress() == info.getAddress()) {
                return server;
            }
        }
        return null;
    }

    public static class UneededException extends Exception {
        public UneededException() {

        }
    }
}
