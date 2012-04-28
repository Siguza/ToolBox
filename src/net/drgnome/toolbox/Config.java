// Bukkit Plugin "ToolBox" by Siguza
// This software is distributed under the following license:
// http://creativecommons.org/licenses/by-nc-sa/3.0/

package net.drgnome.toolbox;

import java.io.*;
import java.util.*;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.*;

import static net.drgnome.toolbox.Lang.*;
import static net.drgnome.toolbox.Util.*;

// Thought for static import
public class Config
{    
    private static FileConfiguration config;
    
    // Because reloadConfig is already used
    public static void reloadConf(FileConfiguration file)
    {
        config = file;
        setDefs();
    }
    
    // Set all default values
    private static void setDefs()
    {
        setDef("economy-disabled", "false");
        setDef("uf.tools", "278,285");
        setDef("uf.buy", "50000");
        setDef("uf.use", "0");
        setDef("hammer.tools", "278,285");
        setDef("hammer.buy", "500000");
        setDef("hammer.use", "0");
        setDef("hammer.maxradius", "3");
        setDef("lb.tools", "271,275,258,286,279");
        setDef("lb.buy", "20000");
        setDef("lb.use", "0.2");
        setDef("lb.maxradius", "10");
        setDef("invpick.buy", "200000");
        setDef("invpick.use", "0");
        setDef("repair.use", "15");
    }
    
    // Set a default value
    private static void setDef(String path, String value)
    {
        if(!config.isSet(path))
        {
            config.set(path, value);
        }
    }
    
    public static String getConfigString(String string)
    {
        return config.getString(string);
    }
    
    public static int getConfigInt(String prefix, String suffix, CommandSender sender, boolean max)
    {
        String groups[] = getPlayerGroups(sender);
        return getConfigInt(prefix, suffix, groups, max);
    }
    
    public static int getConfigInt(String prefix, String suffix, String groups[], boolean max)
    {
        int value = getConfigInt(prefix + "." + suffix);
        int tmp;
        for(int i = 0; i < groups.length; i++)
        {
            if(!config.isSet(prefix + "." + groups[i] + "." + suffix))
            {
                continue;
            }
            tmp = getConfigInt(prefix + "." + groups[i] + "." + suffix);
            if(((max) && (tmp > value)) || ((!max) && (tmp < value)))
            {
                value = tmp;
            }
        }
        return value;
    }
    
    public static int getConfigInt(String string)
    {
        try
        {
            return Integer.parseInt(config.getString(string));
        }
        catch(Exception e)
        {
            try
            {
                return (int)Math.round(Double.parseDouble(config.getString(string)));
            }
            catch(Exception e2)
            {
                return 0;
            }
        }
    }
    
    public static double getConfigDouble(String prefix, String suffix, CommandSender sender, boolean max)
    {
        return getConfigDouble(prefix, suffix, sender, max, 0);
    }
    
    public static double getConfigDouble(String prefix, String suffix, CommandSender sender, boolean max, int digits)
    {
        String groups[] = getPlayerGroups(sender);
        return getConfigDouble(prefix, suffix, groups, max, digits);
    }
    
    public static double getConfigDouble(String prefix, String suffix, String groups[], boolean max)
    {
        return getConfigDouble(prefix, suffix, groups, max, 0);
    }
    
    public static double getConfigDouble(String prefix, String suffix, String groups[], boolean max, int digits)
    {
        double value = getConfigDouble(prefix + "." + suffix, digits);
        double tmp;
        for(int i = 0; i < groups.length; i++)
        {
            if(!config.isSet(prefix + "." + groups[i] + "." + suffix))
            {
                continue;
            }
            tmp = getConfigDouble(prefix + "." + groups[i] + "." + suffix, digits);
            if(((max) && (tmp > value)) || ((!max) && (tmp < value)))
            {
                value = tmp;
            }
        }
        return value;
    }
    
    public static double getConfigDouble(String string, int digits)
    {
        try
        {
            return Double.parseDouble(smoothDouble(Double.parseDouble(config.getString(string)), digits));
        }
        catch(Exception e)
        {
            return 0;
        }
    }
    
    public static boolean getConfigIsInList(String search, String prefix, String suffix, CommandSender sender, boolean max)
    {
        String groups[] = getPlayerGroups(sender);
        return getConfigIsInList(search, prefix, suffix, groups, max);
    }
    
    public static boolean getConfigIsInList(String search, String prefix, String suffix, String groups[], boolean max)
    {
        search = search.toLowerCase();
        String val = config.getString(prefix + "." + suffix);
        if(val != null)
        {
            boolean inList = false;
            String values[] = val.trim().toLowerCase().split(",");
            for(int j = 0; j < values.length; j++)
            {
                if((values[j].equals(search)) || (values[j].equals("*")))
                {
                    inList = true;
                }
            }
            if(max == inList)
            {
                return inList;
            }
            for(int i = 0; i < groups.length; i++)
            {
                val = config.getString(prefix + "." + groups[i] + "." + suffix);
                if(val != null)
                {
                    values = val.trim().toLowerCase().split(",");
                    for(int j = 0; j < values.length; j++)
                    {
                        if((values[j].equals(search)) || (values[j].equals("*")))
                        {
                            inList = true;
                        }
                    }
                    if(max == inList)
                    {
                        return inList;
                    }
                }
            }
        }
        return false;
    }
    
    public static String getConfigList(String prefix, String suffix, CommandSender sender)
    {
        String groups[] = getPlayerGroups(sender);
        return getConfigList(prefix, suffix, groups);
    }
    
    public static String getConfigList(String prefix, String suffix, String groups[])
    {
        String val = config.getString(prefix + "." + suffix);
        if(val != null)
        {
            ArrayList<String> list = new ArrayList<String>();
            String values[] = val.trim().toLowerCase().split(",");
            for(int j = 0; j < values.length; j++)
            {
                if(values[j].equals("*"))
                {
                    return lang("everything");
                }
                list.add(values[j]);
            }
            for(int i = 0; i < groups.length; i++)
            {
                val = config.getString(prefix + "." + groups[i] + "." + suffix);
                if(val != null)
                {
                    values = val.trim().toLowerCase().split(",");
                    for(int j = 0; j < values.length; j++)
                    {
                        if(values[j].equals("*"))
                        {
                            return lang("everything");
                        }
                        list.add(values[j]);
                    }
                }
            }
            String all[] = list.toArray(new String[0]);
            if(all.length > 0)
            {
                String ret = all[0];
                for(int i = 1; i < all.length; i++)
                {
                    ret += "," + all[i];
                }
                return ret;
            }
        }
        return lang("nothing");
    }
}