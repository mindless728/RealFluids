package mindless728.RealFluids;

import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;

import java.util.HashMap;

public class RealFluidsPlayerListener extends PlayerListener {
	RealFluids plugin;
	ItemStack stack;

	public RealFluidsPlayerListener(RealFluids p) {
		plugin = p;
		stack = null;
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
