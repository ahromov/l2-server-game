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
package com.l2jserver.gameserver.instancemanager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.l2jserver.gameserver.GameTimeController;
import com.l2jserver.gameserver.model.L2Spawn;
import com.l2jserver.gameserver.model.actor.L2Npc;
import com.l2jserver.gameserver.model.actor.instance.L2RaidBossInstance;

/**
 * @author godson
 */
public final class DayNightSpawnManager {
	
	private static final Logger LOG = LoggerFactory.getLogger(DayNightSpawnManager.class);
	
	private final List<L2Spawn> _dayCreatures = new ArrayList<>();
	
	private final List<L2Spawn> _nightCreatures = new ArrayList<>();
	
	private final Map<L2Spawn, L2RaidBossInstance> _bosses = new ConcurrentHashMap<>();
	
	protected DayNightSpawnManager() {
		// Prevent external initialization.
	}
	
	public void addDayCreature(L2Spawn spawnDat) {
		_dayCreatures.add(spawnDat);
	}
	
	public void addNightCreature(L2Spawn spawnDat) {
		_nightCreatures.add(spawnDat);
	}
	
	/**
	 * Spawn Day Creatures, and Unspawn Night Creatures
	 */
	public void spawnDayCreatures() {
		spawnCreatures(_nightCreatures, _dayCreatures, "night", "day");
	}
	
	/**
	 * Spawn Night Creatures, and Unspawn Day Creatures
	 */
	public void spawnNightCreatures() {
		spawnCreatures(_dayCreatures, _nightCreatures, "day", "night");
	}
	
	/**
	 * Manage Spawn/Respawn
	 * @param unSpawnCreatures List with spawns must be unspawned
	 * @param spawnCreatures List with spawns must be spawned
	 * @param unspawnLogInfo String for log info for unspawned L2NpcInstance
	 * @param spawnLogInfo String for log info for spawned L2NpcInstance
	 */
	private void spawnCreatures(List<L2Spawn> unSpawnCreatures, List<L2Spawn> spawnCreatures, String unspawnLogInfo, String spawnLogInfo) {
		try {
			if (!unSpawnCreatures.isEmpty()) {
				int i = 0;
				for (L2Spawn spawn : unSpawnCreatures) {
					if (spawn == null) {
						continue;
					}
					
					spawn.stopRespawn();
					L2Npc last = spawn.getLastSpawn();
					if (last != null) {
						last.deleteMe();
						i++;
					}
				}
				LOG.info("Removed {} {} creatures.", i, unspawnLogInfo);
			}
			
			int i = 0;
			for (L2Spawn spawnDat : spawnCreatures) {
				if (spawnDat == null) {
					continue;
				}
				spawnDat.startRespawn();
				spawnDat.doSpawn();
				i++;
			}
			
			LOG.info("Spawned {} {} creatures.", i, spawnLogInfo);
		} catch (Exception ex) {
			LOG.warn("There has been an error while spawning creatures!", ex);
		}
	}
	
	private void changeMode(int mode) {
		if (_nightCreatures.isEmpty() && _dayCreatures.isEmpty() && _bosses.isEmpty()) {
			return;
		}
		
		switch (mode) {
			case 0:
				spawnDayCreatures();
				specialNightBoss(0);
				break;
			case 1:
				spawnNightCreatures();
				specialNightBoss(1);
				break;
		}
	}
	
	public DayNightSpawnManager trim() {
		((ArrayList<?>) _nightCreatures).trimToSize();
		((ArrayList<?>) _dayCreatures).trimToSize();
		return this;
	}
	
	public void notifyChangeMode() {
		if (GameTimeController.getInstance().isNight()) {
			changeMode(1);
		} else {
			changeMode(0);
		}
	}
	
	public void cleanUp() {
		_nightCreatures.clear();
		_dayCreatures.clear();
		_bosses.clear();
	}
	
	private void specialNightBoss(int mode) {
		try {
			for (L2Spawn spawn : _bosses.keySet()) {
				var boss = _bosses.get(spawn);
				if ((boss == null) && (mode == 1)) {
					boss = (L2RaidBossInstance) spawn.doSpawn();
					RaidBossSpawnManager.getInstance().notifySpawnNightBoss(boss);
					_bosses.put(spawn, boss);
					continue;
				}
				
				if ((boss == null) && (mode == 0)) {
					continue;
				}
				
				if ((boss != null) && (boss.getId() == 25328) && boss.getRaidStatus().equals(RaidBossSpawnManager.StatusEnum.ALIVE)) {
					handleHellmans(boss, mode);
				}
				return;
			}
		} catch (Exception ex) {
			LOG.warn("There has been an error while spawning special night boss!", ex);
		}
	}
	
	private void handleHellmans(L2RaidBossInstance boss, int mode) {
		switch (mode) {
			case 0:
				boss.deleteMe();
				LOG.info("Deleting Hellman raidboss.");
				break;
			case 1:
				if (!boss.isVisible()) {
					boss.spawnMe();
				}
				LOG.info("Spawning Hellman raidboss.");
				break;
		}
	}
	
	public L2RaidBossInstance handleBoss(L2Spawn bossSpawn) {
		if (_bosses.containsKey(bossSpawn)) {
			return _bosses.get(bossSpawn);
		}
		
		if (GameTimeController.getInstance().isNight()) {
			L2RaidBossInstance raidboss = (L2RaidBossInstance) bossSpawn.doSpawn();
			_bosses.put(bossSpawn, raidboss);
			
			return raidboss;
		}
		return null;
	}
	
	public static DayNightSpawnManager getInstance() {
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder {
		protected static final DayNightSpawnManager INSTANCE = new DayNightSpawnManager();
	}
}
