package com.thebubblenetwork.bubblebungee;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.thebubblenetwork.api.global.bubblepackets.messaging.messages.handshake.RankDataUpdate;
import com.thebubblenetwork.api.global.bubblepackets.messaging.messages.response.PlayerDataResponse;
import com.thebubblenetwork.api.global.data.PlayerData;
import com.thebubblenetwork.api.global.data.PunishmentData;
import com.thebubblenetwork.api.global.data.RankData;
import com.thebubblenetwork.api.global.file.PropertiesFile;
import com.thebubblenetwork.api.global.player.BubblePlayer;
import com.thebubblenetwork.api.global.plugin.BubbleHub;
import com.thebubblenetwork.api.global.ranks.Rank;
import com.thebubblenetwork.api.global.sql.SQLUtil;
import com.thebubblenetwork.bubblebungee.command.ICommand;
import com.thebubblenetwork.bubblebungee.command.commands.*;
import com.thebubblenetwork.bubblebungee.player.ProxiedBubblePlayer;
import com.thebubblenetwork.bubblebungee.servermanager.BubbleServer;
import com.thebubblenetwork.bubblebungee.servermanager.ServerManager;
import de.mickare.xserver.XServerPlugin;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ConfigurationAdapter;
import net.md_5.bungee.api.config.ListenerInfo;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The Bubble Network 2016
 * BubbleBungee
 * 09/01/2016 {10:59}
 * Created January 2016
 */

public class BubbleBungee extends BubbleHub<Plugin> implements ConfigurationAdapter{

    private static final int VERSION = 12;

    public static BubbleBungee getInstance() {
        return instance;
    }

    public static void setInstance(BubbleBungee instance) {
        BubbleBungee.instance = instance;
    }

    private static BubbleBungee instance;
    private ServerManager manager;
    private BubbleListener listener = new BubbleListener(this);
    private P plugin;
    private BungeePlugman pluginManager;
    private File file;
    private boolean lockdown = true;
    private String lockdownmsg = ChatColor.RED + "The server is currently locked down";
    private PropertiesFile bungeeeproperties;

    public BubbleBungee(P plugin) {
        super();
        this.plugin = plugin;
    }

    public void onBubbleEnable() {
        setInstance(this);

        file = getPlugin().getFile();

        logInfo("Loading ranks...");

        try {
            loadRanks();
        } catch (Exception e) {
            getLogger().log(Level.WARNING,"Failed to load ranks",e);
            endSetup("Failed to load Ranks...");
        }

        logInfo("Loaded ranks");
        
        logInfo("loading PlayerData table...");

        try {
            loadPlayerDataTable();
        } catch (Exception e) {
            getLogger().log(Level.WARNING,"Could not load PlayerData table",e);
            endSetup("Failed to load PlayerData table...");
        }

        logInfo("Loaded PlayerData table");

        logInfo("loading Punishments table...");

        try {
            loadPunishmentsTable();
        } catch (Exception e) {
            getLogger().log(Level.WARNING, "Could not load Punishments table", e);
            endSetup("Failed to load Punishments table...");
        }

        logInfo("Loaded Punishments table");

        logInfo("Setting up components");

        manager = new ServerManager(this);
        getPacketHub().registerListener(listener);

        getPlugin().getProxy().getPluginManager().registerListener(getPlugin(), listener);

        logInfo("Components are set up");

        logInfo("Creating commands");

        registerCommand(new PlugmanCommand(getPlugman()));
        registerCommand(new ReloadCommand("b", getPlugman()));
        registerCommand(new FriendCommand());
        registerCommand(new RankCommand());
        registerCommand(new WhoisCommand());
        registerCommand(new TokenCommand());
        registerCommand(new MessageCommand());
        registerCommand(new SetTokenCommand());
        registerCommand(new PartyCommand());
        registerCommand(new HelpCommand());
        registerCommand(new LockdownCommand());
        registerCommand(new RegisterCommand());
        registerCommand(new BanCommand());
        registerCommand(new UnbanCommand());
        registerCommand(new MuteCommand());
        registerCommand(new UnmuteCommand());

        logInfo("Commands have been created");

        logInfo("Finished setup");

        getPlugin().getProxy().getServers().remove("FakeServer");
    }

    public void registerCommand(ICommand command) {
        if (command instanceof Command) {
            getPlugin().getProxy().getPluginManager().registerCommand(getPlugin(), (Command) command);
        } else {
            throw new IllegalArgumentException(command.getClass().getName() + " is not a bungeecord command!");
        }
    }

    public void onBubbleDisable() {
        for (Rank r : Rank.getRanks()) {
            try {
                r.getData().save("ranks", "rank", r.getName());
            } catch (SQLException | ClassNotFoundException e) {
                getLogger().log(Level.WARNING,"Error saving rank " + r.getName(),e);
            }
        }

        setInstance(null);
        manager = null;
    }

    public Map<String, String> loadPunishmentData(UUID load) throws SQLException, ClassNotFoundException{
        return PunishmentData.loadData(SQLUtil.query(getConnection(), PunishmentData.table, "*", new SQLUtil.WhereVar("uuid", load)));
    }

    private PlayerData loadData(UUID load) throws SQLException, ClassNotFoundException {
        return new PlayerData(loadDataRaw(load));
    }

    private Map<String, String> loadDataRaw(UUID load) throws SQLException, ClassNotFoundException{
        return PlayerData.loadData(SQLUtil.query(getConnection(), PlayerData.table, "*", new SQLUtil.WhereVar("uuid", load)));
    }

    public ServerManager getManager() {
        return manager;
    }

    public P getPlugin() {
        return plugin;
    }

    public void saveXServerDefaults() {

        logInfo("Finding XServer folders...");

        File xserverfoler = new File("plugins" + File.separator + "XServerProxy");
        if (!xserverfoler.exists()) {
            logInfo("Creating XServer folder");
            xserverfoler.mkdir();
        }

        logInfo("Finding XServer configuration...");

        File xserverconfig = new File(xserverfoler + File.separator + "config.yml");
        if (!xserverconfig.exists()) {
            try {
                xserverconfig.createNewFile();
            } catch (IOException ex) {
                logSevere(ex.getMessage());
                endSetup("Could not create XServer configuration");
            }
        }

        logInfo("Loading XServer configuration");

        Configuration c;
        try {
            c = YamlConfiguration.getProvider(YamlConfiguration.class).load(xserverconfig);
        } catch (IOException e) {
            getLogger().log(Level.WARNING,"Could not load XServer config",e);
            endSetup("Could not load XServer config");
            return;
        }
        c.set("servername", "proxy");
        c.set("mysql.User", getConnection().getUser());
        c.set("mysql.Pass", getConnection().getPassword());
        c.set("mysql.Data", getConnection().getDatabase());
        c.set("mysql.Host", getConnection().getHostname());
        c.set("mysql.Port", getConnection().getPort());
        c.set("mysql.TableXServers", "xserver_servers");
        c.set("mysql.TableXGroups", "xserver_groups");
        c.set("mysql.TableXServersGroups", "xserver_servergroups");

        logInfo("Loaded XServer configuration");

        logInfo("Saving XServer configuration");

        try {
            YamlConfiguration.getProvider(YamlConfiguration.class).save(c, xserverconfig);
        } catch (IOException e) {
            logSevere(e.getMessage());
            endSetup("Could not save XServer config");
        }

        logInfo("Saved XServer configuration");
    }

    public void onBubbleLoad() {
        pluginManager = new BungeePlugman(getPlugin().getProxy());
        getPlugin().getProxy().setConfigurationAdapter(this);
        getPlugin().getProxy().setReconnectHandler(listener);
    }

    public ProxiedPlayer getPlayer(UUID uuid) {
        return getPlugin().getProxy().getPlayer(uuid);
    }

    public void updateRank(Rank r) {
        RankDataUpdate update = new RankDataUpdate(r.getName(), r.getData().getRaw());
        for (BubbleServer server : getManager().getServers()) {
            try {
                getPacketHub().sendMessage(server.getServer(), update);
            } catch (IOException e) {
                getLogger().log(Level.WARNING,"Error sending rank update message to " + server,e);
            }
        }
    }

    public void updatePlayer(BubblePlayer player) {
        PlayerDataResponse response = new PlayerDataResponse(player.getName(), player.getData().getRaw());
        ProxiedPlayer proxiedPlayer = getPlugin().getProxy().getPlayer(player.getUUID());
        if (proxiedPlayer != null) {
            ServerInfo info = proxiedPlayer.getServer().getInfo();
            BubbleServer server = getManager().getServer(info);
            if (server != null) {
                try {
                    getPacketHub().sendMessage(server.getServer(), response);
                } catch (IOException e) {
                    getLogger().log(Level.WARNING,"Could not send player update",e);
                }
            }
        }
    }

    public void stop(){
        getPlugin().getProxy().stop();
    }

    public Logger getLogger() {
        if (getPlugin() != null) {
            return getPlugin().getLogger();
        }
        return ProxyServer.getInstance().getLogger();
    }

    public void logInfo(String s) {
        getLogger().log(Level.INFO,s);
    }

    public void logSevere(String s) {
        getLogger().log(Level.WARNING,s);
    }

    public void runTaskLater(Runnable runnable, long l, TimeUnit unit) {
        getPlugin().getProxy().getScheduler().schedule(getPlugin(), runnable, l, unit);
    }

    public void loadRanks() throws SQLException, ClassNotFoundException {
        Rank.getRanks().clear();
        if(!SQLUtil.tableExists(getConnection(),"ranks")){
            getLogger().log(Level.INFO,"Rank table does not exist, creating...");
            getConnection().executeSQL(
                    "CREATE TABLE `ranks` (" +
                    "`rank` VARCHAR(32) NOT NULL DEFAULT 'default'," +
                    "`value` TEXT NOT NULL," +
                    "`key` TEXT NOT NULL," +
                    "INDEX `rank` (`rank`)" +
                    ");");
            RankData defaultrank = new RankData(new HashMap<String,String>());
            defaultrank.set("default",true);
            Rank.loadRank("default",defaultrank.getRaw());
            getLogger().log(Level.INFO,"Created ranks table");
        }
        ResultSet set = SQLUtil.query(getConnection(), "ranks", "*", new SQLUtil.Where("1"));
        Map<String, Map<String, String>> map = new HashMap<>();
        while (set.next()) {
            String rankname = set.getString("rank");
            Map<String, String> currentmap = map.containsKey(rankname) ? map.get(rankname) : new HashMap<String, String>();
            currentmap.put(set.getString("key"), set.getString("value"));
            map.put(rankname, currentmap);
        }
        set.close();
        for (Map.Entry<String, Map<String, String>> entry : map.entrySet()) {
            Rank.loadRank(entry.getKey(), entry.getValue());
            logInfo("Loaded rank: " + entry.getKey());
        }
    }
    
    public void loadPlayerDataTable() throws SQLException, ClassNotFoundException {
        //check if the playerdata table exists
        if (!SQLUtil.tableExists(getConnection(), "playerdata")) {

            //create the playerdata table
            getLogger().log(Level.INFO, "PlayerData table does not exist, creating...");
            getConnection().executeSQL(
                    "CREATE TABLE `playerdata` (" +
                    "`uuid` VARCHAR(36) NOT NULL," +
                    "`value` TEXT NOT NULL," +
                    "`key` TEXT NOT NULL," +
                    "INDEX `UUID KEY` (`uuid`)," +
                    ");");

            //log successful creation
            getLogger().log(Level.INFO, "PlayerData table created successfully!");
        }
    }

    public void loadPunishmentsTable() throws SQLException, ClassNotFoundException {

        //check if the punishments table exists
        if (!SQLUtil.tableExists(getConnection(), "punishments")) {

            //create the punishments table
            getConnection().executeSQL(
                    "CREATE TABLE `punishments` (" +
                    "`uuid` VARCHAR(36) NOT NULL," +
                    "`value` TEXT NOT NULL," +
                    "`key` TEXT NOT NULL," +
                    "INDEX `UUID KEY` (`uuid`)," +
                    ");");

            //log successful createion
            getLogger().log(Level.INFO, "Punishments table created successfully!");

        }

    }

    public XServerPlugin getXPlugin() {
        Plugin p = getPlugin().getProxy().getPluginManager().getPlugin("XServerProxy");
        if (p == null) {
            endSetup("Could not find XServerProxy");
        }
        return (XServerPlugin) p;
    }

    public BubbleListener getListener() {
        return listener;
    }

    public File getReplace() {
        return file;
    }

    public String getArtifact() {
        return getPlugin().getDescription().getName();
    }

    public int getVersion() {
        return VERSION;
    }

    public boolean bungee() {
        return true;
    }

    public BungeePlugman getPlugman() {
        return pluginManager;
    }

    public void update(Runnable r) {
        runTaskLater(r, 1L, TimeUnit.SECONDS);
    }

    public void updateTaskBefore() {
        getPlugman().unload(getPlugin());
    }

    public void updateTaskAfter() {
        getPlugman().load(file);
    }

    public UUID getUUID(String name) {
        ProxiedPlayer player;
        if ((player = ProxyServer.getInstance().getPlayer(name)) != null) {
            return player.getUniqueId();
        }
        for(char c:name.toCharArray()){
            Preconditions.checkArgument( Character.isSpaceChar(c) || Character.isLetterOrDigit(c) || c == '_',"Must be Alphanumeric");
        }
        //QUERY
        String text = "SELECT `uuid` FROM `" + PlayerData.table + "` WHERE `key`=\"%key%\" AND `value`=\"" + name + "\"";
        ResultSet set = null;
        UUID u = null;
        try {
            //Querying for nickname
            set = getConnection().querySQL(text.replace("%key%",PlayerData.NICKNAME));

            //Checking for UUID
            if(set.next()){
                try{
                    u = UUID.fromString(set.getString("uuid"));
                }
                catch (Exception ex){
                }
                if(u != null){
                    return u;
                }
            }
            //Closing SQL
            set.close();

            //Querying for name
            set = getConnection().querySQL(text.replace("%key%",PlayerData.NAME));

            //Checking for UUID
            if(set.next()){
                try{
                    u = UUID.fromString(set.getString("uuid"));
                }
                catch (Exception ex){
                }
                if(u != null){
                    return u;
                }
            }

        } catch (SQLException|ClassNotFoundException ex) {
            //Debug
            BubbleBungee.getInstance().getLogger().log(Level.WARNING,"Error getting SQL data for " + name,ex);
        }
        finally {
            //Making sure statement and resultset are closed
            if (set != null) {
                try {
                    set.close();
                } catch (Exception ex) {

                }
            }
        }
        return null;
    }

    public boolean isLockdown() {
        return lockdown;
    }

    public void setLockdown(boolean lockdown) {
        this.lockdown = lockdown;
    }

    public String getLockdownmsg() {
        return lockdownmsg;
    }

    public void setLockdownmsg(String lockdownmsg) {
        this.lockdownmsg = lockdownmsg;
    }

    public void load() {
        File config = new File("config.properties");
        if(!config.exists()){
            try {
                PropertiesFile.generateFresh(config, new String[]{"ip","port","bind","max_players","tab_size","ping_passthrough","query_enabled"},new String[]{"localhost","25565","true","1000","60","false","true"});
            } catch (Exception e) {
                throw new IllegalArgumentException(e);
            }
        }
        try {
            bungeeeproperties = new PropertiesFile(config);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    public int getInt(String s, int i) {
        switch (s){
            case "network_compression_threshold":
                return 256;
            case "player_limit":
                return -1;
            case "connection_throttle":
                return 4000;
            case "timeout":
                return 8000;
        }
        throw new IllegalArgumentException("Could not find \'" + s + "\'");
    }

    public String getString(String s, String s1) {
        switch (s){
            case "stats":
                return "a4fdab31-7c74-456a-b5fa-8b3f908ae0f9";
        }
        throw new IllegalArgumentException("Could not find \'" + s + "\'");
    }

    public boolean getBoolean(String s, boolean b) {
        switch (s){
            case "log_commands":
                return false;
            case "online_mode":
            case "ip_forward":
                return true;
        }
        throw new IllegalArgumentException("Could not find \'" + s + "\'");
    }

    public Collection<?> getList(String s, Collection<?> collection) {
        return new ArrayList<>();
    }

    public Map<String, ServerInfo> getServers() {
        return new ImmutableMap.Builder<String, ServerInfo>().put("FakeServer", getPlugin().getProxy().constructServerInfo("FakeServer", new InetSocketAddress("localhost",30000),"FakeServer",true)).build();
    }

    public Collection<ListenerInfo> getListeners() {
        try {
            String ip = bungeeeproperties.getString("ip");
            int port = bungeeeproperties.getNumber("port").intValue();
            boolean bind = bungeeeproperties.getString("bind").equalsIgnoreCase("true");
            int maxplayers = bungeeeproperties.getNumber("max_players").intValue();
            int tabsize = bungeeeproperties.getNumber("tab_size").intValue();
            boolean pingpass = bungeeeproperties.getString("ping_passthrough").equalsIgnoreCase("true");
            boolean query = bungeeeproperties.getString("query_enabled").equalsIgnoreCase("true");
            ListenerInfo info = new ListenerInfo(new InetSocketAddress(ip, port), ChatColor.BLUE + "BubbleNetwork", maxplayers, tabsize, new ArrayList<String>(), false, new HashMap<String, String>(), "GLOBAL", bind, pingpass, port, query);
            return Collections.singletonList(info);
        }
        catch (Exception ex){
            throw new IllegalArgumentException("Failed to get objects",ex);
        }
    }

    public Collection<String> getGroups(String s) {
        return new ArrayList<>();
    }

    public Collection<String> getPermissions(String s) {
        return new ArrayList<>();
    }

    public PropertiesFile getBungeeeproperties() {
        return bungeeeproperties;
    }

    public ProxiedBubblePlayer getBubblePlayer(UUID u) throws SQLException, ClassNotFoundException{
        //UUID cannot be null
        Preconditions.checkNotNull(u);
        //If the player is cached return that
        if(ProxiedBubblePlayer.getPlayerObjectMap().containsKey(u)){
            return ProxiedBubblePlayer.getObject(u);
        }
        //return a loaded instance
        return new ProxiedBubblePlayer(u, loadDataRaw(u), new PunishmentData(loadPunishmentData(u)));
    }
}
