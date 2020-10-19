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

import static com.l2jserver.gameserver.config.Configuration.npc;

import java.util.Calendar;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.l2jserver.commons.database.ConnectionFactory;
import com.l2jserver.commons.util.Rnd;
import com.l2jserver.gameserver.ThreadPoolManager;
import com.l2jserver.gameserver.config.Configuration;
import com.l2jserver.gameserver.datatables.SpawnTable;
import com.l2jserver.gameserver.model.L2Spawn;
import com.l2jserver.gameserver.model.StatsSet;
import com.l2jserver.gameserver.model.actor.instance.L2RaidBossInstance;

/**
 * Raid Boss spawn manager.
 * @author godson
 */
public class RaidBossSpawnManager {
	
	private static final Logger LOG = LoggerFactory.getLogger(RaidBossSpawnManager.class);
	
	private static final int EILHALDER_VON_HELLMANN = 25328;
	
	protected static final Map<Integer, L2RaidBossInstance> _bosses = new ConcurrentHashMap<>();
	
	protected static final Map<Integer, L2Spawn> _spawns = new ConcurrentHashMap<>();
	
	protected static final Map<Integer, StatsSet> _storedInfo = new ConcurrentHashMap<>();
	
	protected static final Map<Integer, ScheduledFuture<?>> _schedules = new ConcurrentHashMap<>();
	
	public enum StatusEnum {
		ALIVE,
		DEAD,
		UNDEFINED
	}
	
	protected RaidBossSpawnManager() {
		load();
	}
	
	public void load() {
		LOG.info("Spawning raid bosses...");
		
		_bosses.clear();
		_spawns.clear();
		_storedInfo.clear();
		_schedules.clear();
		
		try (var con = ConnectionFactory.getInstance().getConnection();
			var s = con.createStatement();
			var rs = s.executeQuery("SELECT * FROM raidboss_spawnlist ORDER BY boss_id")) {
			while (rs.next()) {
				final L2Spawn spawnDat = new L2Spawn(rs.getInt("boss_id"));
				spawnDat.setX(rs.getInt("loc_x"));
				spawnDat.setY(rs.getInt("loc_y"));
				spawnDat.setZ(rs.getInt("loc_z"));
				spawnDat.setAmount(rs.getInt("amount"));
				spawnDat.setHeading(rs.getInt("heading"));
				spawnDat.setRespawnDelay(rs.getInt("respawn_delay"), rs.getInt("respawn_random"));
				
				addNewSpawn(spawnDat, rs.getLong("respawn_time"), rs.getDouble("currentHP"), rs.getDouble("currentMP"), false);
			}
			
			LOG.info("Loaded {} bosses.", _bosses.size());
			LOG.info("Scheduled {} boss instances.", _schedules.size());
		} catch (Exception ex) {
			LOG.warn("There has been an error while initializing raid boss spawn manager!", ex);
		}
	}
	
	private static class SpawnSchedule implements Runnable {
		private static final Logger LOG = LoggerFactory.getLogger(SpawnSchedule.class);
		
		private final int bossId;
		
		public SpawnSchedule(int npcId) {
			bossId = npcId;
		}
		
		@Override
		public void run() {
			L2RaidBossInstance raidBoss;
			if (bossId == EILHALDER_VON_HELLMANN) {
				raidBoss = DayNightSpawnManager.getInstance().handleBoss(_spawns.get(bossId));
			} else {
				raidBoss = (L2RaidBossInstance) _spawns.get(bossId).doSpawn();
			}
			
			if (raidBoss != null) {
				raidBoss.setRaidStatus(StatusEnum.ALIVE);
				
				final StatsSet info = new StatsSet();
				info.set("currentHP", raidBoss.getCurrentHp());
				info.set("currentMP", raidBoss.getCurrentMp());
				info.set("respawnTime", 0L);
				
				_storedInfo.put(bossId, info);
				
				LOG.info("Spawning Raid Boss {}.", raidBoss.getName());
				
				_bosses.put(bossId, raidBoss);
			}
			
			_schedules.remove(bossId);
		}
	}
	
	/**
	 * Update status.
	 * @param boss the boss
	 * @param isBossDead the is boss dead
	 */
	public void updateStatus(L2RaidBossInstance boss, boolean isBossDead) {
		final StatsSet info = _storedInfo.get(boss.getId());
		if (info == null) {
			return;
		}
		
		if (isBossDead) {
			boss.setRaidStatus(StatusEnum.DEAD);
			
			final int respawnMinDelay = (int) (boss.getSpawn().getRespawnMinDelay() * npc().getRaidMinRespawnMultiplier());
			final int respawnMaxDelay = (int) (boss.getSpawn().getRespawnMaxDelay() * npc().getRaidMaxRespawnMultiplier());
			final int respawnDelay = Rnd.get(respawnMinDelay, respawnMaxDelay);
			final long respawnTime = Calendar.getInstance().getTimeInMillis() + respawnDelay;
			
			info.set("currentHP", boss.getMaxHp());
			info.set("currentMP", boss.getMaxMp());
			info.set("respawnTime", respawnTime);
			
			if (!_schedules.containsKey(boss.getId()) && ((respawnMinDelay > 0) || (respawnMaxDelay > 0))) {
				final Calendar time = Calendar.getInstance();
				time.setTimeInMillis(respawnTime);
				LOG.info("Updated {} respawn time to {}.", boss.getName(), time.getTime());
				
				_schedules.put(boss.getId(), ThreadPoolManager.getInstance().scheduleGeneral(new SpawnSchedule(boss.getId()), respawnDelay));
				updateDb();
			}
		} else {
			boss.setRaidStatus(StatusEnum.ALIVE);
			
			info.set("currentHP", boss.getCurrentHp());
			info.set("currentMP", boss.getCurrentMp());
			info.set("respawnTime", 0L);
		}
		_storedInfo.put(boss.getId(), info);
	}
	
	/**
	 * Adds the new spawn.
	 * @param spawnDat the spawn dat
	 * @param respawnTime the respawn time
	 * @param currentHP the current hp
	 * @param currentMP the current mp
	 * @param storeInDb the store in db
	 */
	public void addNewSpawn(L2Spawn spawnDat, long respawnTime, double currentHP, double currentMP, boolean storeInDb) {
		if (spawnDat == null) {
			return;
		}
		if (_spawns.containsKey(spawnDat.getId())) {
			return;
		}
		
		final int bossId = spawnDat.getId();
		final long time = Calendar.getInstance().getTimeInMillis();
		
		SpawnTable.getInstance().addNewSpawn(spawnDat, false);
		
		if ((respawnTime == 0L) || (time > respawnTime)) {
			L2RaidBossInstance raidBoss;
			if (bossId == EILHALDER_VON_HELLMANN) {
				raidBoss = DayNightSpawnManager.getInstance().handleBoss(spawnDat);
			} else {
				raidBoss = (L2RaidBossInstance) spawnDat.doSpawn();
			}
			
			if (raidBoss != null) {
				raidBoss.setCurrentHp(currentHP);
				raidBoss.setCurrentMp(currentMP);
				raidBoss.setRaidStatus(StatusEnum.ALIVE);
				
				_bosses.put(bossId, raidBoss);
				
				final StatsSet info = new StatsSet();
				info.set("currentHP", currentHP);
				info.set("currentMP", currentMP);
				info.set("respawnTime", 0L);
				
				_storedInfo.put(bossId, info);
			}
		} else {
			final long spawnTime = respawnTime - Calendar.getInstance().getTimeInMillis();
			_schedules.put(bossId, ThreadPoolManager.getInstance().scheduleGeneral(new SpawnSchedule(bossId), spawnTime));
		}
		
		_spawns.put(bossId, spawnDat);
		
		if (storeInDb) {
			try (var con = ConnectionFactory.getInstance().getConnection();
				var ps = con.prepareStatement("INSERT INTO raidboss_spawnlist (boss_id,amount,loc_x,loc_y,loc_z,heading,respawn_time,currentHp,currentMp) VALUES(?,?,?,?,?,?,?,?,?)")) {
				ps.setInt(1, spawnDat.getId());
				ps.setInt(2, spawnDat.getAmount());
				ps.setInt(3, spawnDat.getX());
				ps.setInt(4, spawnDat.getY());
				ps.setInt(5, spawnDat.getZ());
				ps.setInt(6, spawnDat.getHeading());
				ps.setLong(7, respawnTime);
				ps.setDouble(8, currentHP);
				ps.setDouble(9, currentMP);
				ps.execute();
			} catch (Exception ex) {
				LOG.warn("Could not store raid boss Id {} in the DB!", bossId, ex);
			}
		}
	}
	
	/**
	 * Delete spawn.
	 * @param spawnDat the spawn dat
	 * @param updateDb the update db
	 */
	public void deleteSpawn(L2Spawn spawnDat, boolean updateDb) {
		if (spawnDat == null) {
			return;
		}
		
		final int bossId = spawnDat.getId();
		if (!_spawns.containsKey(bossId)) {
			return;
		}
		
		SpawnTable.getInstance().deleteSpawn(spawnDat, false);
		_spawns.remove(bossId);
		
		_bosses.remove(bossId);
		
		final ScheduledFuture<?> f = _schedules.remove(bossId);
		if (f != null) {
			f.cancel(true);
		}
		
		_storedInfo.remove(bossId);
		
		if (updateDb) {
			try (var con = ConnectionFactory.getInstance().getConnection();
				var ps = con.prepareStatement("DELETE FROM raidboss_spawnlist WHERE boss_id=?")) {
				ps.setInt(1, bossId);
				ps.execute();
			} catch (Exception ex) {
				LOG.warn("Could not remove raid boss Id {} from DB!", bossId, ex);
			}
		}
	}
	
	private void updateDb() {
		try (var con = ConnectionFactory.getInstance().getConnection();
			var ps = con.prepareStatement("UPDATE raidboss_spawnlist SET respawn_time = ?, currentHP = ?, currentMP = ? WHERE boss_id = ?")) {
			for (Integer bossId : _storedInfo.keySet()) {
				if (bossId == null) {
					continue;
				}
				
				L2RaidBossInstance boss = _bosses.get(bossId);
				
				if (boss == null) {
					continue;
				}
				
				if (boss.getRaidStatus().equals(StatusEnum.ALIVE)) {
					updateStatus(boss, false);
				}
				
				StatsSet info = _storedInfo.get(bossId);
				
				if (info == null) {
					continue;
				}
				
				try {
					// TODO(Zoey76): Change this to use batch.
					ps.setLong(1, info.getLong("respawnTime"));
					ps.setDouble(2, info.getDouble("currentHP"));
					ps.setDouble(3, info.getDouble("currentMP"));
					ps.setInt(4, bossId);
					ps.executeUpdate();
					ps.clearParameters();
				} catch (Exception ex) {
					LOG.warn("Could not update raid boss spawn list table!", ex);
				}
			}
		} catch (Exception ex) {
			LOG.warn("SQL error while updating Raid Boss spawn to database!", ex);
		}
	}
	
	/**
	 * Gets the all raid boss status.
	 * @return the all raid boss status
	 */
	public String[] getAllRaidBossStatus() {
		final String[] msg = new String[Math.max(_bosses.size(), 1)];
		if (_bosses.isEmpty()) {
			msg[0] = "None";
			return msg;
		}
		
		int index = 0;
		for (var boss : _bosses.values()) {
			msg[index++] = boss.getName() + ": " + boss.getRaidStatus().name();
		}
		return msg;
	}
	
	/**
	 * Gets the raid boss status.
	 * @param bossId the boss id
	 * @return the raid boss status
	 */
	public String getRaidBossStatus(int bossId) {
		String msg = "RaidBoss Status..." + Configuration.EOL;
		
		if (_bosses == null) {
			msg += "None";
			return msg;
		}
		
		if (_bosses.containsKey(bossId)) {
			final L2RaidBossInstance boss = _bosses.get(bossId);
			
			msg += boss.getName() + ": " + boss.getRaidStatus().name();
		}
		
		return msg;
	}
	
	/**
	 * Gets the raid boss status id.
	 * @param bossId the boss id
	 * @return the raid boss status id
	 */
	public StatusEnum getRaidBossStatusId(int bossId) {
		if (_bosses.containsKey(bossId)) {
			return _bosses.get(bossId).getRaidStatus();
		} else if (_schedules.containsKey(bossId)) {
			return StatusEnum.DEAD;
		} else {
			return StatusEnum.UNDEFINED;
		}
	}
	
	/**
	 * Notify spawn night boss.
	 * @param raidBoss the raid boss
	 */
	public void notifySpawnNightBoss(L2RaidBossInstance raidBoss) {
		final StatsSet info = new StatsSet();
		info.set("currentHP", raidBoss.getCurrentHp());
		info.set("currentMP", raidBoss.getCurrentMp());
		info.set("respawnTime", 0L);
		
		raidBoss.setRaidStatus(StatusEnum.ALIVE);
		
		_storedInfo.put(raidBoss.getId(), info);
		
		LOG.info("Spawning Night Raid Boss {}.", raidBoss.getName());
		
		_bosses.put(raidBoss.getId(), raidBoss);
	}
	
	/**
	 * Checks if the boss is defined.
	 * @param bossId the boss id
	 * @return {@code true} if is defined
	 */
	public boolean isDefined(int bossId) {
		return _spawns.containsKey(bossId);
	}
	
	/**
	 * Gets the bosses.
	 * @return the bosses
	 */
	public Map<Integer, L2RaidBossInstance> getBosses() {
		return _bosses;
	}
	
	/**
	 * Gets the spawns.
	 * @return the spawns
	 */
	public Map<Integer, L2Spawn> getSpawns() {
		return _spawns;
	}
	
	/**
	 * Gets the stored info.
	 * @return the stored info
	 */
	public Map<Integer, StatsSet> getStoredInfo() {
		return _storedInfo;
	}
	
	/**
	 * Saves and clears the raid bosses status, including all schedules.
	 */
	public void cleanUp() {
		updateDb();
		
		_bosses.clear();
		
		if (_schedules != null) {
			for (Integer bossId : _schedules.keySet()) {
				ScheduledFuture<?> f = _schedules.get(bossId);
				f.cancel(true);
			}
			_schedules.clear();
		}
		
		_storedInfo.clear();
		_spawns.clear();
	}
	
	public static RaidBossSpawnManager getInstance() {
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder {
		protected static final RaidBossSpawnManager INSTANCE = new RaidBossSpawnManager();
	}
}
