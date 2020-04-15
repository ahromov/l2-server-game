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
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.l2jserver.commons.database.ConnectionFactory;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;

/**
 * @author Kerberos
 * @authir JIV
 * @version 8/24/10
 */
public class RaidBossPointsManager {
	
	private static final Logger LOG = LoggerFactory.getLogger(RaidBossPointsManager.class);
	
	private final Map<Integer, Map<Integer, Integer>> _list = new ConcurrentHashMap<>();
	
	public RaidBossPointsManager() {
		init();
	}
	
	private final void init() {
		try (var con = ConnectionFactory.getInstance().getConnection();
			var s = con.createStatement();
			var rs = s.executeQuery("SELECT `charId`,`boss_id`,`points` FROM `character_raid_points`")) {
			while (rs.next()) {
				int charId = rs.getInt("charId");
				int bossId = rs.getInt("boss_id");
				int points = rs.getInt("points");
				Map<Integer, Integer> values = _list.get(charId);
				if (values == null) {
					values = new HashMap<>();
				}
				values.put(bossId, points);
				_list.put(charId, values);
			}
			LOG.info("Loaded {} characters raid points.", _list.size());
		} catch (Exception ex) {
			LOG.warn("Couldnt load character raid points!", ex);
		}
	}
	
	public final void updatePointsInDB(L2PcInstance player, int raidId, int points) {
		try (var con = ConnectionFactory.getInstance().getConnection();
			var ps = con.prepareStatement("REPLACE INTO character_raid_points (`charId`,`boss_id`,`points`) VALUES (?,?,?)")) {
			ps.setInt(1, player.getObjectId());
			ps.setInt(2, raidId);
			ps.setInt(3, points);
			ps.executeUpdate();
		} catch (Exception ex) {
			LOG.warn("Could not update character raid points for player {}!", player, ex);
		}
	}
	
	public final void addPoints(L2PcInstance player, int bossId, int points) {
		final Map<Integer, Integer> tmpPoint = _list.computeIfAbsent(player.getObjectId(), k -> new HashMap<>());
		updatePointsInDB(player, bossId, tmpPoint.merge(bossId, points, Integer::sum));
	}
	
	public final int getPointsByOwnerId(int ownerId) {
		Map<Integer, Integer> tmpPoint = _list.get(ownerId);
		int totalPoints = 0;
		
		if ((tmpPoint == null) || tmpPoint.isEmpty()) {
			return 0;
		}
		
		for (int points : tmpPoint.values()) {
			totalPoints += points;
		}
		return totalPoints;
	}
	
	public final Map<Integer, Integer> getList(L2PcInstance player) {
		return _list.get(player.getObjectId());
	}
	
	public final void cleanUp() {
		try (var con = ConnectionFactory.getInstance().getConnection();
			var s = con.createStatement()) {
			s.executeUpdate("DELETE from character_raid_points WHERE charId > 0");
			_list.clear();
		} catch (Exception ex) {
			LOG.warn("Could not clean character raid points!", ex);
		}
	}
	
	public final int calculateRanking(int playerObjId) {
		Map<Integer, Integer> rank = getRankList();
		if (rank.containsKey(playerObjId)) {
			return rank.get(playerObjId);
		}
		return 0;
	}
	
	public Map<Integer, Integer> getRankList() {
		final Map<Integer, Integer> tmpPoints = new HashMap<>();
		for (int ownerId : _list.keySet()) {
			int totalPoints = getPointsByOwnerId(ownerId);
			if (totalPoints != 0) {
				tmpPoints.put(ownerId, totalPoints);
			}
		}
		
		final List<Entry<Integer, Integer>> list = new ArrayList<>(tmpPoints.entrySet());
		list.sort(Comparator.comparing(Entry<Integer, Integer>::getValue).reversed());
		int ranking = 1;
		final Map<Integer, Integer> tmpRanking = new HashMap<>();
		for (Entry<Integer, Integer> entry : list) {
			tmpRanking.put(entry.getKey(), ranking++);
		}
		return tmpRanking;
	}
	
	public static final RaidBossPointsManager getInstance() {
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder {
		protected static final RaidBossPointsManager _instance = new RaidBossPointsManager();
	}
}