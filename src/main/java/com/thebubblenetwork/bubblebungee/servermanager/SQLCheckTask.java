package com.thebubblenetwork.bubblebungee.servermanager;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.thebubblenetwork.bubblebungee.BubbleBungee;
import com.thebubblenetwork.bubblebungee.IBubbleBungee;
import com.thebubblenetwork.bubblebungee.sql.SQLConnection;
import com.thebubblenetwork.bubblebungee.sql.SQLUtil;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;

import java.net.InetSocketAddress;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * The Bubble Network 2016
 * BubbleBungee
 * 23/01/2016 {20:34}
 * Created January 2016
 */
public class SQLCheckTask implements Runnable{
    private static final String table = "serverupdate",name = "name",ip = "ip",port = "port",motd = "motd",playercount = "playercount",min = "mincount",max = "maxcount";

    private IBubbleBungee bungee;

    public SQLCheckTask(IBubbleBungee bungee){
        this.bungee = bungee;
        try {
            if(SQLUtil.tableExists(getBungee().getConnection(),table)){
                SQLUtil.createTable(getBungee().getConnection(),table,
                        new ImmutableMap.Builder<String,Map.Entry<SQLUtil.SQLDataType,Integer>>()
                                .put("action",new AbstractMap.SimpleImmutableEntry<>(SQLUtil.SQLDataType.TEXT,10))
                                .put("variable",new AbstractMap.SimpleEntry<>(SQLUtil.SQLDataType.TEXT,-1)).build()
                );
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        getBungee().getProxy().getScheduler().runAsync(getBungee().getPlugin(),this);
    }

    public void run() {
        try {
            ResultSet r = SQLUtil.query(getBungee().getConnection(),table,"*",new SQLUtil.Where("1"));
            while(r.next()){
                Action action = Action.getAction(r.getString("action"));
                String variable = r.getString("variable");
                r.deleteRow();
                if(action != null && variable != null)action.process(this,variable);
            }
            r.close();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        ProxyServer.getInstance().getScheduler().schedule(BubbleBungee.getInstance().getPlugin(),this,50L, TimeUnit.MILLISECONDS);
    }

    protected IBubbleBungee getBungee(){
        return bungee;
    }

    enum Action{
        REMOVE,ADD,CHANGE;

        static Action getAction(String s){
            for(Action action:Action.values()){
                if(action.toString().equalsIgnoreCase(s))return action;
            }
            return null;
        }

        void process(SQLCheckTask task,String variable){
            if(this == REMOVE){
                BubbleServer server = task.getBungee().getManager().getServer(variable);
                if(server != null)server.remove();
            }
            else if(this == ADD){
                Map<String,Object> info = SQLUtil.decompress(variable);
                String name = (String)info.get(SQLCheckTask.name);
                String ip = (String)info.get(SQLCheckTask.name);
                int port;
                try{
                    port = Integer.parseInt((String)info.get(SQLCheckTask.port));
                }
                catch (NumberFormatException ex){
                    ex.printStackTrace();
                    return;
                }
                if(task.getBungee().getManager().getServer(name) != null)task.getBungee().getManager().create(name,ip,port);
            }
            else if(this == CHANGE){
                Map<String,Object> changes = SQLUtil.decompress(variable);
                String serverstring = (String)changes.remove("server");
                if(serverstring != null){
                    BubbleServer server = task.getBungee().getManager().getServer(serverstring);
                    Map<String,String> strings = new HashMap<>();
                    strings.put(SQLCheckTask.name,server.getName());
                    strings.put(SQLCheckTask.ip,server.getInfo().getAddress().getAddress().getHostAddress());
                    strings.put(SQLCheckTask.port,String.valueOf(server.getInfo().getAddress().getPort()));
                    strings.put(SQLCheckTask.motd,server.getInfo().getMotd());
                    boolean modified = false;
                    for(Map.Entry<String,Object> entry:changes.entrySet()){
                        String value = (String)entry.getValue();
                        String key = entry.getKey();
                        if(key.equals(SQLCheckTask.playercount)){
                            server.setPlayercount(Integer.parseInt(value));
                        }
                        else if(key.equals(SQLCheckTask.min)){
                            server.setMaxplayercount(Integer.parseInt(value));
                        }
                        else if(key.equals(SQLCheckTask.max)){
                            server.setMinplayercount(Integer.parseInt(value));
                        }
                        else{
                            strings.put(key,value);
                            modified = true;
                        }
                    }
                    if(modified){
                        ServerInfo info = task.getBungee().getProxy().
                                constructServerInfo(
                                        strings.get(SQLCheckTask.name)
                                        ,new InetSocketAddress(strings.get(SQLCheckTask.ip), Integer.parseInt(strings.get(SQLCheckTask.port)))
                                        ,strings.get(SQLCheckTask.motd),false);
                        server.setInfo(info);
                    }
                }
            }
        }
    }
}
