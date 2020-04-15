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

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.l2jserver.commons.database.ConnectionFactory;
import com.l2jserver.gameserver.model.L2Territory;
import com.l2jserver.gameserver.model.Location;

/**
 * @author MrBalancer
 */
public class TerritoryTable {
	
	private static final Logger LOG = LoggerFactory.getLogger(TerritoryTable.class);
	
	private static final Map<Integer, L2Territory> _territory = new HashMap<>();
	
	protected TerritoryTable() {
		load();
	}
	
	/**
	 * Gets the random point.
	 * @param terr the territory Id?
	 * @return the random point
	 */
	public Location getRandomPoint(int terr) {
		return _territory.get(terr).getRandomPoint();
	}
	
	/**
	 * Gets the proc max.
	 * @param terr the territory Id?
	 * @return the proc max
	 */
	public int getProcMax(int terr) {
		return _territory.get(terr).getProcMax();
	}
	
	/**
	 * Load the data from database.
	 */
	public void load() {
		_territory.clear();
		try (var con = ConnectionFactory.getInstance().getConnection();
			var stmt = con.createStatement();
			var rset = stmt.executeQuery("SELECT * FROM locations WHERE loc_id>0")) {
			while (rset.next()) {
				int terrId = rset.getInt("loc_id");
				L2Territory terr = _territory.get(terrId);
				if (terr == null) {
					terr = new L2Territory(terrId);
					_territory.put(terrId, terr);
				}
				terr.add(rset.getInt("loc_x"), rset.getInt("loc_y"), rset.getInt("loc_zmin"), rset.getInt("loc_zmax"), rset.getInt("proc"));
			}
			LOG.info("Loaded {} territories from database.", _territory.size());
		} catch (Exception ex) {
			LOG.error("Failed to load territories from database!", ex);
		}
	}
	
	public static TerritoryTable getInstance() {
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder {
		protected static final TerritoryTable INSTANCE = new TerritoryTable();
	}
}
