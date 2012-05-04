// Bukkit Plugin "ToolBox" by Siguza
// This software is distributed under the following license:
// http://creativecommons.org/licenses/by-nc-sa/3.0/

package net.drgnome.toolbox;

import java.util.*;
import java.lang.reflect.*;
import java.util.logging.Logger;

import net.minecraft.server.*;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.entity.Player;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.command.CommandSender;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;

import static net.drgnome.toolbox.Lang.*;

public class Util
{
    public static final String LS = System.getProperty("line.separator");
    public static final String separator[] = {new String(new char[]{(char)17}), new String(new char[]{(char)18}), new String(new char[]{(char)19}), new String(new char[]{(char)20})};
    public static Logger log = Logger.getLogger("Minecraft");
    public static boolean economyDisabled = false;
    private static Economy economy;
    private static Permission perms;
    
    public static boolean initPerms()
    {
        RegisteredServiceProvider perm = Bukkit.getServer().getServicesManager().getRegistration(Permission.class);
        if(perm != null)
        {
            log.warning(lang("tb.missperm"));
            return false;
        }
        perms = (Permission)perm.getProvider();
        return true;
    }
    
    public static boolean hasPermission(String username, String permission)
    {
        return perms == null ? false : perms.has((String)null, username, permission);
    }
    
    public static boolean hasPermission(String[] groups, String permission)
    {
        for(int i = 0; i < groups.length; i++)
        {
            if(perms.groupHas((String)null, groups[i], permission))
            {
                return true;
            }
        }
        return false;
    }
    
    public static String[] getPlayerGroups(String username)
    {
        return perms == null ? new String[0] : perms.getPlayerGroups((String)null, username);
    }
    
    public static String[] getPlayerGroups(CommandSender sender)
    {
        return perms == null ? new String[0] : perms.getPlayerGroups((CraftPlayer)sender);
    }
    
    public static boolean initEconomy()
    {
        if(economyDisabled)
        {
            return true;
        }
        RegisteredServiceProvider eco = Bukkit.getServer().getServicesManager().getRegistration(Economy.class);
        if(eco == null)
        {
            log.warning(lang("tb.misseco"));
            return false;
        }
        economy = (Economy)eco.getProvider();
        return true;
    }
    
    public static boolean moneyHas(String username, double amount)
    {
        // Don't use more RAM than necessary
        if(economyDisabled || (amount == 0.0D))
        {
            return true;
        }
        if(economy == null)
        {
            return false;
        }
        return economy.has(username, amount);
    }
    
    public static void moneyTake(String username, double amount)
    {
        // Don't use more RAM than necessary
        if(economyDisabled || (amount == 0.0D) || (economy == null))
        {
            return;
        }
        economy.withdrawPlayer(username, amount);
    }
    
    public static boolean moneyHasTake(String username, double amount)
    {
        if(moneyHas(username, amount))
        {
            moneyTake(username, amount);
            return true;
        }
        return false;
    }
    
    // This method saves a lot of code
    public static int tryParse(String s, int i)
    {
        try
        {
            return Integer.parseInt(s);
        }
        catch(Exception e)
        {
            return i;
        }
    }
    
    public static boolean isInt(String s)
    {
        try
        {
            Integer.parseInt(s);
            return true;
        }
        catch(Exception e)
        {
            return false;
        }
    }
    
    public static String smoothDouble(double d, int digits)
    {
        if(digits > 0)
        {
            String temp = "" + (int)Math.round(d * Math.pow(10, digits));
            if(digits > temp.length())
            {
                digits = temp.length();
            }
            return (digits == temp.length() ? "0" : "") + temp.substring(0, temp.length() - digits) + "." + temp.substring(temp.length() - digits, temp.length());
        }
        return "" + d;
    }
    
    // These 3 methods split up strings into multiple lines so that the message doesn't get messed up by the minecraft chat.
    // You can also give a prefix that is set before every line.
    public static void sendMessage(CommandSender sender, String message)
    {
        sendMessage(sender, message, "");
    }
    
    public static void sendMessage(CommandSender sender, String message, ChatColor prefix)
    {
        sendMessage(sender, message, "" + prefix);
    }
    
    public static void sendMessage(CommandSender sender, String message, String prefix)
    {
        if((sender == null) || (message == null))
        {
            return;
        }
        if(prefix == null)
        {
            prefix = "";
        }
        int offset = 0;
        int xpos = 0;
        int pos = 0;
        String part;
        while(true)
        {
            if(offset + 60 >= message.length())
            {
                sender.sendMessage(prefix + message.substring(offset, message.length()));
                break;
            }
            part = message.substring(offset, offset + 60);
            xpos = part.lastIndexOf(" ");
            pos = xpos < 0 ? 60 : xpos;
            part = message.substring(offset, offset + pos);
            sender.sendMessage(prefix + part);
            offset += pos + (xpos < 0 ? 0 : 1);
        }
    }
    
    // These 2 methods: Access private functions on classes OR PARENT CLASSES
    public static Object invoke(Object o, String m, Object... params)
    {
        return invoke(o.getClass(), o, m, params);
    }
    
    public static Object invoke(Class<?> c, Object o, String m, Object... params)
    {
        if(c == null)
        {
            return null;
        }
        Method methods[] = c.getDeclaredMethods();
        for(int i = 0; i < methods.length; i++)
        {
            if(methods[i].getName().equals(m))
            {
                try
                {
                    methods[i].setAccessible(true);
                    return methods[i].invoke(o, params);
                }
                catch(Exception e)
                {
                }
            }
        }
        return invoke(c.getSuperclass(), o, m, params);
    }
    
    // Before e.printStackTrace:
    public static void warn()
    {
        log.warning("[ToolBox] AN ERROR OCCURED! PLEASE SEND THE MESSAGE BELOW TO THE DEVELOPER!");
    }
}