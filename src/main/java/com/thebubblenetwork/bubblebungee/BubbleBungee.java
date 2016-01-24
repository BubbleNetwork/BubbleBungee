package com.thebubblenetwork.bubblebungee;

import com.thebubblenetwork.bubblebungee.file.PropertiesFile;
import com.thebubblenetwork.bubblebungee.servermanager.ServerManager;
import com.thebubblenetwork.bubblebungee.sql.SQLConnection;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.plugin.Plugin;

import java.io.File;

/**
 * The Bubble Network 2016
 * BubbleBungee
 * 09/01/2016 {10:59}
 * Created January 2016
 */

public class BubbleBungee extends Plugin implements IBubbleBungee{

    private static IBubbleBungee instance;
    private static final File propertiesfilepath = new File("bubblebungee.properties");

    public static IBubbleBungee getInstance() {
        return instance;
    }

    public static void setInstance(IBubbleBungee instance) {
        BubbleBungee.instance = instance;
    }

    private ServerManager manager;
    private PropertiesFile file;
    private String db_ip,db_port,db_usr,db_pass,db_name;
    private SQLConnection connection;

    public void onEnable(){
        setInstance(this);
        if(!propertiesfilepath.exists()){
            try {
                PropertiesFile.generateFresh(propertiesfilepath,new String[]{
                        "database-ip","database-port","database-user","database-password","database-name"
                },new String[]{
                        "localhost","3306","NONE","root","bubbleserver"
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
            getProxy().stop("Properties File not found");
            return;
        }
        try {
            file = new PropertiesFile(propertiesfilepath);
        } catch (Exception e) {
            getProxy().stop("Properties error in loading");
            return;
        }
        db_ip = file.getString("database-ip");
        db_port = file.getString("database-port");
        db_usr = file.getString("database-user");
        db_pass = file.getString("database-password");
        if(db_pass.equals("NONE"))db_pass = null;
        db_name = file.getString("database-name");
        if(db_ip == null || db_port == null|| db_usr == null || db_name == null){
            getProxy().stop("Properties error");
            return;
        }
        connection = new SQLConnection(getDatabaseAddress(),getDatabasePort(),getDatabaseName(),getDatabaseUser(),getDatabasePassword());
        try{
            connection.openConnection();
        }
        catch (Exception ex){
            ex.printStackTrace();
            getProxy().stop("SQL Connection could not be established");
            return;
        }
        manager = new ServerManager(this);
        for(ServerInfo info:ProxyServer.getInstance().getServers().values()){
            getManager().load(info);
        }
    }

    public void onDisable(){
        setInstance(null);
        manager = null;
    }

    public ServerManager getManager(){
        return manager;
    }

    public PropertiesFile getProperties(){
        return file;
    }

    public SQLConnection getConnection(){
        return connection;
    }

    public String getVersion(){
        return getDescription().getVersion();
    }

    public BubbleBungee getPlugin(){
        return this;
    }

    public String getDatabaseName() {
        return db_name;
    }

    public String getDatabaseAddress() {
        return db_ip;
    }

    public String getDatabasePort() {
        return db_port;
    }

    public String getDatabaseUser() {
        return db_usr;
    }

    public String getDatabasePassword() {
        return db_pass;
    }
}
