// Bukkit Plugin "ToolBox" by Siguza
// This software is distributed under the following license:
// http://creativecommons.org/licenses/by-nc-sa/3.0/

package net.drgnome.toolbox;

import java.io.*;
import java.util.*;
import java.util.logging.Logger;
import java.lang.reflect.*;

import net.minecraft.server.*;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.scheduler.CraftScheduler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.bukkit.craftbukkit.CraftChunk;
import org.bukkit.entity.Player;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.material.MaterialData;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.configuration.file.*;

import static net.drgnome.toolbox.Config.*;
import static net.drgnome.toolbox.Lang.*;
import static net.drgnome.toolbox.Util.*;

public class ToolBox
{
    public boolean hasUF;
    protected int ufTool;
    public boolean hasHammer;
    public boolean hammerAll;
    protected int hammerTool;
    protected int hammerRadius[] = new int[6];
    public boolean hasLB;
    protected int lbTool;
    public boolean hasInvpick;
    public boolean invpickActive;
    
    public ToolBox(String username)
    {
        this(getPlayerGroups(username));
    }
    
    public ToolBox(String groups[])
    {
        if(economyDisabled)
        {
            hasUF = true;
            hasHammer = true;
            hasLB = true;
            hasInvpick = true;
        }
        else
        {
            hasUF = getConfigDouble("uf", "buy", groups, false) <= 0.0D ? true : false;
            hasHammer = getConfigDouble("hammer", "buy", groups, false) <= 0.0D ? true : false;
            hasLB = getConfigDouble("lb", "buy", groups, false) <= 0.0D ? true : false;
            hasInvpick = getConfigDouble("invpick", "buy", groups, false) <= 0.0D ? true : false;
        }
        ufTool = -1;
        hammerAll = false;
        hammerTool = -1;
        hammerRadius = new int[]{0, 0, 0, 0, 0, 0};
        lbTool = -1;
        invpickActive = false;
    }
    
    public ToolBox(String username, String data[])
    {
        this(username, data, 0);
    }
    
    public ToolBox(String username, String data[], int offset)
    {
        this(getPlayerGroups(username), data, offset);
    }
    
    public ToolBox(String groups[], String data[])
    {
        this(groups, data, 0);
    }
    
    public ToolBox(String groups[], String data[], int offset)
    {
        this(groups);
        if(data[offset].equals("new"))
        {
            String blubb[];
            for(offset++; offset < data.length; offset++)
            {
                blubb = data[offset].split(separator[1]);
                if(blubb[0].equals("uf") && (blubb.length >= 3))
                {
                    hasUF = blubb[1].equals("1") || hasUF;
                    ufTool = tryParse(blubb[2], ufTool);
                }
                else if(blubb[0].equals("hammer") && (blubb.length >= 9))
                {
                    hasHammer = blubb[1].equals("1") || hasHammer;
                    hammerTool = tryParse(blubb[2], hammerTool);
                    for(int i = 0; i < 6; i++)
                    {
                        hammerRadius[i] = tryParse(blubb[i + 3], hammerRadius[i]);
                    }
                }
                else if(blubb[0].equals("lb") && (blubb.length >= 3))
                {
                    hasLB = blubb[1].equals("1") || hasLB;
                    lbTool = tryParse(blubb[2], lbTool);
                }
                else if(blubb[0].equals("invpick") && (blubb.length >= 3))
                {
                    hasInvpick = blubb[1].equals("1") || hasInvpick;
                    invpickActive = blubb[2].equals("1") || invpickActive;
                }
            }
        }
        else
        {
            try
            {
                hasUF = data[offset].equals("1") ? true : hasUF;
                ufTool = tryParse(data[offset + 1], ufTool);
                hasHammer = data[offset + 2].equals("1") ? true : hasHammer;
                hammerAll = data[offset + 3].equals("1") ? true : hammerAll;
                hammerTool = tryParse(data[offset + 4], hammerTool);
                for(int i = 0; i < hammerRadius.length; i++)
                {
                    hammerRadius[i] = tryParse(data[offset + 5], hammerRadius[i]);
                }
            }
            catch(Exception e)
            {
            }
        }
        if(!canBeUF(ufTool, groups))
        {
            ufTool = -1;
        }
        if(!canBeHammer(hammerTool, groups))
        {
            hammerTool = -1;
        }
        if(!canBeLB(lbTool, groups))
        {
            lbTool = -1;
        }
    }
    
    public String[] save()
    {
        String string;
        ArrayList<String> list = new ArrayList<String>();
        list.add("new");
        list.add("uf" + separator[1] + (hasUF ? "1" : "0") + separator[1] + ufTool);
        string = "hammer" + separator[1] + (hasHammer ? "1" : "0") + separator[1] + (hammerAll ? "1" : "0");
        for(int i = 0; i < hammerRadius.length; i++)
        {
            string += separator[1] + hammerRadius[i];
        }
        list.add(string);
        list.add("lb" + separator[1] + (hasLB ? "1" : "0") + separator[1] + lbTool);
        list.add("invpick" + separator[1] + (hasInvpick ? "1" : "0") + separator[1] + (invpickActive ? "1" : "0"));
        return list.toArray(new String[0]);
    }
    
    private boolean canBeUF(int i, String name)
    {
        return canBeUF(i, getPlayerGroups(name));
    }
    
    private boolean canBeUF(int i, String groups[])
    {
        if(!hasPermission(groups, "toolbox.use.uf"))
        {
            return false;
        }
        return (i == -1) || getConfigIsInList("" + i, "uf", "tools", groups, true);
    }
    
    private boolean canBeHammer(int i, String name)
    {
        return canBeHammer(i, getPlayerGroups(name));
    }
    
    private boolean canBeHammer(int i, String groups[])
    {
        if(!hasPermission(groups, "toolbox.use.hammer"))
        {
            return false;
        }
        return (i == -1) || getConfigIsInList("" + i, "hammer", "tools", groups, true);
    }
    
    private boolean canBeLB(int i, String name)
    {
        return canBeLB(i, getPlayerGroups(name));
    }
    
    private boolean canBeLB(int i, String groups[])
    {
        if(!hasPermission(groups, "toolbox.use.lb"))
        {
            return false;
        }
        return (i == -1) || getConfigIsInList("" + i, "lb", "tools", groups, true);
    }
    
    private boolean isUF(int i, String name)
    {
        return (i == ufTool) && canBeUF(i, name);
    }
    
    private boolean isUF(int i, String groups[])
    {
        return (i == ufTool) && canBeUF(i, groups);
    }
    
    private boolean isHammer(int i, String name)
    {
        return (i == hammerTool) && canBeHammer(i, name);
    }
    
    private boolean isHammer(int i, String groups[])
    {
        return (i == hammerTool) && canBeHammer(i, groups);
    }
    
    private boolean isLB(int i, String name)
    {
        return (i == lbTool) && canBeLB(i, name);
    }
    
    private boolean isLB(int i, String groups[])
    {
        return (i == lbTool) && canBeLB(i, groups);
    }
    
    public void handleClick(Player bukkitplayer, Block bukkitblock)
    {
        EntityPlayer player = ((CraftPlayer)bukkitplayer).getHandle();
        ItemStack item = player.inventory.getItemInHand();
        int iid = 0;
        if(item != null)
        {
            iid = item.id;
        }
        if(isHammer(iid, player.name))
        {
            int rad[] = {0, 0, hammerRadius[2], hammerRadius[3], 0, 0};
            float yaw = player.yaw;
            yaw %= 360F;
            while(yaw < 0)
            {
                yaw += 360F;
            }
            if((yaw >= 45F) && (yaw < 135F))
            {
                rad[0] = hammerRadius[5];
                rad[1] = hammerRadius[4];
                rad[4] = hammerRadius[1];
                rad[5] = hammerRadius[0];
            }
            else if((yaw >= 135F) && (yaw < 225F))
            {
                rad[0] = hammerRadius[0];
                rad[1] = hammerRadius[1];
                rad[4] = hammerRadius[5];
                rad[5] = hammerRadius[4];
            }
            else if((yaw >= 225F) && (yaw < 315F))
            {
                rad[0] = hammerRadius[4];
                rad[1] = hammerRadius[5];
                rad[4] = hammerRadius[0];
                rad[5] = hammerRadius[1];
            }
            else
            {
                rad[0] = hammerRadius[1];
                rad[1] = hammerRadius[0];
                rad[4] = hammerRadius[4];
                rad[5] = hammerRadius[5];
            }
            Block newblock;
            int mat = player.world.getTypeId(bukkitblock.getX(), bukkitblock.getY(), bukkitblock.getZ());
            for(int x = bukkitblock.getX() - rad[0]; x <= bukkitblock.getX() + rad[1]; x++)
            {
                for(int y = bukkitblock.getY() - rad[2]; y <= bukkitblock.getY() + rad[3]; y++)
                {
                    for(int z = bukkitblock.getZ() - rad[4]; z <= bukkitblock.getZ() + rad[5]; z++)
                    {
                        if(hammerAll || (player.world.getTypeId(x, y, z) == mat))
                        {
                            newblock = new CraftBlock(new CraftChunk(player.world.getChunkAtWorldCoords(x - (x % 16), z - (z % 16))), x, y, z);
                            Bukkit.getServer().getPluginManager().callEvent(new BlockBreakEvent(newblock, bukkitplayer));
                        }
                    }
                }
            }
        }
        else if(isUF(iid, player.name))
        {
            Bukkit.getServer().getPluginManager().callEvent(new BlockBreakEvent(bukkitblock, bukkitplayer));
        }
    }
    
    public void handleBreak(Player bukkitplayer, Block bukkitblock)
    {
        EntityPlayer player = ((CraftPlayer)bukkitplayer).getHandle();
        int x = bukkitblock.getX();
        int y = bukkitblock.getY();
        int z = bukkitblock.getZ();
        int blockid = player.world.getTypeId(x, y, z);
        int blockmeta = player.world.getData(x, y, z);
        ItemStack item = player.inventory.getItemInHand();
        int iid = 0;
        if(item != null)
        {
            iid = item.id;
        }
        if(isUF(iid, player.name) || isHammer(iid, player.name) || isLB(iid, player.name) || invpickActive)
        {
            dropBlock(player, x, y, z);
            if(!isUF(iid, player.name) && !isHammer(iid, player.name))
            {
                try
                {
                    Item.byId[iid].a(item, iid, x, y, z, player);
                }
                catch(Exception e)
                {
                }
            }
        }
        Block newblock;
        int a, b, c, d, e, f;
        if((blockid == 18) && isLB(iid, player.name))
        {
            for(a = x - 1; a <= x + 1; a++)
            {
                for(b = y - 1; b <= y + 1; b++)
                {
                    for(c = z - 1; c <= z + 1; c++)
                    {
                        if((player.world.getTypeId(a, b, c) == 18) && (player.world.getData(a, b, c) == blockmeta))
                        {
                            newblock = new CraftBlock(new CraftChunk(player.world.getChunkAtWorldCoords(a, c)), a, b, c);
                            Bukkit.getServer().getPluginManager().callEvent(new BlockBreakEvent(newblock, bukkitplayer));
                        }
                    }
                }
            }
        }
    }
    
    public void dropBlock(EntityPlayer player, int x, int y, int z)
    {
        int id = player.world.getTypeId(x, y, z);
        if(id < 0)
        {
            return;
        }
        net.minecraft.server.Block block = net.minecraft.server.Block.byId[id];
        if(block == null)
        {
            return;
        }
        if(block.m() >= 0)
        {
            int meta = player.world.getData(x, y, z);
            boolean whatever = false;
            Object value = invoke(block, "h");
            if(value instanceof Boolean)
            {
                whatever = ((Boolean)value).booleanValue();
            }
            if((whatever) && (EnchantmentManager.hasSilkTouchEnchantment(player.inventory)))
            {
                value = invoke(block, "a_", meta);
                if(value instanceof ItemStack)
                {
                    drop(player, x, y, z, (ItemStack)value);
                }
            }
            else
            {
                int bonus = EnchantmentManager.getBonusBlockLootEnchantmentLevel(player.inventory);
                int dropid = block.getDropType(meta, player.world.random, bonus);
                if(dropid > 0)
                {
                    drop(player, x, y, z, new ItemStack(dropid, block.getDropCount(bonus, player.world.random), net.minecraft.server.Block.getDropData(block, meta)));
                }
            }
            player.world.setTypeId(x, y, z, 0);
        }
    }
    
    public void drop(EntityPlayer player, int x, int y, int z, ItemStack item)
    {
        if(item == null)
        {
            return;
        }
        if(item.id == 332)
        {
            item.count = 1;
        }
        if(item.count <= 0)
        {
            return;
        }
        double price = getConfigDouble("invpick", "use", Bukkit.getPlayerExact(player.name), false);
        if((invpickActive) && moneyHas(player.name, price) && (player.inventory.pickup(item)))
        {
            moneyTake(player.name, price);
            return;
        }
        EntityItem entityitem = new EntityItem(player.world, x + 0.5D, y, z + 0.5D, item);
        entityitem.pickupDelay = 5;
        entityitem.motX = 0.0D;
        entityitem.motZ = 0.0D;
        player.world.addEntity(entityitem);
    }
    
    public void buyUF(CommandSender sender)
    {
        if(hasUF)
        {
            sendMessage(sender, lang("uf.have"), ChatColor.RED);
            return;
        }
        EntityPlayer player = ((CraftPlayer)sender).getHandle();
        if(!moneyHasTake(player.name, getConfigDouble("uf", "buy", sender, false)))
        {
            sendMessage(sender, lang("money.toofew"), ChatColor.RED);
            return;
        }
        hasUF = true;
        sendMessage(sender, lang("uf.bought"), ChatColor.GREEN);
    }
    
    public void setUF(CommandSender sender, int i)
    {
        if(!hasUF)
        {
            sendMessage(sender, lang("uf.none"), ChatColor.RED);
            return;
        }
        EntityPlayer player = ((CraftPlayer)sender).getHandle();
        if(!canBeUF(i, player.name))
        {
            sendMessage(sender, lang("uf.invalid"), ChatColor.RED);
            return;
        }
        ufTool = i;
        if(i == -1)
        {
            sendMessage(sender, lang("uf.off"), ChatColor.GREEN);
        }
        else
        {
            sendMessage(sender, lang("uf.set"), ChatColor.GREEN);
        }
    }
    
    public void buyHammer(CommandSender sender)
    {
        if(hasHammer)
        {
            sendMessage(sender, lang("hammer.have"), ChatColor.RED);
            return;
        }
        EntityPlayer player = ((CraftPlayer)sender).getHandle();
        if(!moneyHasTake(player.name, getConfigDouble("hammer", "buy", sender, false)))
        {
            sendMessage(sender, lang("money.toofew"), ChatColor.RED);
            return;
        }
        hasHammer = true;
        sendMessage(sender, lang("hammer.bought"), ChatColor.GREEN);
    }
    
    public void setHammer(CommandSender sender, int i)
    {
        if(!hasHammer)
        {
            sendMessage(sender, lang("hammer.none"), ChatColor.RED);
            return;
        }
        EntityPlayer player = ((CraftPlayer)sender).getHandle();
        if(!canBeHammer(i, player.name))
        {
            sendMessage(sender, lang("hammer.invalid"), ChatColor.RED);
            return;
        }
        hammerTool = i;
        if(i == -1)
        {
            sendMessage(sender, lang("hammer.off"), ChatColor.GREEN);
        }
        else
        {
            sendMessage(sender, lang("hammer.set"), ChatColor.GREEN);
        }
    }
    
    public void setHammerSize(CommandSender sender, int... param)
    {
        if((param == null) || (param.length == 0))
        {
            sendMessage(sender, lang("argument.invalid"), ChatColor.RED);
            return;
        }
        else if(param.length < 3)
        {
            for(int i = 0; i < 6; i++)
            {
                hammerRadius[i] = param[0];
            }
        }
        else if(param.length < 6)
        {
            hammerRadius[0] = param[0];
            hammerRadius[1] = param[0];
            hammerRadius[2] = param[1];
            hammerRadius[3] = param[1];
            hammerRadius[4] = param[2];
            hammerRadius[5] = param[2];
        }
        else
        {
            for(int i = 0; i < 6; i++)
            {
                hammerRadius[i] = param[i];
            }
        }
        boolean overmax = false;
        boolean belowzero = false;
        int max = getConfigInt("hammer", "maxradius", sender, true);
        max = max < 0 ? 0 : max;
        for(int i = 0; i < 6; i++)
        {
            if(hammerRadius[i] < 0)
            {
                hammerRadius[i] = 0;
                belowzero = true;
            }
            else if(hammerRadius[i] > max)
            {
                hammerRadius[i] = max;
                overmax = true;
            }
        }
        if(belowzero)
        {
            sendMessage(sender, lang("hammer.belowzero"), ChatColor.RED);
        }
        if(overmax)
        {
            sendMessage(sender, lang("hammer.overmax", "" + max), ChatColor.RED);
        }
        sendMessage(sender, lang("hammer.setsize", "" + (1 + hammerRadius[0] + hammerRadius[1]), "" + (1 + hammerRadius[2] + hammerRadius[3]), "" + (1 + hammerRadius[4] + hammerRadius[5])), ChatColor.GREEN);
    }
    
    public void buyLB(CommandSender sender)
    {
        if(hasLB)
        {
            sendMessage(sender, lang("lb.have"), ChatColor.RED);
            return;
        }
        EntityPlayer player = ((CraftPlayer)sender).getHandle();
        if(!moneyHasTake(player.name, getConfigDouble("lb", "buy", sender, false)))
        {
            sendMessage(sender, lang("money.toofew"), ChatColor.RED);
            return;
        }
        hasLB = true;
        sendMessage(sender, lang("lb.bought"), ChatColor.GREEN);
    }
    
    public void setLB(CommandSender sender, int i)
    {
        if(!hasLB)
        {
            sendMessage(sender, lang("lb.none"), ChatColor.RED);
            return;
        }
        EntityPlayer player = ((CraftPlayer)sender).getHandle();
        if(!canBeLB(i, player.name))
        {
            sendMessage(sender, lang("lb.invalid"), ChatColor.RED);
            return;
        }
        lbTool = i;
        if(i == -1)
        {
            sendMessage(sender, lang("lb.off"), ChatColor.GREEN);
        }
        else
        {
            sendMessage(sender, lang("lb.set"), ChatColor.GREEN);
        }
    }
    
    public void buyInvpick(CommandSender sender)
    {
        if(hasInvpick)
        {
            sendMessage(sender, lang("invpick.have"), ChatColor.RED);
            return;
        }
        EntityPlayer player = ((CraftPlayer)sender).getHandle();
        if(!moneyHasTake(player.name, getConfigDouble("invpick", "buy", sender, false)))
        {
            sendMessage(sender, lang("money.toofew"), ChatColor.RED);
            return;
        }
        hasInvpick = true;
        sendMessage(sender, lang("invpick.bought"), ChatColor.GREEN);
    }
    
    public void setInvpick(CommandSender sender, boolean mode)
    {
        if(!hasInvpick)
        {
            sendMessage(sender, lang("invpick.none"), ChatColor.RED);
            return;
        }
        invpickActive = mode;
        if(mode)
        {
            sendMessage(sender, lang("invpick.on"), ChatColor.GREEN);
        }
        else
        {
            sendMessage(sender, lang("invpick.off"), ChatColor.GREEN);
        }
    }
}