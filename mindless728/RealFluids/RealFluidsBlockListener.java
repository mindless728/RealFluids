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
        if(block.getTypeId() == 8 || block.getTypeId() == 9)
            plugin.addFluidFlow(block);
        plugin.removeFluidFlow(block);
    }
    
    @Override
    public void onBlockFlow(BlockFromToEvent event) {
        plugin.addFluidFlow(event.getBlock());
        event.setCancelled(true);
    }
}
