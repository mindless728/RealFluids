/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package mindless728.RealFluids;

import org.bukkit.block.Block;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoader;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.Server;

import java.io.File;

import java.util.HashMap;
import java.util.LinkedHashSet;

/**
 *
 * @author colin
 */
public class RealFluids extends JavaPlugin implements Runnable {
    BukkitScheduler scheduler;
    LinkedHashSet<Block> waterFlows;
    HashMap<Block,Integer> waterData;
    RealFluidsBlockListener blockListener;
    int waterStartLevel = 4500;
    double minimumDifferencelevelFraction = 0.10;
    int repeatRate = 1;
    int simsMaxPerRepeat = Integer.MAX_VALUE;
    double simsPerRepeatFraction = 0.5;
    double flowDownFraction = 0.5;
    
    public RealFluids(PluginLoader pluginLoader, Server instance, PluginDescriptionFile desc, File folder, File plugin, ClassLoader cLoader) {
        super(pluginLoader, instance, desc, folder, plugin, cLoader);
        waterFlows = new LinkedHashSet<Block>();
        waterData = new HashMap<Block,Integer>();
        blockListener = new RealFluidsBlockListener(this);
    }
    
    public void onEnable() {
        scheduler = this.getServer().getScheduler();
        scheduler.scheduleSyncRepeatingTask(this, this, 1, repeatRate);
        
        getServer().getPluginManager().registerEvent(Type.BLOCK_PLACED, blockListener, Priority.Low, this);
        getServer().getPluginManager().registerEvent(Type.BLOCK_FLOW, blockListener, Priority.Low, this);
        
        System.out.println(getDescription().getName()+" version "+getDescription().getVersion()+" enabled");
    }
    
    public void onDisable() {
        waterFlows.clear();
        System.out.println(getDescription().getName()+" version "+getDescription().getVersion()+" disabled");
    }
    
    public void addFluidFlow(Block b) {
        if(!waterData.containsKey(b))
            waterData.put(b,new Integer(waterStartLevel));
        waterFlows.add(b);
    }
    
    public void removeFluidFlow(Block b) {
        waterFlows.remove(b);
        waterData.remove(b);
    }
    
    public boolean hasFluidData(Block b) {
        return waterData.containsKey(b);
    }
    
    public void run() {
        Block front;
        int ave;
        int repeat = (int)(simsPerRepeatFraction*waterFlows.size());
        if(repeat > simsMaxPerRepeat)
            repeat = simsMaxPerRepeat;
        //if(waterFlows.size() != 0)
            //System.out.println("Queue size: "+waterFlows.size());
        
        for(int n = 0; n < repeat; ++n) {
            if(waterFlows.isEmpty())
                break;
            front = waterFlows.iterator().next();
            waterFlows.remove(front);
            if(front.getTypeId() != 0 && front.getTypeId() != 8 && front.getTypeId() != 9)
                continue;
            
            //test to see if there is an empty block underneath
            if(canFlowDown(front)) {
                flowDown(front);
            } else { //can't go down, spread outwards
                //get average of 3x3 square
                ave = get3x3Average(front);
                if (ave == -1) { //if there was an error, do nothing
                    continue;
                }

                //set the level of the surrounding blocks
                flow3x3Area(front, ave);
            }
        }
        
        //Garbage Collect
        garbageCollect();
        
    }
    
    public int get3x3Average(Block front) {
        int ave = 0;
        int blocks = 0;
        Block temp;
        for (int i = -1; i < 2; ++i) {
            for (int j = -1; j < 2; ++j) {
                if ((i == j || i == -j) && (i != 0 || j != 0)) {
                    continue;
                }
                temp = front.getWorld().getBlockAt(front.getX() + j, front.getY(), front.getZ() + i);
                if (temp.getTypeId() == 8 || temp.getTypeId() == 9 || temp.getTypeId() == 0 || temp.getTypeId() == 19) {
                    if (waterData.get(temp) != null) {
                        ave += waterData.get(temp).intValue();
                    } else if (temp.getTypeId() == 8 || temp.getTypeId() == 9) {
                        waterData.put(temp, waterStartLevel);
                        ave += waterStartLevel;
                    } else {
                        waterData.put(temp, 0);
                    }
                    blocks += 1;
                }
            }
        }
        //if (blocks == 0) {
            //return -1;
        //}
        ave /= blocks;
        return ave;
    }

    public void flow3x3Area(Block front, int ave) {
        Block temp;
        for (int i = -1; i < 2; ++i) {
            for (int j = -1; j < 2; ++j) {
                if ((i == j || i == -j) && (i != 0 || j != 0)) {
                    continue;
                }
                temp = front.getWorld().getBlockAt(front.getX() + j, front.getY(), front.getZ() + i);
                if ((temp.getTypeId() == 8 || temp.getTypeId() == 9 || temp.getTypeId() == 0)) {
                    if (Math.abs(waterData.get(temp).intValue() - ave) > (int) (waterData.get(temp).intValue() * minimumDifferencelevelFraction)) {
                        if (ave > 0) {
                            temp.setTypeId(8);
                        } else {
                            temp.setTypeId(0);
                        }
                        waterData.put(temp, ave);
                        
                        if (i != 0 || j != 0) {
                            waterFlows.add(temp);
                        }
                    }
                }
            }
        }
    }
    
    public void garbageCollect() {
        if (waterFlows.isEmpty() && !waterData.isEmpty()) {
            LinkedHashSet<Block> remove = new LinkedHashSet<Block>();
            for (Block b : waterData.keySet()) {
                if (waterData.get(b).intValue() == 0) {
                    remove.add(b);
                }
            }
            for (Block b : remove) {
                waterData.remove(b);
            }
            remove.clear();
        }
    }
    
    public boolean canFlowDown(Block front) {
        Block below = front.getWorld().getBlockAt(front.getX(), front.getY()-1, front.getZ());
        Integer data = waterData.get(below);
        
        if(waterData.get(front).intValue() == 0)
            return false;
        
        if(below != null && (
           below.getTypeId() == 0 ||
           below.getTypeId() == 8 ||
           below.getTypeId() == 9)) {
               if(data == null) {
                   if(below.getTypeId() == 0) {
                       waterData.put(below, 0);
                       return true;
                   }
                   waterData.put(below, waterStartLevel);
                   return false;
               } else if(data.intValue() == waterStartLevel)
                   return false;
               return true;
           }
        
        return false;
    }
    
    public void flowDown(Block front) {
        Block below = front.getWorld().getBlockAt(front.getX(), front.getY()-1, front.getZ());
        int flowAmount = (int)(waterData.get(front).intValue() * flowDownFraction);
        if((waterStartLevel - waterData.get(below).intValue()) < flowAmount)
            flowAmount = waterStartLevel - waterData.get(below).intValue();
        if(flowAmount == 0)
            flowAmount = 1;
        waterData.put(front,waterData.get(front).intValue() - flowAmount);
        waterData.put(below,waterData.get(below).intValue() + flowAmount);
        if(waterData.get(front).intValue() == 0)
            front.setTypeId(0);
        below.setTypeId(8);
        
        waterFlows.add(below);
        waterFlows.add(front);
    }
}
