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
package com.l2jserver.gameserver.data.sql.impl;

import static java.sql.ResultSet.CONCUR_UPDATABLE;
import static java.sql.ResultSet.TYPE_FORWARD_ONLY;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.l2jserver.commons.database.ConnectionFactory;
import com.l2jserver.gameserver.model.L2Clan;
import com.l2jserver.gameserver.model.L2Crest;
import com.l2jserver.gameserver.model.L2Crest.CrestType;

/**
 * Loads and saves crests from database.
 * @author NosBit
 */
public final class CrestTable {
	
	private static final Logger LOG = LoggerFactory.getLogger(CrestTable.class);
	
	private final Map<Integer, L2Crest> _crests = new ConcurrentHashMap<>();
	
	private final AtomicInteger _nextId = new AtomicInteger(1);
	
	protected CrestTable() {
		load();
	}
	
	public synchronized void load() {
		_crests.clear();
		Set<Integer> crestsInUse = new HashSet<>();
		for (L2Clan clan : ClanTable.getInstance().getClans()) {
			if (clan.getCrestId() != 0) {
				crestsInUse.add(clan.getCrestId());
			}
			
			if (clan.getCrestLargeId() != 0) {
				crestsInUse.add(clan.getCrestLargeId());
			}
			
			if (clan.getAllyCrestId() != 0) {
				crestsInUse.add(clan.getAllyCrestId());
			}
		}
		
		try (var con = ConnectionFactory.getInstance().getConnection();
			var statement = con.createStatement(TYPE_FORWARD_ONLY, CONCUR_UPDATABLE);
			var rs = statement.executeQuery("SELECT `crest_id`, `data`, `type` FROM `crests` ORDER BY `crest_id` DESC")) {
			while (rs.next()) {
				int id = rs.getInt("crest_id");
				
				if (_nextId.get() <= id) {
					_nextId.set(id + 1);
				}
				
				// delete all unused crests except the last one we don't want to reuse
				// a crest id because client will display wrong crest if its reused
				if (!crestsInUse.contains(id) && (id != (_nextId.get() - 1))) {
					rs.deleteRow();
					continue;
				}
				
				byte[] data = rs.getBytes("data");
				CrestType crestType = CrestType.getById(rs.getInt("type"));
				if (crestType != null) {
					_crests.put(id, new L2Crest(id, data, crestType));
				} else {
					LOG.warn("Unknown crest type {} found in database!", rs.getInt("type"));
				}
			}
			
		} catch (Exception ex) {
			LOG.warn("There was an error while loading crests from database!", ex);
		}
		
		LOG.info("Loaded {} crests.", _crests.size());
		
		for (L2Clan clan : ClanTable.getInstance().getClans()) {
			if (clan.getCrestId() != 0) {
				if (getCrest(clan.getCrestId()) == null) {
					LOG.info("Removing non-existent crest Id {} for clan {}.", clan.getCrestId(), clan.getName());
					clan.setCrestId(0);
					clan.changeClanCrest(0);
				}
			}
			
			if (clan.getCrestLargeId() != 0) {
				if (getCrest(clan.getCrestLargeId()) == null) {
					LOG.info("Removing non-existent large crest Id {} for clan {}.", clan.getCrestId(), clan.getName());
					clan.setCrestLargeId(0);
					clan.changeLargeCrest(0);
				}
			}
			
			if (clan.getAllyCrestId() != 0) {
				if (getCrest(clan.getAllyCrestId()) == null) {
					LOG.info("Removing non-existent ally crest Id {} for clan {}.", clan.getCrestId(), clan.getName());
					clan.setAllyCrestId(0);
					clan.changeAllyCrest(0, true);
				}
			}
		}
	}
	
	/**
	 * @param crestId The crest id
	 * @return {@code L2Crest} if crest is found, {@code null} if crest was not found.
	 */
	public L2Crest getCrest(int crestId) {
		return _crests.get(crestId);
	}
	
	/**
	 * Creates a {@code L2Crest} object and inserts it in database and cache.
	 * @param data
	 * @param crestType
	 * @return {@code L2Crest} on success, {@code null} on failure.
	 */
	public L2Crest createCrest(byte[] data, CrestType crestType) {
		try (var con = ConnectionFactory.getInstance().getConnection();
			var statement = con.prepareStatement("INSERT INTO `crests`(`crest_id`, `data`, `type`) VALUES(?, ?, ?)")) {
			final L2Crest crest = new L2Crest(getNextId(), data, crestType);
			statement.setInt(1, crest.getId());
			statement.setBytes(2, crest.getData());
			statement.setInt(3, crest.getType().getId());
			statement.executeUpdate();
			_crests.put(crest.getId(), crest);
			return crest;
		} catch (Exception ex) {
			LOG.warn("There has been an error while saving crest in database!", ex);
		}
		return null;
	}
	
	/**
	 * Removes crest from database and cache.
	 * @param crestId the id of crest to be removed.
	 */
	public void removeCrest(int crestId) {
		_crests.remove(crestId);
		
		// avoid removing last crest id we don't want to lose index...
		// because client will display wrong crest if its reused
		if (crestId == (_nextId.get() - 1)) {
			return;
		}
		
		try (var con = ConnectionFactory.getInstance().getConnection();
			var statement = con.prepareStatement("DELETE FROM `crests` WHERE `crest_id` = ?")) {
			statement.setInt(1, crestId);
			statement.executeUpdate();
		} catch (Exception ex) {
			LOG.warn("There has been an  error while deleting crest from database!", ex);
		}
	}
	
	public int getNextId() {
		return _nextId.getAndIncrement();
	}
	
	public static CrestTable getInstance() {
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder {
		protected static final CrestTable INSTANCE = new CrestTable();
	}
}
