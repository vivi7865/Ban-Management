package me.confuser.banmanager.scheduler;

import me.confuser.banmanager.BanManager;
import me.confuser.banmanager.Database;
import me.confuser.banmanager.data.MuteData;

import java.sql.ResultSet;
import java.sql.SQLException;

public class MuteAsync implements Runnable {

	private Database localConn;
	private BanManager plugin;
	private static long lastRun;

	public MuteAsync(BanManager banManager, long lastChecked) {
		plugin = banManager;
		localConn = plugin.localConn;
		lastRun = lastChecked;
	}

	public void run() {
		// Check for new mutes
        long thisRun = System.currentTimeMillis() / 1000;
		ResultSet result = localConn.query("SELECT * FROM " + localConn.getTable("mutes") + " WHERE mute_time >= " + lastRun + "");

		try {
			while (result.next()) {
				// Add them to the muted list
				final String name = result.getString("muted");

				if (!plugin.isPlayerMuted(name)) {
					// Firt we see if they are online, if they are then and only
					// then do we mute them, otherwise we're just adding to the
					// HashMap for no reason
					if (plugin.getServer().getPlayer(name) != null) {
						// Add the mute!
						plugin.getPlayerMutes().put(name, new MuteData(name, result.getString("muted_by"), result.getString("mute_reason"), result.getLong("mute_time"), result.getLong("mute_expires_on")));
					}
				}
			}

			result.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		// Check for old mutes and remove them!
		ResultSet result1 = localConn.query("SELECT muted FROM " + localConn.getTable("muteRecords") + " WHERE unmuted_time >= " + lastRun + "");

		try {
			while (result1.next()) {
				// Remove them from the muted list
				final String name = result1.getString("muted");

				if (plugin.getPlayerMutes().get(name) != null) {
					plugin.getPlayerMutes().remove(name);
				}
			}

			result1.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

        lastRun = thisRun;
	}
	
	public static long getLastRun() {
		return lastRun;
	}
}
