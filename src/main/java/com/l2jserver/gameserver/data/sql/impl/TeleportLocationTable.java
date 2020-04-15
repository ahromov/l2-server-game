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

import static com.l2jserver.gameserver.config.Configuration.general;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.l2jserver.commons.database.ConnectionFactory;
import com.l2jserver.gameserver.model.L2TeleportLocation;

public class TeleportLocationTable {
	
	private static final Logger LOG = LoggerFactory.getLogger(TeleportLocationTable.class);
	
	private final Map<Integer, L2TeleportLocation> _teleports = new HashMap<>();
	
	protected TeleportLocationTable() {
		reloadAll();
	}
	
	public void reloadAll() {
		_teleports.clear();
		try (var con = ConnectionFactory.getInstance().getConnection();
			var s = con.createStatement();
			var rs = s.executeQuery("SELECT id, loc_x, loc_y, loc_z, price, fornoble, itemId FROM teleport")) {
			L2TeleportLocation teleport;
			while (rs.next()) {
				teleport = new L2TeleportLocation();
				
				teleport.setTeleId(rs.getInt("id"));
				teleport.setLocX(rs.getInt("loc_x"));
				teleport.setLocY(rs.getInt("loc_y"));
				teleport.setLocZ(rs.getInt("loc_z"));
				teleport.setPrice(rs.getInt("price"));
				teleport.setIsForNoble(rs.getInt("fornoble") == 1);
				teleport.setItemId(rs.getInt("itemId"));
				
				_teleports.put(teleport.getTeleId(), teleport);
			}
			LOG.info("Loaded {} teleport location templates.", _teleports.size());
		} catch (Exception ex) {
			LOG.error("There has been an error loading teleport table.", ex);
		}
		
		if (general().customTeleportTable()) {
			int _cTeleCount = _teleports.size();
			try (var con = ConnectionFactory.getInstance().getConnection();
				var s = con.createStatement();
				var rs = s.executeQuery("SELECT id, loc_x, loc_y, loc_z, price, fornoble, itemId FROM custom_teleport")) {
				L2TeleportLocation teleport;
				while (rs.next()) {
					teleport = new L2TeleportLocation();
					teleport.setTeleId(rs.getInt("id"));
					teleport.setLocX(rs.getInt("loc_x"));
					teleport.setLocY(rs.getInt("loc_y"));
					teleport.setLocZ(rs.getInt("loc_z"));
					teleport.setPrice(rs.getInt("price"));
					teleport.setIsForNoble(rs.getInt("fornoble") == 1);
					teleport.setItemId(rs.getInt("itemId"));
					
					_teleports.put(teleport.getTeleId(), teleport);
				}
				_cTeleCount = _teleports.size() - _cTeleCount;
				if (_cTeleCount > 0) {
					LOG.info("Loaded {} custom teleport location templates.", _cTeleCount);
				}
			} catch (Exception ex) {
				LOG.warn("There has been an error while creating custom teleport table!", ex);
			}
		}
	}
	
	public L2TeleportLocation getTemplate(int id) {
		return _teleports.get(id);
	}
	
	public static TeleportLocationTable getInstance() {
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder {
		protected static final TeleportLocationTable INSTANCE = new TeleportLocationTable();
	}
}
