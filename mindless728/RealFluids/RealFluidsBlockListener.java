/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package mindless728.RealFluids;

import org.bukkit.block.Block;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPlaceEvent;

/**
 *
 * @author colin
 */
public class RealFluidsBlockListener extends BlockListener {
    RealFluids plugin;
    
    RealFluidsBlockListener(RealFluids p) {
        plugin = p;
    }
    
    @Override
    public void onBlockPlace(BlockPlaceEvent event) {
        Block block = event.getBlockPlaced();
		RealFluidsBlock rfblock = plugin.getBlock(block.getX(), block.getY(), block.getZ(), block.getWorld());
		//add flow event if block can flow
		if(block.getTypeId() == 8 || block.getTypeId() == 9 ||
		   block.getTypeId() == 10 || block.getTypeId() == 11) {
			if(block.getTypeId() == 8 || block.getTypeId() == 9)
				rfblock.setLevel(plugin.waterStartLevel);
			else
				rfblock.setLevel(plugin.lavaStartLevel);
			plugin.addFlowEvent(rfblock);
		}
		//try to remove a flow event at that location if it can't
		else
			rfblock.setLevel(0);
    }
    
    @Override
    public void onBlockFlow(BlockFromToEvent event) {
		//add flow event for fluid
		Block block = event.getBlock();
		RealFluidsBlock rfblock = plugin.getBlock(block.getX(), block.getY(), block.getZ(), block.getWorld());
		plugin.addFlowEvent(rfblock);
        event.setCancelled(true);
    }
}
