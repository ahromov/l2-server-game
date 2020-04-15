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
package com.l2jserver.gameserver.dao.impl.mysql;

import java.sql.ResultSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.l2jserver.commons.database.ConnectionFactory;
import com.l2jserver.gameserver.dao.FriendDAO;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;

/**
 * Friend DAO MySQL implementation.
 * @author Zoey76
 */
public class FriendDAOMySQLImpl implements FriendDAO {
	
	private static final Logger LOG = LoggerFactory.getLogger(FriendDAOMySQLImpl.class);
	
	private static final String SELECT = "SELECT friendId FROM character_friends WHERE charId=? AND relation=0 AND friendId<>charId";
	
	@Override
	public void load(L2PcInstance player) {
		try (var con = ConnectionFactory.getInstance().getConnection();
			var ps = con.prepareStatement(SELECT)) {
			ps.setInt(1, player.getObjectId());
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					player.addFriend(rs.getInt("friendId"));
				}
			}
		} catch (Exception e) {
			LOG.error("Error found in {} FriendList: ", player, e);
		}
	}
}
