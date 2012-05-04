// Bukkit Plugin "ToolBox" by Siguza
// This software is distributed under the following license:
// http://creativecommons.org/licenses/by-nc-sa/3.0/

package net.drgnome.toolbox;

import java.io.*;

import org.bukkit.configuration.file.*;

import static net.drgnome.toolbox.Util.*;

// Thought for static import
public class Lang
{    
    private static YamlConfiguration config = new YamlConfiguration();
    private static File dir;
    
    public static void initLang(File folder)
    {
        dir = folder;
        reloadLang();
    }
    
    public static void reloadLang()
    {
        try
        {
            File file = new File(dir, "lang.yml");
            if(!file.exists())
            {
                file.mkdirs();
                PrintStream writer = new PrintStream(new FileOutputStream(file));
                writer.close();
            }
            config.load(file);
            setDefs();
            config.save(file);
        }
        catch(Exception e)
        {
            warn();
            e.printStackTrace();
        }
    }
    
    // Set all default values
    private static void setDefs()
    {
        setDef("tb.misseco", "ToolBox: Cannot find any vault-hooked economy plugin, disabling.");
        setDef("tb.missperm", "ToolBox: Cannot find any vault-hooked permissions plugin, disabling.");
        setDef("tb.enable", "ToolBox %1 enabled");
        setDef("tb.startdisable", "Disabling ToolBox %1...");
        setDef("tb.disable", "ToolBox %1 disabled");
        setDef("help.title", "----- ----- ----- ToolBox Help ----- ----- -----");
        setDef("help.help", "/tb help [x] - Show help page x");
        setDef("help.specifichelp", "/tb help [tool] - Show help for a tool (e.g. uf)");
        setDef("help.price", "/tb p - Show the prices");
        setDef("help.use.uf", "The Ultimate Fist is a super piackaxe that destroys every block (except bedrock) in no time and takes no damage!");
        setDef("help.use.hammer", "Gods Hammer is a larger version of Ultimate Fist: It mines with range!");
        setDef("help.use.lb", "Destroy one leave and all leaves next to it will be blown away.");
        setDef("help.use.invpick", "Instead of dropping the blocks when mined, they are directly sent to your inventory.");
        setDef("help.uf.buy", "/uf buy - Buy the Ultimate Fist");
        setDef("help.uf.use", "/uf - Bind the Ultimate Fist to your current tool. Allowed tools: %1");
        setDef("help.uf.off", "/uf off - Turn off the Ultimate Fist");
        setDef("help.hammer.buy", "/hammer buy - Buy Gods Hammer");
        setDef("help.hammer.use", "/hammer - Bind Gods Hammer to your current tool. Allowed tools: %1");
        setDef("help.hammer.off", "/hammer off - Turn off Gods Hammer");
        setDef("help.hammer.mode", "/hammer (soft/hard) - Switch between soft and hard mode. In soft mode, Gods Hammer only mines the same blocks as the one you clicked, in hard mode it mines all blocks within its radius.");
        setDef("help.hammer.set1", "/hammer [s] - Set the hammers radius. The edge length of the cube it mines is 2*s+1.");
        setDef("help.hammer.set2", "/hammer [x] [y] [z] - Set the hammers radius. x is left and right, y up and down, z forward and backward.");
        setDef("help.hammer.set3", "/hammer [left] [right] [down] [up] [back] [for] - Set the hammers radius.");
        setDef("help.lb.buy", "/tb lb buy - Buy the Leaf Blower");
        setDef("help.lb.use", "/tb lb - Bind the Leaf Blower to your current tool. Allowed tools: %1");
        setDef("help.lb.off", "/tb lb off - Turn off the Leaf Blower");
        setDef("help.invpick.buy", "/invpick buy - Buy the InvPick");
        setDef("help.invpick.toggle", "/invpick - Toggle between on and off");
        setDef("help.invpick.mode", "/invpick (on/off) - Turn InvPicking on or off");
        setDef("help.repair", "/repair - Fully repair your tool");
        setDef("version", "ToolBox %1");
        setDef("yes", "yes");
        setDef("no", "no");
        setDef("everything", "everything");
        setDef("nothing", "nothing");
        setDef("use.player", "This command can only be used by a player.");
        setDef("use.perm", "You're not allowed to use ToolBox.");
        setDef("money.toofew", "You don't have enough money.");
        setDef("argument.invalid", "Invalid argument.");
        setDef("argument.unknown", "Unknown argument.");
        setDef("argument.few", "Too few arguments.");
        setDef("argument.error", "Invalid command.");
        setDef("uf.perm", "You're not allowed to use Ultimate Fist.");
        setDef("uf.none", "You don't have an Ultimate Fist.");
        setDef("uf.have", "You already have an Ultimate Fist.");
        setDef("uf.bought", "You bought an Ultimate Fist.");
        setDef("uf.set", "Bound Ultimate Fist to that tool.");
        setDef("uf.off", "Disabled Ultimate Fist.");
        setDef("uf.invalid", "You can't use that tool as Ultimate Fist.");
        setDef("hammer.perm", "You're not allowed to use Gods Hammer.");
        setDef("hammer.none", "You don't have Gods Hammer.");
        setDef("hammer.have", "You already have Gods Hammer.");
        setDef("hammer.bought", "You bought Gods Hammer.");
        setDef("hammer.set", "Bound Gods Hammer to that tool.");
        setDef("hammer.setsize", "Set the size of Gods Hammer to %1x%2x%3.");
        setDef("hammer.off", "Disabled Gods Hammer.");
        setDef("hammer.invalid", "You can't use that tool as Gods Hammer.");
        setDef("hammer.amount", "You have to enter 1, 3 or 6 values.");
        setDef("hammer.mode.perm", "You aren't allowed to use Gods Hammer in hard mode.");
        setDef("hammer.mode.soft", "Set Gods Hammer to soft mode.");
        setDef("hammer.mode.hard", "Set Gods Hammer to HARD mode!");
        setDef("hammer.belowzero", "You can't use negative values!");
        setDef("hammer.overmax", "You can't use values higher than %1!");
        setDef("lb.perm", "You're not allowed to use the Leaf Blower.");
        setDef("lb.none", "You don't have a Leaf Blower.");
        setDef("lb.have", "You already have a Leaf Blower.");
        setDef("lb.bought", "You bought a Leaf Blower.");
        setDef("lb.set", "Bound the Leaf Blower to that tool.");
        setDef("lb.off", "Disabled the Leaf Blower.");
        setDef("lb.invalid", "You can't use that tool as Leaf Blower.");
        setDef("invpick.perm", "You're not allowed to use InvPick.");
        setDef("invpick.none", "You don't have an InvPick.");
        setDef("invpick.have", "You already have an InvPick.");
        setDef("invpick.bought", "You bought an InvPick.");
        setDef("invpick.on", "InvPicking on");
        setDef("invpick.off", "InvPicking off");
        setDef("repair.perm", "You aren't allowed to repair your items.");
        setDef("repair.invalid", "You can't repair this item.");
        setDef("repair.done", "Item repaired. Cost: %1");
        setDef("price.noeco", "Economy is disabled, everythign is for free!");
        setDef("price.title", "---- ---- ---- ToolBox Prices ---- ---- ----");
        setDef("price.uf", "Ultimate Fist: %1Buy: %2%3 %1Use: %2%4");
        setDef("price.hammer", "Gods Hammer: %1Buy: %2%3 %1Use: %2%4");
        setDef("price.lb", "Leafblower: %1Buy: %2%3 %1Use: %2%4");
        setDef("price.invpick", "InvPick: %1Buy: %2%3 %1Use: %2%4");
        setDef("price.repair", "Repair: %1%3 %2per damage point");
        setDef("admin.perm", "You're not allowed to perform this command.");
        setDef("admin.help.title", "----- ----- ----- ToolBox Admin Help ----- ----- -----");
        setDef("admin.help.give", "/tb a give (player) (tool) - Give a player a tool.");
        setDef("admin.help.take", "/tb a take (player) (tool) - Take a tool away from a player.");
        setDef("admin.help.delete", "/tb a delete (player) - Delete a player's ToolBox.");
        setDef("admin.help.reload", "/tb a reload - Reload the config.");
        setDef("admin.null", "%1 doesn't have a ToolBox.");
        setDef("admin.tool.null", "Unknown tool.");
        setDef("admin.give.uf.has", "%1 already has an Ultimate Fist.");
        setDef("admin.give.uf.done", "Gave %1 an Ultimate Fist.");
        setDef("admin.give.hammer.has", "%1 already has Gods Hammer.");
        setDef("admin.give.hammer.done", "Gave %1 Gods Hammer.");
        setDef("admin.give.lb.has", "%1 already has a Leaf Blower");
        setDef("admin.give.lb.done", "Gave %1 a Leaf Blower.");
        setDef("admin.give.invpick.has", "%1 already has an InvPick.");
        setDef("admin.give.invpick.done", "Gave %1 an InvPick.");
        setDef("admin.take.uf.none", "%1 doesn't have an Ultimate Fist.");
        setDef("admin.take.uf.done", "Took the Ultimate Fist away from %1.");
        setDef("admin.take.hammer.none", "%1 doesn't have Gods Hammer.");
        setDef("admin.take.hammer.done", "Took Gods Hammer away from %1.");
        setDef("admin.take.lb.none", "%1 doesn't have a Leaf Blower");
        setDef("admin.take.lb.done", "Took the Leaf Blower away from %1.");
        setDef("admin.take.invpick.none", "%1 doesn't have an InvPick.");
        setDef("admin.take.invpick.done", "Took the InvPick away from %1.");
        setDef("admin.delete", "Deleted %1's ToolBox.");
        setDef("admin.reload", "ToolBox: Config reloaded.");
    }
    
    // Set a default value
    private static void setDef(String path, String value)
    {
        if(!config.isSet(path))
        {
            config.set(path, value);
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
        if((config != null) && (config.isSet(string)))
        {
            return config.getString(string);
        }
        return "STRING NOT FOUND";
    }
}