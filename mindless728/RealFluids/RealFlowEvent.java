package mindless728.RealFluids;

import org.bukkit.event.Event;
import org.bukkit.event.Cancellable;

public class RealFlowEvent extends Event implements Cancellable {
	private static RealFluids plugin;
    private RealFluidsBlock block;
	private RealFluidsFlowType flow;
	boolean cancelled;
	private static final long serialVersionUID = 1L;
	
	RealFlowEvent(RealFluidsBlock b) {
		super("RealFlowEvent");
		block = b;
		flow = null;
		cancelled = false;
	}
	
	public static void setPlugin(RealFluids p) {
		plugin = p;
	}
	
	public RealFluidsBlock getBlock() {
	    return block;
	}
	
	public RealFluidsFlowType getFlow() {
		int typeId = block.getTypeId();
		if(typeId != 0 && (typeId < 8 || typeId > 11)) {
			flow = RealFluidsFlowType.NO_FLOW;
		}
		else if(plugin.blockCanFlowDown(block)) {
			if(typeId == 8 || typeId == 9)
			    flow = RealFluidsFlowType.WATER_DOWN;
			else
				flow = RealFluidsFlowType.LAVA_DOWN;
		}
		
		else if(plugin.blockCanFlowUp(block)) {
			if(typeId == 8 || typeId == 9)
				flow = RealFluidsFlowType.WATER_UP;
			else
				flow = RealFluidsFlowType.WATER_UP;
		}
		
		else {
			if(typeId == 8 || typeId == 9)
			    flow = RealFluidsFlowType.WATER_HORIZONTAL;
			else if(typeId == 10 || typeId == 11)
				flow = RealFluidsFlowType.LAVA_HORIZONTAL;
			else
				flow = RealFluidsFlowType.AIR_HORIZONTAL;
		}
		
		return flow;
	}
	
	public boolean isCancelled() {
		return cancelled;
	}
	
	public void setCancelled(boolean c) {
		cancelled = c;
	}
	
	@Override
	public int hashCode() {
		return block.hashCode();
	}
	
	@Override
	public boolean equals(Object o) {
		RealFlowEvent rfe = null;
		if(o instanceof RealFlowEvent) {
			rfe = (RealFlowEvent)o;
			return block.equals(rfe.block);
		}
		return false;
	}
}