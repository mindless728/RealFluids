package mindless728.RealFluids;

import org.bukkit.block.Block;
import org.bukkit.Location;
import org.bukkit.World;

import mindless728.BlockStorage.PluginBlockStorage;

public class RealFluidsBlock {
	private static PluginBlockStorage<Integer> storage;
	private static RealFluids plugin;
	private Location location;
	
	public RealFluidsBlock(Location loc) {
		this(loc.getWorld(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
	}
	
	public RealFluidsBlock(World w, int x, int y, int z) {
	    location = new Location(w,x,y,z);
	}
	
	public static void setStorage(PluginBlockStorage<Integer> pbs) {
		storage = pbs;
	}
	
	public static void setPlugin(RealFluids p) {
		plugin = p;
	}
	
	public Location getLocation() {
		return location;
	}
	
	public Block getBlock() {
		return location.getBlock();
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
		Integer i = storage.getData(location);
		int id = getTypeId();
	    if(i == null) {
			if(id == 8 || id == 9)
				setLevel(plugin.getWaterStartLevel());
			else if(id == 10 || id == 11)
				setLevel(plugin.getLavaStartLevel());
			else
				setLevel(0);
			i = storage.getData(location);
		}
		return i;
	}
	
	public void setLevel(int level) {
		if(getTypeId() != 19)
			storage.setData(location,level);
		else
			storage.setData(location,0);
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
			       location.equals(rfb.location);
		}
		return false;
	}
}
