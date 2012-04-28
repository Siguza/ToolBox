// Bukkit Plugin "ToolBox" by Siguza
// This software is distributed under the following license:
// http://creativecommons.org/licenses/by-nc-sa/3.0/

package net.drgnome.toolbox;

import java.io.*;
import java.util.*;
import java.util.logging.Logger;

import net.minecraft.server.*;

import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicesManager;
import org.bukkit.configuration.file.*;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;

public abstract class TBPluginBase extends JavaPlugin implements Listener
{
    public static String version = "Beta 0.2.0";
    protected static boolean economyDisabled;
    public static Economy economy;
    public static Permission perms;
    public static FileConfiguration config;
    public static YamlConfiguration lang;
    public static final String LS = System.getProperty("line.separator");
    public static final String separator[] = {new String(new char[]{(char)17}), new String(new char[]{(char)18}), new String(new char[]{(char)19}), new String(new char[]{(char)20})};
    protected Logger log = Logger.getLogger("Minecraft");
    protected HashMap<String, ToolBox> boxes;

    @EventHandler
    public abstract void handleClickEvent(PlayerInteractEvent event);
    
    @EventHandler
    public abstract void handleBreakEvent(BlockBreakEvent event);
    
    public void onEnable()
    {
        log.info("Enabling ToolBox " + version);
        boxes = new HashMap<String, ToolBox>();
        checkFiles();
        config = getConfig();
        reloadLang();
        setDefaults();
        economyDisabled = config.getString("economy-disabled").equalsIgnoreCase("true") ? true : false;
        if(!economyDisabled)
        {
            RegisteredServiceProvider eco = getServer().getServicesManager().getRegistration(Economy.class);
            if(eco != null)
            {
                economy = (Economy)eco.getProvider();
            }
            else
            {
                log.warning(lang("tb.misseco"));
                getPluginLoader().disablePlugin(this);
                return;
            }
        }
        RegisteredServiceProvider perm = getServer().getServicesManager().getRegistration(Permission.class);
        if(perm != null)
        {
            perms = (Permission)perm.getProvider();
        }
        else
        {
            log.warning(lang("tb.missperm"));
            getPluginLoader().disablePlugin(this);
            return;
        }
        loadUserData();
        getServer().getPluginManager().registerEvents(this, this);
        log.info(lang("tb.enable", version));
    }

    public void onDisable()
    {
        log.info(lang("tb.startdisable", version));
        getServer().getScheduler().cancelTasks(this);
        saveUserData();
        log.info(lang("tb.disable", version));
    }
    
    public void reloadConfig()
    {
        super.reloadConfig();
        config = getConfig();
        reloadLang();
    }
    
    public void reloadLang()
    {
        try
        {
            if(lang == null)
            {
                lang = new YamlConfiguration();
            }
            lang.load(new File(getDataFolder(), "lang.yml"));
        }
        catch(Exception e)
        {
            checkFiles();
        }
    }
    
    private void setDefaults()
    {
        setDef(config, "economy-disabled", "false");
        setDef(config, "uf.tools", "278,285");
        setDef(config, "uf.buy", "50000");
        setDef(config, "uf.use", "0");
        setDef(config, "hammer.tools", "278,285");
        setDef(config, "hammer.buy", "500000");
        setDef(config, "hammer.use", "0");
        setDef(config, "hammer.maxradius", "3");
        setDef(config, "lb.tools", "271,275,258,286,279");
        setDef(config, "lb.buy", "20000");
        setDef(config, "lb.use", "0.2");
        setDef(config, "invpick.buy", "200000");
        setDef(config, "invpick.use", "0");
        setDef(config, "repair.use", "15");
        saveConfig();
        config = getConfig();
        setDef(lang, "tb.misseco", "ToolBox: Cannot find any vault-hooked economy plugin, disabling.");
        setDef(lang, "tb.missperm", "ToolBox: Cannot find any vault-hooked permissions plugin, disabling.");
        setDef(lang, "tb.enable", "ToolBox %1 enabled");
        setDef(lang, "tb.startdisable", "Disabling ToolBox %1...");
        setDef(lang, "tb.disable", "ToolBox %1 disabled");
        setDef(lang, "help.title", "----- ----- ----- ToolBox Help ----- ----- -----");
        setDef(lang, "help.help", "/tb help [x] - Show help page x");
        setDef(lang, "help.specifichelp", "/tb help [tool] - Show help for a tool (e.g. uf)");
        setDef(lang, "help.use.uf", "The Ultimate Fist is a super piackaxe that destroys every block (except bedrock) in no time and takes no damage!");
        setDef(lang, "help.use.hammer", "Gods Hammer is a larger version of Ultimate Fist: It mines with range!");
        setDef(lang, "help.use.lb", "Destroy one leave and all leaves next to it will be blown away.");
        setDef(lang, "help.use.invpick", "Instead of dropping the blocks when mined, they are directly sent to your inventory.");
        setDef(lang, "help.uf.buy", "/uf buy - Buy the Ultimate Fist");
        setDef(lang, "help.uf.use", "/uf - Bind the Ultimate Fist to your current tool. Allowed tools: %1");
        setDef(lang, "help.uf.off", "/uf off - Turn off the Ultimate Fist");
        setDef(lang, "help.hammer.buy", "/hammer buy - Buy Gods Hammer");
        setDef(lang, "help.hammer.use", "/hammer - Bind Gods Hammer to your current tool. Allowed tools: %1");
        setDef(lang, "help.hammer.off", "/hammer off - Turn off Gods Hammer");
        setDef(lang, "help.hammer.mode", "/hammer (soft/hard) - Switch between soft and hard mode. In soft mode, Gods Hammer only mines the same blocks as the one you clicked, in hard mode it mines all blocks within its radius.");
        setDef(lang, "help.hammer.set1", "/hammer [s] - Set the hammers radius. The edge length of the cube it mines is 2*s+1.");
        setDef(lang, "help.hammer.set2", "/hammer [x] [y] [z] - Set the hammers radius. x is left and right, y up and down, z forward and backward.");
        setDef(lang, "help.hammer.set3", "/hammer [left] [right] [down] [up] [back] [for] - Set the hammers radius.");
        setDef(lang, "help.lb.buy", "/tb lb buy - Buy the Leaf Blower");
        setDef(lang, "help.lb.use", "/tb lb - Bind the Leaf Blower to your current tool. Allowed tools: %1");
        setDef(lang, "help.lb.off", "/tb lb off - Turn off the Leaf Blower");
        setDef(lang, "help.invpick.buy", "/invpick buy - Buy the InvPick");
        setDef(lang, "help.invpick.toggle", "/invpick - Toggle between on and off");
        setDef(lang, "help.invpick.mode", "/invpick (on/off) - Turn InvPicking on or off");
        setDef(lang, "help.repair", "/repair - Fully repair your tool");
        setDef(lang, "version", "ToolBox %1");
        setDef(lang, "yes", "yes");
        setDef(lang, "no", "no");
        setDef(lang, "everything", "everything");
        setDef(lang, "nothing", "nothing");
        setDef(lang, "use.player", "This command can only be used by a player.");
        setDef(lang, "use.perm", "You're not allowed to use ToolBox.");
        setDef(lang, "money.toofew", "You don't have enough money.");
        setDef(lang, "argument.invalid", "Invalid argument.");
        setDef(lang, "argument.unknown", "Unknown argument.");
        setDef(lang, "argument.few", "Too few arguments.");
        setDef(lang, "argument.error", "Invalid command.");
        setDef(lang, "uf.perm", "You're not allowed to use Ultimate Fist.");
        setDef(lang, "uf.none", "You don't have an Ultimate Fist.");
        setDef(lang, "uf.have", "You already have an Ultimate Fist.");
        setDef(lang, "uf.bought", "You bought an Ultimate Fist.");
        setDef(lang, "uf.set", "Bound Ultimate Fist to that tool.");
        setDef(lang, "uf.off", "Disabled Ultimate Fist.");
        setDef(lang, "uf.invalid", "You can't use that tool as Ultimate Fist.");
        setDef(lang, "hammer.perm", "You're not allowed to use Gods Hammer.");
        setDef(lang, "hammer.none", "You don't have Gods Hammer.");
        setDef(lang, "hammer.have", "You already have Gods Hammer.");
        setDef(lang, "hammer.bought", "You bought Gods Hammer.");
        setDef(lang, "hammer.set", "Bound Gods Hammer to that tool.");
        setDef(lang, "hammer.setsize", "Set the size of Gods Hammer to %1x%2x%3.");
        setDef(lang, "hammer.off", "Disabled Gods Hammer.");
        setDef(lang, "hammer.invalid", "You can't use that tool as Gods Hammer.");
        setDef(lang, "hammer.amount", "You have to enter 1, 3 or 6 values.");
        setDef(lang, "hammer.mode.perm", "You aren't allowed to use Gods Hammer in hard mode.");
        setDef(lang, "hammer.mode.soft", "Set Gods Hammer to soft mode.");
        setDef(lang, "hammer.mode.hard", "Set Gods Hammer to HARD mode!");
        setDef(lang, "hammer.belowzero", "You can't use negative values!");
        setDef(lang, "hammer.overmax", "You can't use values higher than %1!");
        setDef(lang, "lb.perm", "You're not allowed to use the Leaf Blower.");
        setDef(lang, "lb.none", "You don't have a Leaf Blower.");
        setDef(lang, "lb.have", "You already have a Leaf Blower.");
        setDef(lang, "lb.bought", "You bought a Leaf Blower.");
        setDef(lang, "lb.set", "Bound the Leaf Blower to that tool.");
        setDef(lang, "lb.off", "Disabled the Leaf Blower.");
        setDef(lang, "lb.invalid", "You can't use that tool as Leaf Blower.");
        setDef(lang, "invpick.perm", "You're not allowed to use InvPick.");
        setDef(lang, "invpick.none", "You don't have an InvPick.");
        setDef(lang, "invpick.have", "You already have an InvPick.");
        setDef(lang, "invpick.bought", "You bought an InvPick.");
        setDef(lang, "invpick.on", "InvPicking on");
        setDef(lang, "invpick.off", "InvPicking off");
        setDef(lang, "repair.perm", "You aren't allowed to repair your items.");
        setDef(lang, "repair.invalid", "You can't repair this item.");
        setDef(lang, "repair.done", "Item repaired. Cost: %1");
        // setDef(lang, "", );
        try
        {
            lang.save(new File(getDataFolder(), "lang.yml"));
        }
        catch(Exception e)
        {
        }
    }
    
    private void setDef(FileConfiguration file, String path, String value)
    {
        if(!file.isSet(path))
        {
            file.set(path, value);
        }
    }
    
    private void checkFiles()
    {
        try
        {
            File file = getDataFolder();
            if(!file.exists())
            {
                file.mkdirs();
            }
            PrintStream writer;
            File data;
            String files[] = new String[]{"config.yml", "lang.yml", "data.db"};
            for(int i = 0; i < files.length; i++)
            {
                data = new File(file, files[i]);
                if(!data.exists())
                {
                    writer = new PrintStream(new FileOutputStream(data));
                    writer.close();
                }
            }
        }
        catch(Exception e)
        {
        }
    }
    
    protected void loadUserData()
    {
        try
		{
			BufferedReader file = new BufferedReader(new FileReader(new File(getDataFolder(), "data.db")));
			String line;
            String data[];
			while((line = file.readLine()) != null)
			{
                data = line.split(separator[0]);
                if(data.length >= 2)
                {
                    putBox(data[0], new ToolBox(data[0].toLowerCase(), data, 1));
                }
			}
			file.close();
		}
		catch(Exception e)
		{
            log.warning("[ToolBox] AN ERROR OCCURED! PLEASE SEND THE MESSAGE BELOW TO THE DEVELOPER!");
            e.printStackTrace();
		}
    }
    
    protected void saveUserData()
    {
        try
		{
			BufferedWriter writer = new BufferedWriter(new FileWriter(new File(getDataFolder(), "data.db")));
			Object key[] = boxes.keySet().toArray();
            String name;
            ToolBox box;
            String contents;
            String data[];
            for(int i = 0; i < key.length; i++)
            {
                name = (String)key[i];
                box = getBox(name);
                if(box != null)
                {
                    contents = name;
                    data = box.save();
                    for(int j = 0; j < data.length; j++)
                    {
                        contents += separator[0] + data[j];
                    }
                    writer.write(contents);
                    writer.newLine();
                }
            }
			writer.close();
		}
		catch(Exception e)
		{
            log.warning("[ToolBox] AN ERROR OCCURED! PLEASE SEND THE MESSAGE BELOW TO THE DEVELOPER!");
            e.printStackTrace();
        }
    }
    
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
    {
        if((label != null) && !label.equalsIgnoreCase("tb") && !label.equalsIgnoreCase("toolbox"))
        {
            if((args.length >= 1) && (args[0].equalsIgnoreCase("help")))
            {
                return onCommand(sender, cmd, "tb", args);
            }
            String fish[] = new String[args.length + 1];
            fish[0] = label;
            for(int i = 0; i < args.length; i++)
            {
                fish[i + 1] = args[i];
            }
            return onCommand(sender, cmd, "tb", fish);
        }
        if((args.length <= 0) || ((args.length >= 1) && (args[0].equals("help"))))
        {
            cmdHelp(sender, args);
            return true;
        }
        args[0] = longname(args[0]);
        if(args[0].equals("version"))
        {
            sendMessage(sender, lang("version", version), ChatColor.BLUE);
            return true;
        }
        else if(args[0].equals("admin"))
        {
            cmdAdmin(sender, args);
            return true;
        }
        else if(!(sender instanceof Player))
        {
            sendMessage(sender, lang("use.player"), ChatColor.RED);
            return true;
        }
        else if(!sender.hasPermission("toolbox.use"))
        {
            sendMessage(sender, lang("use.perm"), ChatColor.RED);
            return true;
        }
        try
        {
            if(args[0].equals("price"))
            {
                cmdPrices(sender, args);
            }
            else if(args[0].equals("ultimatefist"))
            {
                cmdUF(sender, args);
            }
            else if(args[0].equals("hammer"))
            {
                cmdHammer(sender, args);
            }
            else if(args[0].equals("leafblower"))
            {
                cmdLeafblower(sender, args);
            }
            else if(args[0].equals("invpick"))
            {
                cmdInvpick(sender, args);
            }
            else if(args[0].equals("repair"))
            {
                cmdRepair(sender, args);
            }
            else
            {
                sendMessage(sender, lang("argument.unknown"), ChatColor.RED);
            }
        }
        catch(Exception e)
        {
            sendMessage(sender, lang("argument.error"), ChatColor.RED);
            log.warning("[ToolBox] AN ERROR OCCURED! PLEASE SEND THE MESSAGE BELOW TO THE DEVELOPER!");
            e.printStackTrace();
        }
        return true;
    }
    
    protected abstract void cmdHelp(CommandSender sender, String[] args);
    protected abstract void cmdAdmin(CommandSender sender, String[] args);
    protected abstract void cmdPrices(CommandSender sender, String[] args);
    protected abstract void cmdUF(CommandSender sender, String[] args);
    protected abstract void cmdHammer(CommandSender sender, String[] args);
    protected abstract void cmdLeafblower(CommandSender sender, String[] args);
    protected abstract void cmdInvpick(CommandSender sender, String[] args);
    protected abstract void cmdRepair(CommandSender sender, String[] args);
    
    public String longname(String s)
    {
        s = s.toLowerCase().trim();
        if(s.length() > 2)
        {
            return s;
        }
        if(s.equals("v"))
        {
            return "version";
        }
        if(s.equals("a"))
        {
            return "admin";
        }
        if(s.equals("p"))
        {
            return "price";
        }
        if(s.equals("uf"))
        {
            return "ultimatefist";
        }
        if(s.equals("h"))
        {
            return "hammer";
        }
        if(s.equals("lb"))
        {
            return "leafblower";
        }
        if(s.equals("i"))
        {
            return "invpick";
        }
        if(s.equals("r"))
        {
            return "repair";
        }
        return s;
    }
    
    public boolean hasBox(String name)
    {
        name = name.toLowerCase();
        return !(boxes.get(name) == null);
    }
    
    public ToolBox getBox(String name)
    {
        name = name.toLowerCase();
        ToolBox box = boxes.get(name);
        if(box == null)
        {
            box = new ToolBox(name);
            putBox(name, box);
        }
        return box;
    }
    
    public void putBox(String name, ToolBox box)
    {
        name = name.toLowerCase();
        boxes.put(name, box);
    }
    
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
    
    public static String lang(String string, String... replacements)
    {
        string = lang(string);
        if(replacements != null)
        {
            for(int i = 1; i <= replacements.length; i++)
            {
                string = string.replaceAll("%" + i, replacements[i - 1]);
            }
        }
        return string;
    }
    
    public static String lang(String string)
    {
        if((lang != null) && (lang.isSet(string)))
        {
            return lang.getString(string);
        }
        return "STRING NOT FOUND";
    }
    
    public static int getConfigInt(String prefix, String suffix, CommandSender sender, boolean max)
    {
        String groups[] = perms.getPlayerGroups((CraftPlayer)sender);
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
        String groups[] = perms.getPlayerGroups((CraftPlayer)sender);
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
    
    public static boolean getConfigIsInList(String search, String prefix, String suffix, CommandSender sender, boolean max)
    {
        String groups[] = perms.getPlayerGroups((CraftPlayer)sender);
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
        String groups[] = perms.getPlayerGroups((CraftPlayer)sender);
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