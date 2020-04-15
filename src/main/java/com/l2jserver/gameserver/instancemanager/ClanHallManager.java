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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.l2jserver.commons.database.ConnectionFactory;
import com.l2jserver.gameserver.data.sql.impl.ClanTable;
import com.l2jserver.gameserver.model.L2Clan;
import com.l2jserver.gameserver.model.L2Object;
import com.l2jserver.gameserver.model.StatsSet;
import com.l2jserver.gameserver.model.entity.Auction;
import com.l2jserver.gameserver.model.entity.ClanHall;
import com.l2jserver.gameserver.model.entity.clanhall.AuctionableHall;
import com.l2jserver.gameserver.model.entity.clanhall.SiegableHall;
import com.l2jserver.gameserver.model.zone.type.L2ClanHallZone;

/**
 * Clan Hall Manger.
 * @author Steuf
 */
public final class ClanHallManager {
	
	private static final Logger LOG = LoggerFactory.getLogger(ClanHallManager.class);
	
	private final Map<Integer, AuctionableHall> _clanHall = new ConcurrentHashMap<>();
	
	private final Map<Integer, AuctionableHall> _freeClanHall = new ConcurrentHashMap<>();
	
	private final Map<Integer, AuctionableHall> _allAuctionableClanHalls = new HashMap<>();
	
	private static Map<Integer, ClanHall> _allClanHalls = new HashMap<>();
	
	private boolean _loaded = false;
	
	public boolean loaded() {
		return _loaded;
	}
	
	protected ClanHallManager() {
		load();
	}
	
	private final void load() {
		try (var con = ConnectionFactory.getInstance().getConnection();
			var s = con.createStatement();
			var rs = s.executeQuery("SELECT * FROM clanhall ORDER BY id")) {
			int id, ownerId, lease;
			while (rs.next()) {
				StatsSet set = new StatsSet();
				
				id = rs.getInt("id");
				ownerId = rs.getInt("ownerId");
				lease = rs.getInt("lease");
				
				set.set("id", id);
				set.set("name", rs.getString("name"));
				set.set("ownerId", ownerId);
				set.set("lease", lease);
				set.set("desc", rs.getString("desc"));
				set.set("location", rs.getString("location"));
				set.set("paidUntil", rs.getLong("paidUntil"));
				set.set("grade", rs.getInt("Grade"));
				set.set("paid", rs.getBoolean("paid"));
				AuctionableHall ch = new AuctionableHall(set);
				_allAuctionableClanHalls.put(id, ch);
				addClanHall(ch);
				
				if (ch.getOwnerId() > 0) {
					_clanHall.put(id, ch);
					continue;
				}
				_freeClanHall.put(id, ch);
				
				Auction auc = AuctionManager.getInstance().getAuction(id);
				if ((auc == null) && (lease > 0)) {
					AuctionManager.getInstance().initNPC(id);
				}
			}
			LOG.info("Loaded {} clan halls.", getClanHalls().size());
			LOG.info("Loaded {} free clan halls.", getFreeClanHalls().size());
			_loaded = true;
		} catch (Exception ex) {
			LOG.warn("There has been an error loading clan halls from database!", ex);
		}
	}
	
	public static final Map<Integer, ClanHall> getAllClanHalls() {
		return _allClanHalls;
	}
	
	public final Map<Integer, AuctionableHall> getFreeClanHalls() {
		return _freeClanHall;
	}
	
	/**
	 * @return all ClanHalls that have owner
	 */
	public final Map<Integer, AuctionableHall> getClanHalls() {
		return _clanHall;
	}
	
	public final Map<Integer, AuctionableHall> getAllAuctionableClanHalls() {
		return _allAuctionableClanHalls;
	}
	
	public static final void addClanHall(ClanHall hall) {
		_allClanHalls.put(hall.getId(), hall);
	}
	
	public final boolean isFree(int chId) {
		if (_freeClanHall.containsKey(chId)) {
			return true;
		}
		return false;
	}
	
	public final synchronized void setFree(int chId) {
		_freeClanHall.put(chId, _clanHall.get(chId));
		ClanTable.getInstance().getClan(_freeClanHall.get(chId).getOwnerId()).setHideoutId(0);
		_freeClanHall.get(chId).free();
		_clanHall.remove(chId);
	}
	
	/**
	 * Set ClanHallOwner
	 * @param chId
	 * @param clan
	 */
	public final synchronized void setOwner(int chId, L2Clan clan) {
		if (!_clanHall.containsKey(chId)) {
			_clanHall.put(chId, _freeClanHall.get(chId));
			_freeClanHall.remove(chId);
		} else {
			_clanHall.get(chId).free();
		}
		ClanTable.getInstance().getClan(clan.getId()).setHideoutId(chId);
		_clanHall.get(chId).setOwner(clan);
	}
	
	public final ClanHall getClanHallById(int clanHallId) {
		return _allClanHalls.get(clanHallId);
	}
	
	public final AuctionableHall getAuctionableHallById(int clanHallId) {
		return _allAuctionableClanHalls.get(clanHallId);
	}
	
	public final ClanHall getClanHall(int x, int y, int z) {
		for (ClanHall temp : _allClanHalls.values()) {
			if (temp.checkIfInZone(x, y, z)) {
				return temp;
			}
		}
		return null;
	}
	
	public final ClanHall getClanHall(L2Object activeObject) {
		return getClanHall(activeObject.getX(), activeObject.getY(), activeObject.getZ());
	}
	
	public final AuctionableHall getNearbyClanHall(int x, int y, int maxDist) {
		L2ClanHallZone zone = null;
		
		for (Map.Entry<Integer, AuctionableHall> ch : _clanHall.entrySet()) {
			zone = ch.getValue().getZone();
			if ((zone != null) && (zone.getDistanceToZone(x, y) < maxDist)) {
				return ch.getValue();
			}
		}
		for (Map.Entry<Integer, AuctionableHall> ch : _freeClanHall.entrySet()) {
			zone = ch.getValue().getZone();
			if ((zone != null) && (zone.getDistanceToZone(x, y) < maxDist)) {
				return ch.getValue();
			}
		}
		return null;
	}
	
	public final ClanHall getNearbyAbstractHall(int x, int y, int maxDist) {
		L2ClanHallZone zone = null;
		for (Map.Entry<Integer, ClanHall> ch : _allClanHalls.entrySet()) {
			zone = ch.getValue().getZone();
			if ((zone != null) && (zone.getDistanceToZone(x, y) < maxDist)) {
				return ch.getValue();
			}
		}
		return null;
	}
	
	public final AuctionableHall getClanHallByOwner(L2Clan clan) {
		for (Map.Entry<Integer, AuctionableHall> ch : _clanHall.entrySet()) {
			if (clan.getId() == ch.getValue().getOwnerId()) {
				return ch.getValue();
			}
		}
		return null;
	}
	
	public final ClanHall getAbstractHallByOwner(L2Clan clan) {
		// Separate loops to avoid iterating over free clan halls
		for (Map.Entry<Integer, AuctionableHall> ch : _clanHall.entrySet()) {
			if (clan.getId() == ch.getValue().getOwnerId()) {
				return ch.getValue();
			}
		}
		for (Map.Entry<Integer, SiegableHall> ch : ClanHallSiegeManager.getInstance().getConquerableHalls().entrySet()) {
			if (clan.getId() == ch.getValue().getOwnerId()) {
				return ch.getValue();
			}
		}
		return null;
	}
	
	public static ClanHallManager getInstance() {
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder {
		protected static final ClanHallManager INSTANCE = new ClanHallManager();
	}
}