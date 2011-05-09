package mindless728.RealFluids;

import org.bukkit.Material;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;

public class RealFluidsPlayerListener extends PlayerListener {
	RealFluids plugin;

	public RealFluidsPlayerListener(RealFluids p) {
		plugin = p;
	}

	@Override
	public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event) {
	}

	@Override
	public void onPlayerBucketFill(PlayerBucketFillEvent event) {
	}

	//@TODO grab when a player logs in
	//@TODO grab when a player logs out
}
