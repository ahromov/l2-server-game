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
import com.l2jserver.gameserver.dao.SubclassDAO;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.base.SubClass;

/**
 * Subclass DAO MySQL implementation.
 * @author Zoey76
 */
public class SubclassDAOMySQLImpl implements SubclassDAO {
	
	private static final Logger LOG = LoggerFactory.getLogger(SubclassDAOMySQLImpl.class);
	
	private static final String SELECT = "SELECT class_id,exp,sp,level,class_index FROM character_subclasses WHERE charId=? ORDER BY class_index ASC";
	
	private static final String INSERT = "INSERT INTO character_subclasses (charId,class_id,exp,sp,level,class_index) VALUES (?,?,?,?,?,?)";
	
	private static final String UPDATE = "UPDATE character_subclasses SET exp=?,sp=?,level=?,class_id=? WHERE charId=? AND class_index =?";
	
	private static final String DELETE = "DELETE FROM character_subclasses WHERE charId=? AND class_index=?";
	
	@Override
	public void update(L2PcInstance player) {
		if (player.getTotalSubClasses() <= 0) {
			return;
		}
		
		try (var con = ConnectionFactory.getInstance().getConnection();
			var ps = con.prepareStatement(UPDATE)) {
			for (SubClass subClass : player.getSubClasses().values()) {
				ps.setLong(1, subClass.getExp());
				ps.setInt(2, subClass.getSp());
				ps.setInt(3, subClass.getLevel());
				ps.setInt(4, subClass.getClassId());
				ps.setInt(5, player.getObjectId());
				ps.setInt(6, subClass.getClassIndex());
				ps.addBatch();
			}
			ps.executeBatch();
		} catch (Exception e) {
			LOG.error("Could not store sub class data for {} : {}", player, e);
		}
	}
	
	@Override
	public boolean insert(L2PcInstance player, SubClass newClass) {
		try (var con = ConnectionFactory.getInstance().getConnection();
			var ps = con.prepareStatement(INSERT)) {
			ps.setInt(1, player.getObjectId());
			ps.setInt(2, newClass.getClassId());
			ps.setLong(3, newClass.getExp());
			ps.setInt(4, newClass.getSp());
			ps.setInt(5, newClass.getLevel());
			ps.setInt(6, newClass.getClassIndex());
			ps.execute();
		} catch (Exception e) {
			LOG.error("Could not add character sub class for {}, {}", player, e);
			return false;
		}
		return true;
	}
	
	@Override
	public void delete(L2PcInstance player, int classIndex) {
		try (var con = ConnectionFactory.getInstance().getConnection();
			var ps = con.prepareStatement(DELETE)) {
			ps.setInt(1, player.getObjectId());
			ps.setInt(2, classIndex);
			ps.execute();
		} catch (Exception e) {
			LOG.error("Could not delete subclass for {} to class index {}, {}", player, classIndex, e);
		}
	}
	
	@Override
	public void load(L2PcInstance player) {
		try (var con = ConnectionFactory.getInstance().getConnection();
			var ps = con.prepareStatement(SELECT)) {
			ps.setInt(1, player.getObjectId());
			try (var rs = ps.executeQuery()) {
				while (rs.next()) {
					final SubClass subClass = new SubClass(player);
					subClass.setClassId(rs.getInt("class_id"));
					subClass.setExp(rs.getLong("exp"));
					subClass.setLevel(rs.getInt("level"));
					subClass.setSp(rs.getInt("sp"));
					subClass.setClassIndex(rs.getInt("class_index"));
					
					// Enforce the correct indexing of _subClasses against their class indexes.
					player.getSubClasses().put(subClass.getClassIndex(), subClass);
				}
			}
		} catch (Exception e) {
			LOG.error("Could not restore classes for {}, {}", player, e);
		}
	}
}
