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
package com.l2jserver.gameserver.dao;

import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;

/**
 * Player DAO interface.
 * @author Zoey76
 */
public interface PlayerDAO {
	void storeCharBase(L2PcInstance player);
	
	/**
	 * Create a new player in the characters table of the database.
	 * @param player the player
	 * @return {@code true} if the player was inserted into the database
	 */
	boolean insert(L2PcInstance player);
	
	/**
	 * Updates the database with online status and last access of the player (called when login and logout).
	 * @param player the player
	 */
	void updateOnlineStatus(L2PcInstance player);
	
	/**
	 * Restores a player from the database.
	 * @param objectId the player's object ID
	 * @return the player
	 */
	L2PcInstance load(int objectId);
	
	/**
	 * Retrieve the name and ID of the other characters assigned to this account.
	 * @param player the player
	 */
	void loadCharacters(L2PcInstance player);
}
