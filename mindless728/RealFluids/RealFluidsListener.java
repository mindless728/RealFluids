package mindless728.RealFluids;

import org.bukkit.event.CustomEventListener;
import org.bukkit.event.Event;

public class RealFluidsListener extends CustomEventListener {
	RealFluids plugin;
	
	public RealFluidsListener(RealFluids p) throws NullPointerException {
		if(p == null)
			throw new NullPointerException();
	    plugin = p;
	}
	
    public final void onCustomEvent(Event event) {
	    if(event.getEventName().equals("RealFlowEvent") && (event instanceof RealFlowEvent))
			onRealFlow((RealFlowEvent)event);
	}
	
	public void onRealFlow(RealFlowEvent event) {
	}
}