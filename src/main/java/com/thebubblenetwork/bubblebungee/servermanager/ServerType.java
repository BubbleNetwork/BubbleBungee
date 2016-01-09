package com.thebubblenetwork.bubblebungee.servermanager;

import net.md_5.bungee.api.config.ServerInfo;

import java.util.HashSet;
import java.util.Set;

/**
 * The Bubble Network 2016
 * BubbleBungee
 * 09/01/2016 {11:12}
 * Created January 2016
 */

public class ServerType implements ServerTypeWrapper{
    public static ServerTypeWrapper
            LOBBY = registerType(new ServerType("Lobby","L")),
            SKYFORTRESS = registerType(new ServerType("SkyFortress","SKF"));



    private static Set<ServerTypeWrapper> types = new HashSet<>();

    public static ServerTypeWrapper registerType(ServerTypeWrapper type){
        types.add(type);
        return type;
    }

    public ServerTypeWrapper getType(String name) throws Exception{
        for(ServerTypeWrapper wrapper:types){
            if(wrapper.getName().equals(name))return wrapper;
        }
        throw new Exception(name + " is not a correct servertype");
    }

    public static ServerTypeWrapper getType(ServerInfo info) throws Exception{
        for(ServerTypeWrapper wrapper:types){
            if(info.getName().startsWith(wrapper.getPrefix()))return wrapper;
        }
        throw new Exception("No servertype found for " + info.getName());
    }

    public static int getID(ServerInfo info,ServerTypeWrapper wrapper) throws Exception{
        try{
            return Integer.parseInt(info.getName().replace(wrapper.getPrefix(),""));
        }
        catch(NumberFormatException ex){
            throw new Exception("Could not format information",ex);
        }
    }


    private String name,prefix;

    private ServerType(String name, String prefix) {
        this.name = name;
        this.prefix = prefix;
    }

    public String getName() {
        return name;
    }

    public String getPrefix() {
        return prefix;
    }
}
