package mindless728.RealFluids;

import org.bukkit.block.Block;
import org.bukkit.World;

public class RealFluidsBlock {
    private World world;
	private int x;
	private int y;
	private int z;
	private int fluidLevel;
	
	public RealFluidsBlock(World w, int x, int y, int z, int level) {
	    world = w;
		this.x = x;
		this.y = y;
		this.z = z;
		fluidLevel = level;
	}
	
	public int getX() {
	    return x;
	}
	
	public int getY() {
	    return y;
	}
	
	public int getZ() {
	    return z;
	}
	
	public World getWorld() {
		return world;
	}
	
	public void setLocation(int x, int y, int z) {
		if(world != null)
			return;
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public int getLevel() {
	    return fluidLevel;
	}
	
	public void setLevel(int level) {
		if(getTypeId() != 19)
			fluidLevel = level;
	}
	
	public int getTypeId() {
	    return world.getBlockTypeIdAt(x, y, z);
	}
	
	public void setTypeId(int type) {
	    world.getBlockAt(x,y,z).setTypeId(type);
	}
	
	@Override
	public int hashCode() {
		return ((y & 0xFF) << 24) + ((x & 0xFFF) << 12) + (z & 0xFFF);
	}
	
	@Override
	public boolean equals(Object o) {
		RealFluidsBlock rfb = null;
		if(o instanceof RealFluidsBlock) {
			rfb = (RealFluidsBlock)o;
			return ((world == null) || (world == rfb.world)) &&
			       (x == rfb.x) &&
				   (y == rfb.y) &&
				   (z == rfb.z);
		}
		return false;
	}
}