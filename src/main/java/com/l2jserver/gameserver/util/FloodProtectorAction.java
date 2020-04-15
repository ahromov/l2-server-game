/*
 * Copyright © 2004-2020 L2J Server
 * 
 * This file is part of L2J Server.
 * 
 * L2J Server is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.l2jserver.gameserver.util;

import static com.l2jserver.gameserver.config.Configuration.floodProtector;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.l2jserver.gameserver.GameTimeController;
import com.l2jserver.gameserver.instancemanager.PunishmentManager;
import com.l2jserver.gameserver.model.PcCondOverride;
import com.l2jserver.gameserver.model.punishment.PunishmentAffect;
import com.l2jserver.gameserver.model.punishment.PunishmentTask;
import com.l2jserver.gameserver.model.punishment.PunishmentType;
import com.l2jserver.gameserver.network.L2GameClient;

/**
 * Flood protector action implementation.
 * @author fordfrog
 * @author Zoey76
 */
public final class FloodProtectorAction {
	
	private static final Logger _log = Logger.getLogger(FloodProtectorAction.class.getName());
	
	private final L2GameClient client;
	
	private final String type;
	
	private final int interval;
	
	private final boolean logFlooding;
	
	private final int punishmentLimit;
	
	private final String punishmentType;
	
	private final int punishmentTime;
	
	/** Next game tick when new request is allowed. */
	private volatile int nextGameTick = GameTimeController.getInstance().getGameTicks();
	
	/** Request counter. */
	private final AtomicInteger count = new AtomicInteger(0);
	
	/** Flag determining whether exceeding request has been logged. */
	private boolean logged;
	
	/** Flag determining whether punishment application is in progress so that we do not apply punishment multiple times (flooding). */
	private volatile boolean punishmentInProgress;
	
	/**
	 * Creates new instance of FloodProtectorAction.
	 * @param client the game client for which flood protection is being created
	 * @param config flood protector configuration
	 */
	public FloodProtectorAction(L2GameClient client, String configName) {
		this.client = client;
		type = configName;
		interval = Integer.parseInt(floodProtector().getProperty(configName + "Interval"));
		logFlooding = Boolean.parseBoolean(floodProtector().getProperty(configName + "LogFlooding"));
		punishmentLimit = Integer.parseInt(floodProtector().getProperty(configName + "PunishmentLimit"));
		punishmentType = floodProtector().getProperty(configName + "PunishmentType");
		punishmentTime = Integer.parseInt(floodProtector().getProperty(configName + "PunishmentTime")) * 60_000;
	}
	
	/**
	 * Checks whether the request is flood protected or not.
	 * @param command command issued or short command description
	 * @return true if action is allowed, otherwise false
	 */
	public boolean tryPerformAction(final String command) {
		final int curTick = GameTimeController.getInstance().getGameTicks();
		
		if ((client.getActiveChar() != null) && client.getActiveChar().canOverrideCond(PcCondOverride.FLOOD_CONDITIONS)) {
			return true;
		}
		
		if ((curTick < nextGameTick) || punishmentInProgress) {
			if (logFlooding && !logged && _log.isLoggable(Level.WARNING)) {
				log(" called command ", command, " ~", String.valueOf((interval - (nextGameTick - curTick)) * GameTimeController.MILLIS_IN_TICK), " ms after previous command");
				logged = true;
			}
			
			count.incrementAndGet();
			
			if (!punishmentInProgress && (punishmentLimit > 0) && (count.get() >= punishmentLimit) && (punishmentType != null)) {
				punishmentInProgress = true;
				
				if ("kick".equals(punishmentType)) {
					kickPlayer();
				} else if ("ban".equals(punishmentType)) {
					banAccount();
				} else if ("jail".equals(punishmentType)) {
					jailChar();
				}
				
				punishmentInProgress = false;
			}
			return false;
		}
		
		if (count.get() > 0) {
			if (logFlooding && _log.isLoggable(Level.WARNING)) {
				log(" issued ", String.valueOf(count), " extra requests within ~", String.valueOf(interval * GameTimeController.MILLIS_IN_TICK), " ms");
			}
		}
		
		nextGameTick = curTick + interval;
		logged = false;
		count.set(0);
		return true;
	}
	
	/**
	 * Kick player from game (close network connection).
	 */
	private void kickPlayer() {
		if (client.getActiveChar() != null) {
			client.getActiveChar().logout(false);
		} else {
			client.closeNow();
		}
		
		if (_log.isLoggable(Level.WARNING)) {
			log("kicked for flooding");
		}
	}
	
	/**
	 * Bans char account and logs out the char.
	 */
	private void banAccount() {
		PunishmentManager.getInstance().startPunishment(new PunishmentTask(client.getAccountName(), PunishmentAffect.ACCOUNT, PunishmentType.BAN, System.currentTimeMillis() + punishmentTime, "", getClass().getSimpleName()));
		if (_log.isLoggable(Level.WARNING)) {
			log(" banned for flooding ", punishmentTime <= 0 ? "forever" : "for " + (punishmentTime / 60000) + " mins");
		}
	}
	
	/**
	 * Jails char.
	 */
	private void jailChar() {
		if (client.getActiveChar() != null) {
			int charId = client.getActiveChar().getObjectId();
			if (charId > 0) {
				PunishmentManager.getInstance().startPunishment(new PunishmentTask(charId, PunishmentAffect.CHARACTER, PunishmentType.JAIL, System.currentTimeMillis() + punishmentTime, "", getClass().getSimpleName()));
			}
			
			if (_log.isLoggable(Level.WARNING)) {
				log(" jailed for flooding ", punishmentTime <= 0 ? "forever" : "for " + (punishmentTime / 60000) + " mins");
			}
		}
	}
	
	private void log(String... lines) {
		final StringBuilder output = StringUtil.startAppend(100, type, ": ");
		String address = null;
		try {
			if (!client.isDetached()) {
				address = client.getConnection().getInetAddress().getHostAddress();
			}
		} catch (Exception e) {
		}
		
		switch (client.getState()) {
			case JOINING:
			case IN_GAME: {
				if (client.getActiveChar() != null) {
					StringUtil.append(output, client.getActiveChar().getName());
					StringUtil.append(output, "(", String.valueOf(client.getActiveChar().getObjectId()), ") ");
				}
				break;
			}
			case AUTHED: {
				if (client.getAccountName() != null) {
					StringUtil.append(output, client.getAccountName(), " ");
				}
				break;
			}
			case CONNECTED: {
				if (address != null) {
					StringUtil.append(output, address);
				}
				break;
			}
			default: {
				throw new IllegalStateException("Missing state on switch");
			}
		}
		
		StringUtil.append(output, lines);
		_log.warning(output.toString());
	}
}