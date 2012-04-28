// Bukkit Plugin "ToolBox" by Siguza
// This software is distributed under the following license:
// http://creativecommons.org/licenses/by-nc-sa/3.0/

package net.drgnome.toolbox;

import java.io.*;
import java.util.*;
import java.util.logging.Logger;

import net.minecraft.server.*;

import org.bukkit.block.Block;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import static net.drgnome.toolbox.Config.*;
import static net.drgnome.toolbox.Lang.*;
import static net.drgnome.toolbox.Util.*;

public class TBPlugin extends TBPluginBase implements Listener
{
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void handleClickEvent(PlayerInteractEvent event)
    {
        if((event != null) && (event.getAction() == Action.LEFT_CLICK_BLOCK) && (event.hasBlock()))
        {
            getBox(((CraftPlayer)event.getPlayer()).getHandle().name).handleClick(event.getPlayer(), event.getClickedBlock());
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void handleBreakEvent(BlockBreakEvent event)
    {
        if(event != null)
        {
            getBox(((CraftPlayer)event.getPlayer()).getHandle().name).handleBreak(event.getPlayer(), event.getBlock());
        }
    }
    
    protected void cmdHelp(CommandSender sender, String[] args)
    {
        sendMessage(sender, lang("help.title"), ChatColor.AQUA);
        sendMessage(sender, lang("help.help"), ChatColor.YELLOW);
        sendMessage(sender, lang("help.specifichelp"), ChatColor.YELLOW);
        int page = 1;
        if(args.length >= 2)
        {
            try
            {
                page = Integer.parseInt(args[1]);
            }
            catch(Exception e)
            {
                args[1] = longname(args[1]);
                if(args[1].equals("ultimatefist"))
                {
                    sendMessage(sender, lang("help.use.uf"), ChatColor.AQUA);
                    return;
                }
                else if(args[1].equals("hammer"))
                {
                    sendMessage(sender, lang("help.use.hammer"), ChatColor.AQUA);
                    return;
                }
                else if(args[1].equals("leafblower"))
                {
                    sendMessage(sender, lang("help.use.lb"), ChatColor.AQUA);
                    return;
                }
                else if(args[1].equals("invpick"))
                {
                    sendMessage(sender, lang("help.use.invpick"), ChatColor.AQUA);
                    return;
                }
            }
        }
        switch(page)
        {
            case 1:
                sendMessage(sender, lang("help.uf.buy"), ChatColor.AQUA);
                sendMessage(sender, lang("help.uf.use", getConfigList("uf", "tools", sender)), ChatColor.AQUA);
                sendMessage(sender, lang("help.uf.off"), ChatColor.AQUA);
                sendMessage(sender, lang("help.lb.buy"), ChatColor.AQUA);
                sendMessage(sender, lang("help.lb.use", getConfigList("lb", "tools", sender)), ChatColor.AQUA);
                sendMessage(sender, lang("help.lb.off"), ChatColor.AQUA);
                break;
            case 2:
                sendMessage(sender, lang("help.hammer.buy"), ChatColor.AQUA);
                sendMessage(sender, lang("help.hammer.use", getConfigList("hammer", "tools", sender)), ChatColor.AQUA);
                sendMessage(sender, lang("help.hammer.off"), ChatColor.AQUA);
                sendMessage(sender, lang("help.hammer.mode"), ChatColor.AQUA);
                sendMessage(sender, lang("help.hammer.set1"), ChatColor.AQUA);
                sendMessage(sender, lang("help.hammer.set2"), ChatColor.AQUA);
                sendMessage(sender, lang("help.hammer.set3"), ChatColor.AQUA);
                break;
            case 3:
                sendMessage(sender, lang("help.invpick.buy"), ChatColor.AQUA);
                sendMessage(sender, lang("help.invpick.toggle"), ChatColor.AQUA);
                sendMessage(sender, lang("help.invpick.mode"), ChatColor.AQUA);
                sendMessage(sender, lang("help.repair"), ChatColor.AQUA);
                break;
            default:
                break;
        }
    }
    
    protected void cmdAdmin(CommandSender sender, String[] args)
    {
        
    }
    
    protected void cmdPrices(CommandSender sender, String[] args)
    {
        
    }
    
    protected void cmdUF(CommandSender sender, String[] args)
    {
        if(!sender.hasPermission("toolbox.use.uf"))
        {
            sendMessage(sender, lang("uf.perm"), ChatColor.RED);
            return;
        }
        EntityPlayer player = ((CraftPlayer)sender).getHandle();
        if(args.length >= 2)
        {
            if(args[1].equalsIgnoreCase("buy"))
            {
                getBox(player.name).buyUF(sender);
                return;
            }
            else if(args[1].equalsIgnoreCase("off"))
            {
                getBox(player.name).setUF(sender, -1);
                return;
            }
        }
        ItemStack item = player.inventory.getItemInHand();
        if(item == null)
        {
            getBox(player.name).setUF(sender, 0);
        }
        else
        {
            getBox(player.name).setUF(sender, item.id);
        }
    }
    
    protected void cmdHammer(CommandSender sender, String[] args)
    {
        if(!sender.hasPermission("toolbox.use.hammer"))
        {
            sendMessage(sender, lang("hammer.perm"), ChatColor.RED);
            return;
        }
        EntityPlayer player = ((CraftPlayer)sender).getHandle();
        if(args.length >= 2)
        {
            if(args[1].equalsIgnoreCase("buy"))
            {
                getBox(player.name).buyHammer(sender);
                return;
            }
            else if(args[1].equalsIgnoreCase("off"))
            {
                getBox(player.name).setHammer(sender, -1);
                return;
            }
            else if(args[1].equalsIgnoreCase("soft"))
            {
                getBox(player.name).hammerAll = false;
                sendMessage(sender, lang("hammer.mode.soft"), ChatColor.GREEN);
                return;
            }
            else if(args[1].equalsIgnoreCase("hard"))
            {
                if(!sender.hasPermission("toolbox.use.hammer.hard"))
                {
                    sendMessage(sender, lang("hammer.mode.perm"), ChatColor.RED);
                    return;
                }
                getBox(player.name).hammerAll = true;
                sendMessage(sender, lang("hammer.mode.hard"), ChatColor.GREEN);
                return;
            }
            else
            {
                try
                {
                    if(args.length == 2)
                    {
                        getBox(player.name).setHammerSize(sender, Integer.parseInt(args[1]));
                    }
                    else if(args.length == 4)
                    {
                        getBox(player.name).setHammerSize(sender, Integer.parseInt(args[1]), Integer.parseInt(args[2]), Integer.parseInt(args[3]));
                    }
                    else if(args.length == 7)
                    {
                        getBox(player.name).setHammerSize(sender, Integer.parseInt(args[1]), Integer.parseInt(args[2]), Integer.parseInt(args[3]), Integer.parseInt(args[4]), Integer.parseInt(args[5]), Integer.parseInt(args[6]));
                    }
                    else
                    {
                        sendMessage(sender, lang("hammer.amount"), ChatColor.RED);
                        return;
                    }
                }
                catch(Exception e)
                {
                    sendMessage(sender, lang("argument.invalid"), ChatColor.RED);
                }
            }
            return;
        }
        ItemStack item = player.inventory.getItemInHand();
        if(item == null)
        {
            getBox(player.name).setHammer(sender, 0);
        }
        else
        {
            getBox(player.name).setHammer(sender, item.id);
        }
    }
    
    protected void cmdLeafblower(CommandSender sender, String[] args)
    {
        if(!sender.hasPermission("toolbox.use.lb"))
        {
            sendMessage(sender, lang("lb.perm"), ChatColor.RED);
            return;
        }
        EntityPlayer player = ((CraftPlayer)sender).getHandle();
        if(args.length >= 2)
        {
            if(args[1].equalsIgnoreCase("buy"))
            {
                getBox(player.name).buyLB(sender);
                return;
            }
            else if(args[1].equalsIgnoreCase("off"))
            {
                getBox(player.name).setLB(sender, -1);
                return;
            }
        }
        ItemStack item = player.inventory.getItemInHand();
        if(item == null)
        {
            getBox(player.name).setLB(sender, 0);
        }
        else
        {
            getBox(player.name).setLB(sender, item.id);
        }
    }
    
    protected void cmdInvpick(CommandSender sender, String[] args)
    {
        if(!sender.hasPermission("toolbox.use.invpick"))
        {
            sendMessage(sender, lang("invpick.perm"), ChatColor.RED);
            return;
        }
        EntityPlayer player = ((CraftPlayer)sender).getHandle();
        if(args.length >= 2)
        {
            if(args[1].equalsIgnoreCase("buy"))
            {
                getBox(player.name).buyInvpick(sender);
                return;
            }
            else if(args[1].equalsIgnoreCase("on"))
            {
                getBox(player.name).setInvpick(sender, true);
                return;
            }
            else if(args[1].equalsIgnoreCase("off"))
            {
                getBox(player.name).setInvpick(sender, false);
                return;
            }
        }
        getBox(player.name).setInvpick(sender, !getBox(player.name).invpickActive);
    }
    
    protected void cmdRepair(CommandSender sender, String[] args)
    {
        if(!sender.hasPermission("toolbox.use.repair"))
        {
            sendMessage(sender, lang("repair.perm"), ChatColor.RED);
            return;
        }
        EntityPlayer player = ((CraftPlayer)sender).getHandle();
        ItemStack item = player.inventory.getItemInHand();
        if((item == null) || !Item.byId[item.id].g())
        {
            sendMessage(sender, lang("repair.invalid"), ChatColor.RED);
            return;
        }
        double price = getConfigDouble("repair", "use", sender, false) * (double)item.count * (double)item.getData();
        if(!moneyHasTake(player.name, price))
        {
            sendMessage(sender, lang("money.toofew"), ChatColor.RED);
            return;
        }
        item.setData(0);
        sendMessage(sender, lang("repair.done", "" + smoothDouble(price, 2)), ChatColor.GREEN);
    }
}