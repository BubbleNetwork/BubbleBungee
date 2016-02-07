package com.thebubblenetwork.bubblebungee;

import com.thebubblenetwork.api.global.data.DataObject;
import com.thebubblenetwork.api.global.data.PlayerData;
import com.thebubblenetwork.api.global.plugin.BubbleHubObject;
import com.thebubblenetwork.api.global.ranks.Rank;
import com.thebubblenetwork.api.global.sql.SQLUtil;
import com.thebubblenetwork.api.global.type.ServerTypeObject;
import com.thebubblenetwork.bubblebungee.servermanager.ServerManager;
import de.mickare.xserver.XServerPlugin;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * The Bubble Network 2016
 * BubbleBungee
 * 09/01/2016 {10:59}
 * Created January 2016
 */

public class BubbleBungee extends BubbleHubObject<Plugin,ProxiedPlayer> implements IBubbleBungee{

    private static IBubbleBungee instance;

    public static IBubbleBungee getInstance() {
        return instance;
    }

    public static void setInstance(IBubbleBungee instance) {
        BubbleBungee.instance = instance;
    }

    private ServerManager manager;
    private BubbleListener listener;
    private P plugin;

    public BubbleBungee(P plugin){
        super();
        this.plugin = plugin;
    }

    public void onBubbleEnable(){
        setInstance(this);

        logInfo("Loading ranks...");

        try {
            loadRanks();
        } catch (Exception e) {
            logSevere(e.getMessage());
            endSetup("Failed to setup map");
        }

        logInfo("Loaded ranks");


        logInfo("Setting up components");

        manager = new ServerManager(this);
        listener = new BubbleListener(this);
        getPacketHub().registerListener(listener);
        getPlugin().getProxy().getPluginManager().registerListener(getPlugin(),listener);

        logInfo("Finished setup");
    }

    public void onBubbleDisable(){
        setInstance(null);
        manager = null;
    }



    public PlayerData loadData(UUID load) throws SQLException, ClassNotFoundException {
        return new PlayerData(DataObject.loadData(SQLUtil.query(getConnection(), PlayerData.table, "*",
                new SQLUtil.WhereVar("uuid", load))));
    }

    public ServerManager getManager(){
        return manager;
    }

    public P getPlugin(){
        return plugin;
    }

    public void saveXServerDefaults() {

        logInfo("Finding XServer folders...");

        File xserverfoler = new File("plugins" + File.separator + "XServerProxy");
        if(!xserverfoler.exists()){
            logInfo("Creating XServer folder");
            xserverfoler.mkdir();
        }

        logInfo("Finding XServer configuration...");

        File xserverconfig = new File(xserverfoler + File.separator + "config.yml");
        if(!xserverconfig.exists()){
            try{
                xserverconfig.createNewFile();
            }
            catch (IOException ex){
                logSevere(ex.getMessage());
                endSetup("Could not create XServer configuration");
            }
        }

        logInfo("Loading XServer configuration");

        Configuration c;
        try {
            c = YamlConfiguration.getProvider(YamlConfiguration.class).load(xserverconfig);
        } catch (IOException e) {
            logSevere(e.getMessage());
            endSetup("Could not load XServer config");
            return;
        }
        c.set("servername","proxy");
        c.set("mysql.User",getConnection().getUser());
        c.set("mysql.Pass",getConnection().getPassword());
        c.set("mysql.Data",getConnection().getDatabase());
        c.set("mysql.Host",getConnection().getHostname());
        c.set("mysql.Port",getConnection().getPort());
        c.set("mysql.TableXServers", "xserver_servers");
        c.set("mysql.TableXGroups", "xserver_groups");
        c.set("mysql.TableXServersGroups", "xserver_servergroups");

        logInfo("Loaded XServer configuration");

        logInfo("Saving XServer configuration");

        try {
            YamlConfiguration.getProvider(YamlConfiguration.class).save(c,xserverconfig);
        } catch (IOException e) {
            logSevere(e.getMessage());
            endSetup("Could not save XServer config");
        }

        logInfo("Saved XServer configuration");
    }

    public void onBubbleLoad() {

    }

    public ProxiedPlayer getPlayer(UUID uuid) {
        return getPlugin().getProxy().getPlayer(uuid);
    }

    public void endSetup(String s) throws RuntimeException {
        getPlugin().getProxy().stop(s);
        throw new RuntimeException(s);
    }

    public Logger getLogger(){
        if(getPlugin() != null)return getPlugin().getLogger();
        return null;
    }

    public void logInfo(String s) {
        Logger l = getLogger();
        if(l != null)l.info(s);
        else System.out.println("[BubbleBungee] " + s);
    }

    public void logSevere(String s) {
        Logger l = getLogger();
        if(l != null)l.severe(s);
        else System.err.println("[BubbleBungee] " + s);
    }

    public void runTaskLater(Runnable runnable,long l,TimeUnit unit) {
        getPlugin().getProxy().getScheduler().schedule(getPlugin(),runnable,l,unit);
    }

    public void loadRanks() throws SQLException, ClassNotFoundException {
        Rank.getRanks().clear();
        ResultSet set = SQLUtil.query(BubbleHubObject.getInstance().getConnection(), "ranks", "*", new SQLUtil.Where("1"));
        Map<String, Map> map = new HashMap<>();
        while (set.next()) {
            String rankname = set.getString("rank");
            Map currentmap = map.containsKey(rankname) ? map.get(rankname) : new HashMap();
            currentmap.put(set.getObject("key"), set.getObject("value"));
            map.put(rankname, currentmap);
        }
        set.close();
        for (Map.Entry<String, Map> entry : map.entrySet()) {
            Rank.loadRank(entry.getKey(),entry.getValue());
        }
    }

    public XServerPlugin getXPlugin() {
        Plugin p = getPlugin().getProxy().getPluginManager().getPlugin("XServerProxy");
        if(p == null)endSetup("Could not find XServerProxy");
        return (XServerPlugin) p;
    }

    public boolean bungee(){
        return true;
    }
}
