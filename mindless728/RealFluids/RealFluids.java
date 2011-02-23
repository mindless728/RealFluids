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
import org.bukkit.World;

import java.io.File;

import java.util.HashMap;
import java.util.LinkedHashSet;

/**
 *
 * @author colin
 */
public class RealFluids extends JavaPlugin implements Runnable {
    BukkitScheduler scheduler;
    RealFluidsBlockListener blockListener;
	LinkedHashSet<RealFlowEvent> flowEvents;
	HashMap<RealFluidsBlock,RealFluidsBlock> blockData;
	RealFluidsBlock tempBlock;
	
    int waterStartLevel = 2000;
	int lavaStartLevel = 1000;
    double minimumDifferenceLevelFraction = 0.05;
    int repeatRate = 1;
    int simsMaxPerRepeat = Integer.MAX_VALUE;
    double simsPerRepeatFraction = 0.5;
    double flowDownFraction = 0.5;
    
    public RealFluids() {
        blockListener = new RealFluidsBlockListener(this);
		flowEvents = new LinkedHashSet<RealFlowEvent>();
		blockData = new HashMap<RealFluidsBlock,RealFluidsBlock>();
		
		RealFlowEvent.setPlugin(this);
		tempBlock = new RealFluidsBlock(null,0,0,0,0);
    }
    
    public void onEnable() {
        scheduler = this.getServer().getScheduler();
        scheduler.scheduleSyncRepeatingTask(this, this, 1, repeatRate);
        
        getServer().getPluginManager().registerEvent(Type.BLOCK_PLACED, blockListener, Priority.Low, this);
        getServer().getPluginManager().registerEvent(Type.BLOCK_FLOW, blockListener, Priority.Low, this);
        
        System.out.println(getDescription().getName()+" version "+getDescription().getVersion()+" enabled");
    }
    
    public void onDisable() {
        System.out.println(getDescription().getName()+" version "+getDescription().getVersion()+" disabled");
    }
	
	public void addFlowEvent(RealFluidsBlock block) {
		flowEvents.add(new RealFlowEvent(block));
	}
	
	public RealFluidsBlock getBlock(int x, int y, int z, World world) {
		tempBlock.setLocation(x,y,z);
		return getBlock(tempBlock, world);
	}
		
	public RealFluidsBlock getBlock(RealFluidsBlock lookup, World world) {
		RealFluidsBlock block = null;
		int blockId = 0;
		
		if(blockData.containsKey(lookup)) {
			block = blockData.get(lookup);
		} else {
			if(lookup.getY() >= 0 && lookup.getY() <= 127) {
				block = new RealFluidsBlock(world, lookup.getX(), lookup.getY(), lookup.getZ(), 0);
				blockId = block.getTypeId();
				if(blockId == 8 || blockId == 9)
					block.setLevel(waterStartLevel);
				else if(blockId == 10 || blockId == 11)
					block.setLevel(lavaStartLevel);
				blockData.put(block, block);
			}
		}
		
		return block;
	}
	
	public RealFluidsBlock getAboveBlock(RealFluidsBlock block) {
		tempBlock.setLocation(block.getX(), block.getY()+1, block.getZ());
		return getBlock(tempBlock, block.getWorld());
	}
	
	public RealFluidsBlock getBelowBlock(RealFluidsBlock block) {
		tempBlock.setLocation(block.getX(), block.getY()-1, block.getZ());
		return getBlock(tempBlock, block.getWorld());
	}
	
	public RealFluidsBlock[][] get3x3Blocks(RealFluidsBlock block) {
		RealFluidsBlock[][] blocks = new RealFluidsBlock[3][3];
		for(int i = -1; i < 2; ++i) {
			for(int j = -1; j < 2; ++j) {
				if(i == 0 && j == 0)
					blocks[i+1][j+1] = block;
				else {
					tempBlock.setLocation(block.getX()+j, block.getY(), block.getZ()+i);
					blocks[i+1][j+1] = getBlock(tempBlock, block.getWorld());
				}
			}
		}
		return blocks;
	}
	
	public int get3x3Average(RealFluidsBlock[][] blocks) {
		int ave = 0;
		int fluids = 0;
		RealFluidsBlock center = blocks[1][1];
		
		for(int i = -1; i < 2; ++i) {
			for(int j = -1; j < 2; ++j) {
				if((i == j || i == -j) && (i != 0 || j != 0))
					continue;
				//System.out.println("i: "+i+", j: "+j+", type: "+blocks[i+1][j+1].getTypeId());
				if((blocks[i+1][j+1].getTypeId() == 0) || sameFluid(blocks[i+1][j+1].getTypeId(), center.getTypeId())) {
					ave += blocks[i+1][j+1].getLevel();
					++fluids;
				} else if(blocks[i+1][j+1].getTypeId() == 19)
					++fluids;
			}
		}
		
		/*if(fluids != 0 && center.getY() >= 64)
			System.out.println("Amount: "+ave+", Blocks: "+fluids+", Ave: "+ave/fluids);*/
		
		if(fluids != 0)
			return ave / fluids;
		return -1;
	}
	
	public RealFluidsBlock[][][] get3x3x3Blocks(RealFluidsBlock block) {
		RealFluidsBlock[][][] blocks = new RealFluidsBlock[3][3][3];
		for(int i = -1; i < 2; ++i) {
			for(int j = -1; j < 2; ++j) {
				for(int k = -1; k < 2; ++k) {
					if(i == 0 && j == 0 && k == 0)
						blocks[i+1][j+1][k+1] = block;
					else {
						tempBlock.setLocation(block.getX()+k, block.getY()+i, block.getZ()+j);
						blocks[i+1][j+1][k+1] = getBlock(tempBlock, block.getWorld());
					}
				}
			}
		}
		return blocks;
	}
	
	public boolean blockCanFlowDown(RealFluidsBlock block) {
		RealFluidsBlock below = getBelowBlock(block);
		if(below == null)
			return false;
		
		int blockId = block.getTypeId();
		int belowId = below.getTypeId();
		int startLevel = ((blockId == 8 || blockId == 9) ? waterStartLevel : lavaStartLevel);
		
		if((belowId == 0 || sameFluid(blockId, belowId)) && (below.getLevel() < startLevel) && block.getLevel() != 0)
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
		RealFluidsBlock[][] blocks = get3x3Blocks(block);
		int blockId = block.getTypeId();
		int ave = get3x3Average(blocks);
		int diff;
		int minDiff = (int)(minimumDifferenceLevelFraction * block.getLevel());
		
		if(ave == -1) {
			//System.out.println("Ave: "+ave);
			return;
		}
		
		for(int i = -1; i < 2; ++i) {
			for(int j = -1; j < 2; ++j) {
				if((i == j) || (i == -j))
					continue;
				//System.out.println("Start: "+blocks[i][j].getLevel()+", Ave: "+ave);
				diff = ave - blocks[i+1][j+1].getLevel();
				minDiff = (int)(minimumDifferenceLevelFraction * blocks[i+1][j+1].getLevel());
				if((Math.abs(diff) > minDiff) && (blocks[i+1][j+1].getTypeId() == 0 || blocks[i+1][j+1].getTypeId()==19 || sameFluid(blocks[i+1][j+1].getTypeId(),blockId))) {
					blocks[i+1][j+1].setLevel(blocks[i+1][j+1].getLevel()+diff);
					if(blocks[i+1][j+1].getTypeId() == 0)
						blocks[i+1][j+1].setTypeId(blockId);
					block.setLevel(block.getLevel()-diff);
					if(ave != 0)
						flowEvents.add(new RealFlowEvent(blocks[i+1][j+1]));
				}
			}
		}
		
		if(ave == 0 || block.getLevel() == 0) {
			block.setLevel(0);
			block.setTypeId(0);
		}
		if(ave != 0)
			flowEvents.add(new RealFlowEvent(block));
		
		//while(true){}
	}
	
	public void flowDown(RealFluidsBlock block) {
		RealFluidsBlock below = getBelowBlock(block);
		int flowAmount = ((int)(flowDownFraction*block.getLevel()));
		int startLevel = ((block.getTypeId() == 8 || block.getTypeId() == 9)?waterStartLevel:lavaStartLevel);
		
		if(flowAmount > (startLevel - below.getLevel()))
			flowAmount = startLevel - below.getLevel();
		else if(flowAmount == 0)
			flowAmount = 1;
		
		below.setLevel(below.getLevel()+flowAmount);
		block.setLevel(block.getLevel()-flowAmount);
		below.setTypeId(block.getTypeId());
		if(block.getLevel() == 0)
			block.setTypeId(0);
			
		flowEvents.add(new RealFlowEvent(below));
		flowEvents.add(new RealFlowEvent(block));
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
		long time;
		
		if(sims > simsMaxPerRepeat)
			sims = simsMaxPerRepeat;
		else if(sims == 0)
			sims = 1;
			
		time = System.nanoTime();
		
		for(i = 0; i < sims; ++i) {
			if(flowEvents.isEmpty()/* || (time > 25000000)*/)
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
		}
		
		time = System.nanoTime() - time;
		/*if(i != 0)
			System.out.println("Sims: "+i+", Time: "+time+", Sims/s: "+((long)1000000000)*i/time);*/
	}
}
