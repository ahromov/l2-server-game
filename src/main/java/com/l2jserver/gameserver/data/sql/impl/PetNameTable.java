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

import static com.l2jserver.gameserver.config.Configuration.character;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.l2jserver.commons.database.ConnectionFactory;

public class PetNameTable {
	
	private static final Logger LOG = LoggerFactory.getLogger(PetNameTable.class);
	
	public boolean doesPetNameExist(String name) {
		try (var con = ConnectionFactory.getInstance().getConnection();
			var ps = con.prepareStatement("SELECT name FROM pets WHERE name=?")) {
			ps.setString(1, name);
			try (var rs = ps.executeQuery()) {
				return rs.next();
			}
		} catch (Exception ex) {
			LOG.warn("Could not check existing pet name {}!", ex);
		}
		return false;
	}
	
	public boolean isValidPetName(String name) {
		return character().getPetNameTemplate().matcher(name).matches();
	}
	
	public static PetNameTable getInstance() {
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder {
		protected static final PetNameTable INSTANCE = new PetNameTable();
	}
}
