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

import mindless728.BlockStorage.BlockStorage;
import mindless728.BlockStorage.PluginBlockStorage;

import java.io.File;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.util.Map;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Scanner;

/**
 *
 * @author colin
 */
public class RealFluids extends JavaPlugin implements Runnable {
    BukkitScheduler scheduler;
    RealFluidsBlockListener blockListener;
	RealFluidsPlayerListener playerListener;
	LinkedHashSet<RealFlowEvent> flowEvents;
	PluginBlockStorage<Integer> storage;
	Location tempLoc;
	String dataFile;
	
    int waterStartLevel = 2000;
	int lavaStartLevel = 1000;
    double minimumDifferenceLevelFraction = 0.05;
    int repeatRate = 1;
	long maxFlowTimePerRepeat = 25000000;
    double simsPerRepeatFraction = 0.5;
    double flowDownFraction = 0.5;
	int chunkCacheSize = 1024;
	HashMap<Integer,Boolean> waterOverwriteList;
	HashMap<Integer,Boolean> lavaOverwriteList;
    
    public RealFluids() {
        blockListener = new RealFluidsBlockListener(this);
		playerListener = new RealFluidsPlayerListener(this);
		flowEvents = new LinkedHashSet<RealFlowEvent>();
		waterOverwriteList = new HashMap<Integer,Boolean>();
		lavaOverwriteList = new HashMap<Integer,Boolean>();
		
		RealFlowEvent.setPlugin(this);
		tempLoc = new Location(null,0,0,0);
    }
    
    public void onEnable() {
		Object o = getServer().getPluginManager().getPlugin("BlockStorage");
		if(o == null || !(o instanceof BlockStorage)) {
			System.out.println("BlockStorage not found, disabling...");
			getServer().getPluginManager().disablePlugin(this);
			return;
		}
		storage = ((BlockStorage)o).<Integer>getPluginBlockStorage(this, chunkCacheSize);
		if(storage == null) {
			System.out.println("Could not grab a storage object, disabling...");
			getServer().getPluginManager().disablePlugin(this);
			return;
		}
		RealFluidsBlock.setStorage(storage);
		RealFluidsBlock.setPlugin(this);

		getDataFolder().mkdir();
		dataFile = getDataFolder().getPath()+File.separatorChar+"RealFluids.txt";
		loadProperties();
	
    	scheduler = this.getServer().getScheduler();
    	scheduler.scheduleSyncRepeatingTask(this, this, 1, repeatRate);
        
    	getServer().getPluginManager().registerEvent(Type.BLOCK_PLACE, blockListener, Priority.Low, this);
    	getServer().getPluginManager().registerEvent(Type.BLOCK_FROMTO, blockListener, Priority.Low, this);
		getServer().getPluginManager().registerEvent(Type.BLOCK_BREAK, blockListener, Priority.Low, this);

		getServer().getPluginManager().registerEvent(Type.PLAYER_BUCKET_EMPTY, playerListener, Priority.Low, this);
		getServer().getPluginManager().registerEvent(Type.PLAYER_BUCKET_FILL, playerListener, Priority.Low, this);
        
        System.out.println(getDescription().getName()+" version "+getDescription().getVersion()+" enabled");
    }
    
    public void onDisable() {
        System.out.println(getDescription().getName()+" version "+getDescription().getVersion()+" disabled");
    }

	public void loadProperties() {
		String s;
		int i;
		boolean b;
		Scanner scanner;
		try {
			scanner = new Scanner(new File(dataFile));
			while(scanner.hasNext()) {
				s = scanner.next();
				if(s.equals("WaterStartLevel:"))
					waterStartLevel = scanner.nextInt();
				else if(s.equals("LavaStartLevel:"))
					lavaStartLevel = scanner.nextInt();
				else if(s.equals("MinimumDifferenceLevelFraction:"))
					minimumDifferenceLevelFraction = scanner.nextDouble();
				else if(s.equals("RepeatRate:"))
					repeatRate = scanner.nextInt();
				else if(s.equals("MaxFlowTimePerRepeat:"))
					maxFlowTimePerRepeat = scanner.nextLong();
				else if(s.equals("SimsPerRepeatFraction:"))
					simsPerRepeatFraction = scanner.nextDouble();
				else if(s.equals("FlowDownFraction:"))
					flowDownFraction = scanner.nextDouble();
				else if(s.equals("ChunkCacheSize:"))
					chunkCacheSize = scanner.nextInt();
				else if(s.equals("WaterOverwriteList:")) {
					while(scanner.hasNextInt()) {
						i = scanner.nextInt();
						b = scanner.nextBoolean();
						waterOverwriteList.put(i,b);
					}
				} else if(s.equals("LavaOverwriteList:")) {
					while(scanner.hasNextInt()) {
						i = scanner.nextInt();
						b = scanner.nextBoolean();
						lavaOverwriteList.put(i,b);
					}
				}
			}
		} catch(FileNotFoundException fnfe) {
			saveProperties();
		} catch(Exception e) {
			System.out.println("*** RealFluids: Error in configuration file ***");
		}
	}

	public void saveProperties() {
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(dataFile));
			writer.write("WaterStartLevel: "+waterStartLevel);
			writer.newLine();
			writer.write("LavaStartLevel: "+lavaStartLevel);
			writer.newLine();
			writer.write("MinimumDifferenceLevelFraction: "+minimumDifferenceLevelFraction);
			writer.newLine();
			writer.write("RepeatRate: "+repeatRate);
			writer.newLine();
			writer.write("MaxFlowTimePerRepeat: "+maxFlowTimePerRepeat);
			writer.newLine();
			writer.write("SimsPerRepeatFraction: "+simsPerRepeatFraction);
			writer.newLine();
			writer.write("FlowDownFraction: "+flowDownFraction);
			writer.newLine();
			writer.write("ChunkCacheSize: "+chunkCacheSize);
			writer.newLine();
			writer.write("WaterOverwriteList:");
			for(Map.Entry e : waterOverwriteList.entrySet()) {
				writer.write(" "+e.getKey()+" "+e.getValue());
			}
			writer.newLine();
			writer.write("LavaOverwriteList:");
			for(Map.Entry e : lavaOverwriteList.entrySet()) {
				writer.write(" "+e.getKey()+" "+e.getValue());
			}
			writer.newLine();
			writer.close();
		} catch(IOException ioe) {}
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
	
	public boolean isOverwrittable(int fluidType, int typeId) {
		if(fluidType == 8 || fluidType == 9)
			return waterOverwriteList.containsKey(typeId);
		else
			return lavaOverwriteList.containsKey(typeId);
	}
	
	public void overwriteBlock(RealFluidsBlock rfb, int fluidId) {
		int oldId = rfb.getTypeId();
		try {
			if((fluidId == 8 || fluidId == 9) && waterOverwriteList.get(oldId))
				rfb.getWorld().dropItemNaturally(rfb.getLocation(), new ItemStack(oldId,1));
			else if((fluidId == 10 || fluidId == 11) && lavaOverwriteList.get(oldId))
				rfb.getWorld().dropItemNaturally(rfb.getLocation(), new ItemStack(oldId,1));
		} catch(Exception e) {}
		rfb.setTypeId(fluidId);
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
		
		rfb = new RealFluidsBlock(loc);
		
		return rfb;
	}
	
	public RealFluidsBlock getNorthBlock(RealFluidsBlock block) {
		Location loc = block.getLocation();
		return getBlock(loc.getBlockX()-1, loc.getBlockY(), loc.getBlockZ(), loc.getWorld());
	}
	
	public RealFluidsBlock getSouthBlock(RealFluidsBlock block) {
		Location loc = block.getLocation();
		return getBlock(loc.getBlockX()+1, loc.getBlockY(), loc.getBlockZ(), loc.getWorld());
	}
	
	public RealFluidsBlock getEastBlock(RealFluidsBlock block) {
		Location loc = block.getLocation();
		return getBlock(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()-1, loc.getWorld());
	}
	
	public RealFluidsBlock getWestBlock(RealFluidsBlock block) {
		Location loc = block.getLocation();
		return getBlock(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()+1, loc.getWorld());
	}
	
	public RealFluidsBlock getAboveBlock(RealFluidsBlock block) {
		Location loc = block.getLocation();
		if(loc.getBlockY() == 127)
			return null;
		return getBlock(loc.getBlockX(), loc.getBlockY()+1, loc.getBlockZ(), loc.getWorld());
	}
	
	public RealFluidsBlock getBelowBlock(RealFluidsBlock block) {
		Location loc = block.getLocation();
		if(loc.getBlockY() == 0)
			return null;
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
			if(isOverwrittable(sourceId, tempId) || sameFluid(sourceId, tempId)) {
				ave += rfb.getLevel();
				++fluids;
			} else if(tempId == 19)
				++fluids;
		}
		
		return ave/fluids;
	}
	
	public boolean blockCanFlowDown(RealFluidsBlock block) {
		if(block == null)
			return false;

		RealFluidsBlock below = getBelowBlock(block);
		
		if(below == null || block.getY() == 0)
			return false;
		
		int blockId = block.getTypeId();
		int belowId = below.getTypeId();
		int startLevel = ((blockId == 8 || blockId == 9) ? waterStartLevel : lavaStartLevel);
		
		if((isOverwrittable(blockId, belowId) || sameFluid(blockId, belowId)) && (below.getLevel() < startLevel) && block.getLevel() != 0)
			return true;
		
		return false;
	}
	
	public boolean blockCanFlowUp(RealFluidsBlock block) {
		if(block == null)
			return false;

		RealFluidsBlock above = getAboveBlock(block);

		if(above == null)
			return false;
			
		int blockId = block.getTypeId();
		int aboveId = above.getTypeId();
		int startLevel = ((blockId == 8 || blockId == 9) ? waterStartLevel : lavaStartLevel);
		
		if((block.getLevel() > startLevel) && (aboveId == 0 || sameFluid(blockId,aboveId)) && (block.getLevel() > above.getLevel())) {
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
	
	public boolean isFluid(int id) {
		return ((id >= 8) && (id <= 11));
	}
	
	public int getMetaData(int fluidLevel) {
		int data = 15*(fluidLevel-1)/waterStartLevel+1;
		if(data == 15)
			data = 0;
			
		return data;
	}
	
	public void flowHorizontal(RealFluidsBlock block) {
		LinkedList<RealFluidsBlock> lrfb = getHorizontalAdjacentBlocks(block);
		RealFluidsBlock above = null;
		int blockId = block.getTypeId();
		int tempId = 0;
		int ave = getBlockAverage(block,lrfb);
		int diff = 0;
		int minDiff = 0;
		
		if(block.getY() < 127)
			above = getAboveBlock(block);
		
		for(RealFluidsBlock rfb : lrfb) {
			diff = ave - rfb.getLevel();
			minDiff = (int)(minimumDifferenceLevelFraction * rfb.getLevel());
			tempId = rfb.getTypeId();
			if((Math.abs(diff) > minDiff) && (isOverwrittable(blockId, tempId) || tempId == 19 || sameFluid(tempId, blockId))) {
				rfb.setLevel(rfb.getLevel()+diff);
				//rfb.getBlock().setData((byte)getMetaData(rfb.getLevel()));
				//if(rfb.getLevel() == 0)
					//rfb.setTypeId(0);
				if(isOverwrittable(blockId, tempId))
					overwriteBlock(rfb,blockId);
				block.setLevel(block.getLevel()-diff);
				//block.getBlock().setData((byte)getMetaData(block.getLevel()));
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
		addFlowEvent(above);
		addFlowEvent(block);
	}
	
	public void run() {
		int sims = (int)(flowEvents.size() * simsPerRepeatFraction);
		int i = 0;
		RealFlowEvent front = null;
		RealFluidsFlowType flow = null;
		long time;
		long elapsed = 0;
		
		if(sims == 0)
			sims = 1;
			
		time = System.nanoTime();
		
		for(i = 0; i < sims; ++i) {
			if(flowEvents.isEmpty() || (elapsed > maxFlowTimePerRepeat))
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
				flowHorizontal(front.getBlock());
				if(blockCanFlowUp(front.getBlock()))
					flowUp(front.getBlock());
			}
			elapsed = System.nanoTime() - time;
			//System.out.println("Elapsed: "+elapsed);
		}
		
		//time = System.nanoTime() - time;
		/*if(i != 0)
			System.out.println("Sims: "+i+", Time: "+time+", Sims/s: "+((long)1000000000)*i/time);*/
	}
}
