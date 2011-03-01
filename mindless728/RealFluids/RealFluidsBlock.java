package mindless728.RealFluids;

import org.bukkit.block.Block;
import org.bukkit.Location;
import org.bukkit.World;

public class RealFluidsBlock {
	Location location;
	private int fluidLevel;
	
	public RealFluidsBlock(Location loc, int level) {
		this(loc.getWorld(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(), level);
	}
	
	public RealFluidsBlock(World w, int x, int y, int z, int level) {
	    location = new Location(w,x,y,z);
		fluidLevel = level;
	}
	
	public Location getLocation() {
		return location;
	}
	
	public int getX() {
	    return location.getBlockX();
	}
	
	public int getY() {
	    return location.getBlockY();
	}
	
	public int getZ() {
	    return location.getBlockZ();
	}
	
	public World getWorld() {
		return location.getWorld();
	}
	
	public void setLocation(int x, int y, int z) {
		if(location.getWorld() != null)
			return;
		location.setX(x);
		location.setY(y);
		location.setZ(z);
	}
	
	public int getLevel() {
	    return fluidLevel;
	}
	
	public void setLevel(int level) {
		if(getTypeId() != 19)
			fluidLevel = level;
	}
	
	public int getTypeId() {
	    return location.getWorld().getBlockTypeIdAt(location);
	}
	
	public void setTypeId(int type) {
	    location.getBlock().setTypeId(type);
	}
	
	@Override
	public int hashCode() {
		return location.hashCode();
		/*
		int x = location.getBlockX(),
		    y = location.getBlockY(),
			z = location.getBlockZ();
		return ((y & 0xFF) << 24) + ((x & 0xFFF) << 12) + (z & 0xFFF);
		*/
	}
	
	@Override
	public boolean equals(Object o) {
		RealFluidsBlock rfb = null;
		if((o != null) && (o instanceof RealFluidsBlock)) {
			rfb = (RealFluidsBlock)o;
			return ((location.getWorld() == null) || (location.getWorld() == rfb.location.getWorld())) &&
			       location.equals(rfb.location) && 
				   (fluidLevel == rfb.fluidLevel);
		}
		return false;
	}
}