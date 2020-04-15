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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.l2jserver.commons.database.ConnectionFactory;
import com.l2jserver.gameserver.dao.TeleportBookmarkDAO;
import com.l2jserver.gameserver.model.TeleportBookmark;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;

/**
 * Teleport Bookmark DAO MySQL implementation.
 * @author Zoey76
 */
public class TeleportBookmarkDAOMySQLImpl implements TeleportBookmarkDAO {
	
	private static final Logger LOG = LoggerFactory.getLogger(TeleportBookmarkDAOMySQLImpl.class);
	
	private static final String INSERT = "INSERT INTO character_tpbookmark (charId,Id,x,y,z,icon,tag,name) values (?,?,?,?,?,?,?,?)";
	
	private static final String UPDATE = "UPDATE character_tpbookmark SET icon=?,tag=?,name=? where charId=? AND Id=?";
	
	private static final String SELECT = "SELECT Id,x,y,z,icon,tag,name FROM character_tpbookmark WHERE charId=?";
	
	private static final String DELETE = "DELETE FROM character_tpbookmark WHERE charId=? AND Id=?";
	
	@Override
	public void delete(L2PcInstance player, int id) {
		try (var con = ConnectionFactory.getInstance().getConnection();
			var ps = con.prepareStatement(DELETE)) {
			ps.setInt(1, player.getObjectId());
			ps.setInt(2, id);
			ps.execute();
		} catch (Exception e) {
			LOG.error("Could not delete character teleport bookmark data: {}", e);
		}
	}
	
	@Override
	public void insert(L2PcInstance player, int id, int x, int y, int z, int icon, String tag, String name) {
		try (var con = ConnectionFactory.getInstance().getConnection();
			var ps = con.prepareStatement(INSERT)) {
			ps.setInt(1, player.getObjectId());
			ps.setInt(2, id);
			ps.setInt(3, x);
			ps.setInt(4, y);
			ps.setInt(5, z);
			ps.setInt(6, icon);
			ps.setString(7, tag);
			ps.setString(8, name);
			ps.execute();
		} catch (Exception e) {
			LOG.warn("Could not insert character teleport bookmark data: {}", e);
		}
	}
	
	@Override
	public void update(L2PcInstance player, int id, int icon, String tag, String name) {
		try (var con = ConnectionFactory.getInstance().getConnection();
			var ps = con.prepareStatement(UPDATE)) {
			ps.setInt(1, icon);
			ps.setString(2, tag);
			ps.setString(3, name);
			ps.setInt(4, player.getObjectId());
			ps.setInt(5, id);
			ps.execute();
		} catch (Exception e) {
			LOG.error("Could not update character teleport bookmark data: {}", e);
		}
	}
	
	@Override
	public void load(L2PcInstance player) {
		try (var con = ConnectionFactory.getInstance().getConnection();
			var ps = con.prepareStatement(SELECT)) {
			ps.setInt(1, player.getObjectId());
			try (var rs = ps.executeQuery()) {
				while (rs.next()) {
					player.getTpbookmarks().put(rs.getInt("Id"), new TeleportBookmark(rs.getInt("Id"), rs.getInt("x"), rs.getInt("y"), rs.getInt("z"), rs.getInt("icon"), rs.getString("tag"), rs.getString("name")));
				}
			}
		} catch (Exception e) {
			LOG.error("Failed restoing character teleport bookmark.", e);
		}
	}
}
