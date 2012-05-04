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
import org.bukkit.plugin.ServicesManager;
import org.bukkit.configuration.file.*;

import static net.drgnome.toolbox.Config.*;
import static net.drgnome.toolbox.Lang.*;
import static net.drgnome.toolbox.Util.*;

public abstract class TBPluginBase extends JavaPlugin implements Listener
{
    public static String version = "Beta 0.3.0";
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
        initLang(getDataFolder());
        reloadConf(getConfig());
        saveConfig();
        reloadLang();
        if(!initPerms())
        {
            getPluginLoader().disablePlugin(this);
        }
        economyDisabled = getConfigString("economy-disabled").equalsIgnoreCase("true") ? true : false;
        if(!initEconomy())
        {
            getPluginLoader().disablePlugin(this);
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
        reloadConf(getConfig());
        saveConfig();
        reloadLang();
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
            String files[] = new String[]{"config.yml", "data.db"};
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
            warn();
            e.printStackTrace();
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
            warn();
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
            warn();
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
}