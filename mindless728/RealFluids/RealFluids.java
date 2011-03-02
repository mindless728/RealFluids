/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package mindless728.RealFluids;

import org.bukkit.block.Block;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoader;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.Server;
import org.bukkit.World;

import java.io.File;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;

/**
 *
 * @author colin
 */
public class RealFluids extends JavaPlugin implements Runnable {
    BukkitScheduler scheduler;
    RealFluidsBlockListener blockListener;
	LinkedHashSet<RealFlowEvent> flowEvents;
	HashMap<Location,RealFluidsBlock> blockData;
	Location tempLoc;
	
    int waterStartLevel = 2000;
	int lavaStartLevel = 1000;
    double minimumDifferenceLevelFraction = 0.05;
    int repeatRate = 1;
	//long maxFlowTimePerRepeat = 25000000;
    double simsPerRepeatFraction = 0.5;
    double flowDownFraction = 0.5;
    
    public RealFluids() {
        blockListener = new RealFluidsBlockListener(this);
		flowEvents = new LinkedHashSet<RealFlowEvent>();
		blockData = new HashMap<Location,RealFluidsBlock>();
		
		RealFlowEvent.setPlugin(this);
		tempLoc = new Location(null,0,0,0);
    }
    
    public void onEnable() {
        scheduler = this.getServer().getScheduler();
        scheduler.scheduleSyncRepeatingTask(this, this, 1, repeatRate);
        
        getServer().getPluginManager().registerEvent(Type.BLOCK_PLACED, blockListener, Priority.Low, this);
        getServer().getPluginManager().registerEvent(Type.BLOCK_FLOW, blockListener, Priority.Low, this);
		getServer().getPluginManager().registerEvent(Type.BLOCK_DAMAGED, blockListener, Priority.Low, this);
        
        System.out.println(getDescription().getName()+" version "+getDescription().getVersion()+" enabled");
    }
    
    public void onDisable() {
        System.out.println(getDescription().getName()+" version "+getDescription().getVersion()+" disabled");
    }
	
	public int getWaterStartLevel() {
		return waterStartLevel;
	}
	
	public int getLavaStartLevel() {
		return lavaStartLevel;
	}
	
	public void addFlowEvent(RealFluidsBlock block) {
		flowEvents.add(new RealFlowEvent(block));
	}
	
	public boolean isOverwrittable(int typeId) {
		return ((typeId == 0) || (typeId == 78));
	}
	
	public void overwriteBlock(RealFluidsBlock rfb, int typeId) {
		int oldId = rfb.getTypeId();
		if(oldId == 78)
			rfb.getWorld().dropItemNaturally(rfb.getLocation(),new ItemStack(78,1));
		rfb.setTypeId(typeId);
	}
	
	public RealFluidsBlock getBlock(int x, int y, int z, World world) {
		tempLoc.setWorld(world);
		tempLoc.setX(x);
		tempLoc.setY(y);
		tempLoc.setZ(z);
		return getBlock(tempLoc);
	}
	
	public RealFluidsBlock getBlock(Location loc) {
		RealFluidsBlock rfb = null;
		int blockId = 0;
				
		//if the block exists in the server get it
		if(blockData.containsKey(loc)) {
			rfb = blockData.get(loc);
		//if not, create the block and assign data to the block
		} else if(loc.getBlockY() >= 0 && loc.getBlockY() <= 127) {
			rfb = new RealFluidsBlock(loc,0);
			blockId = rfb.getTypeId();
			//get starting level
			if(blockId == 8 || blockId == 9)
				rfb.setLevel(waterStartLevel);
			else if(blockId == 10 || blockId == 11)
				rfb.setLevel(lavaStartLevel);
			blockData.put(rfb.getLocation(), rfb);
		}
		
		return rfb;
	}
	
	public RealFluidsBlock getAboveBlock(RealFluidsBlock block) {
		Location loc = block.getLocation();
		return getBlock(loc.getBlockX(), loc.getBlockY()+1, loc.getBlockZ(), loc.getWorld());
	}
	
	public RealFluidsBlock getBelowBlock(RealFluidsBlock block) {
		Location loc = block.getLocation();
		return getBlock(loc.getBlockX(), loc.getBlockY()-1, loc.getBlockZ(), loc.getWorld());
	}
	
	public LinkedList<RealFluidsBlock> getHorizontalAdjacentBlocks(RealFluidsBlock block) {
		LinkedList<RealFluidsBlock> lrfb = new LinkedList<RealFluidsBlock>();
		RealFluidsBlock temp = null;
		Location loc = block.getLocation();
		
		for(int i = -1; i < 2; ++i) {
			for(int j = -1; j < 2; ++j) {
				if(i == j || i == -j)
					continue;
				temp = getBlock(loc.getBlockX()+j, loc.getBlockY(), loc.getBlockZ()+i, loc.getWorld());
				if(temp != null)
					lrfb.addLast(temp);
			}
		}
		
		return lrfb;
	}
	
	public LinkedList<RealFluidsBlock> getAdjacentBlocks(RealFluidsBlock block) {
		LinkedList<RealFluidsBlock> lrfb = getHorizontalAdjacentBlocks(block);
		RealFluidsBlock temp = null;
		
		temp = getAboveBlock(block);
		if(temp != null)
			lrfb.addLast(temp);
		temp = getBelowBlock(block);
		if(temp != null)
			lrfb.addLast(temp);
		
		return lrfb;
	}
	
	public int getBlockAverage(RealFluidsBlock source, LinkedList<RealFluidsBlock> lrfb) {
		int ave = source.getLevel();
		int fluids = 1;
		int tempId = 0;
		int sourceId = source.getTypeId();
		
		for(RealFluidsBlock rfb : lrfb) {
			tempId = rfb.getTypeId();
			if(isOverwrittable(tempId) || sameFluid(sourceId, tempId)) {
				ave += rfb.getLevel();
				++fluids;
			} else if(tempId == 19)
				++fluids;
		}
		
		return ave/fluids;
	}
	
	public boolean blockCanFlowDown(RealFluidsBlock block) {
		RealFluidsBlock below = getBelowBlock(block);
		if(below == null)
			return false;
		
		int blockId = block.getTypeId();
		int belowId = below.getTypeId();
		int startLevel = ((blockId == 8 || blockId == 9) ? waterStartLevel : lavaStartLevel);
		
		if((isOverwrittable(belowId) || sameFluid(blockId, belowId)) && (below.getLevel() < startLevel) && block.getLevel() != 0)
			return true;
		
		return false;
	}
	
	public boolean blockCanFlowUp(RealFluidsBlock block) {
		RealFluidsBlock above = getAboveBlock(block);
		if(above == null)
			return false;
			
		int blockId = block.getTypeId();
		int aboveId = above.getTypeId();
		int startLevel = ((blockId == 8 || blockId == 9) ? waterStartLevel : lavaStartLevel);
		
		if((block.getLevel() > startLevel) && sameFluid(blockId,aboveId) && (block.getLevel() > above.getLevel())) {
			return true;
		}
				
		return false;
	}
	
	public boolean sameFluid(int aId, int bId) {
		if((aId == 8 || aId == 9) && (bId == 8 || bId == 9))
			return true;
		if((aId == 10 || aId == 11) && (bId == 10 || bId == 11))
			return true;
		return false;
	}
	
	public void flowHorizontal(RealFluidsBlock block) {
		LinkedList<RealFluidsBlock> lrfb = getHorizontalAdjacentBlocks(block);
		RealFluidsBlock above = getAboveBlock(block);
		int blockId = block.getTypeId();
		int tempId = 0;
		int ave = getBlockAverage(block,lrfb);
		int diff = 0;
		int minDiff = 0;
		
		for(RealFluidsBlock rfb : lrfb) {
			diff = ave - rfb.getLevel();
			minDiff = (int)(minimumDifferenceLevelFraction * rfb.getLevel());
			tempId = rfb.getTypeId();
			if((Math.abs(diff) > minDiff) && (isOverwrittable(tempId) || tempId == 19 || sameFluid(tempId, blockId))) {
				rfb.setLevel(rfb.getLevel()+diff);
				if(isOverwrittable(tempId))
					overwriteBlock(rfb,blockId);
				block.setLevel(block.getLevel()-diff);
				if(ave != 0)
					flowEvents.add(new RealFlowEvent(rfb));
			}
		}
		
		if(ave == 0 || block.getLevel() == 0) {
			block.setLevel(0);
			block.setTypeId(0);
		}
		if(ave != 0)
			flowEvents.add(new RealFlowEvent(block));
		if(blockCanFlowDown(above)) {
			addFlowEvent(above);
		}
	}
	
	public void flowDown(RealFluidsBlock block) {
		LinkedList<RealFluidsBlock> lrfb = getHorizontalAdjacentBlocks(block);
		RealFluidsBlock below = getBelowBlock(block);
		int flowAmount = ((int)(flowDownFraction*block.getLevel()));
		int startLevel = ((block.getTypeId() == 8 || block.getTypeId() == 9)?waterStartLevel:lavaStartLevel);
		
		if(flowAmount > (startLevel - below.getLevel()))
			flowAmount = startLevel - below.getLevel();
		else if(flowAmount == 0)
			flowAmount = 1;
		
		below.setLevel(below.getLevel()+flowAmount);
		block.setLevel(block.getLevel()-flowAmount);
		overwriteBlock(below,block.getTypeId());
		if(block.getLevel() == 0)
			block.setTypeId(0);
			
		flowEvents.add(new RealFlowEvent(below));
		flowEvents.add(new RealFlowEvent(block));
		
		for(RealFluidsBlock rfb : lrfb) {
			if(sameFluid(block.getTypeId(), rfb.getTypeId()))
				addFlowEvent(rfb);
		}
	}
	
	public void flowUp(RealFluidsBlock block) {
		RealFluidsBlock above = getAboveBlock(block);
		int startLevel = ((block.getTypeId() == 8 || block.getTypeId() == 9)?waterStartLevel:lavaStartLevel);
		int diff = block.getLevel()-startLevel;
		block.setLevel(startLevel);
		above.setLevel(above.getLevel()+diff);
		above.setTypeId(block.getTypeId());
	}
	
	public void run() {
		int sims = (int)(flowEvents.size() * simsPerRepeatFraction);
		int i = 0;
		RealFlowEvent front = null;
		RealFluidsFlowType flow = null;
		//long time;
		//long elapsed = 0;
		
		if(sims == 0)
			sims = 1;
			
		//time = System.nanoTime();
		
		for(i = 0; i < sims; ++i) {
			if(flowEvents.isEmpty()/* || (elapsed > maxFlowTimePerRepeat)*/)
				break;
			
			front = flowEvents.iterator().next();
			flowEvents.remove(front);
			
			getServer().getPluginManager().callEvent(front);
			if(front.isCancelled())
				continue;
			
			flow = front.getFlow();
			if(flow == RealFluidsFlowType.WATER_HORIZONTAL ||
			   flow == RealFluidsFlowType.LAVA_HORIZONTAL ||
			   flow == RealFluidsFlowType.AIR_HORIZONTAL) {
				flowHorizontal(front.getBlock());
			} else if(flow == RealFluidsFlowType.WATER_DOWN || flow == RealFluidsFlowType.LAVA_DOWN) {
				flowDown(front.getBlock());
			} else if(flow == RealFluidsFlowType.WATER_UP || flow == RealFluidsFlowType.LAVA_UP) {
				flowUp(front.getBlock());
			}
			//elapsed = System.nanoTime() - time;
			//System.out.println("Elapsed: "+elapsed/1000);
		}
		
		//time = System.nanoTime() - time;
		/*if(i != 0)
			System.out.println("Sims: "+i+", Time: "+time+", Sims/s: "+((long)1000000000)*i/time);*/
	}
}
