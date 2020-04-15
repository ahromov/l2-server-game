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
import com.l2jserver.gameserver.dao.PetDAO;
import com.l2jserver.gameserver.data.xml.impl.PetDataTable;
import com.l2jserver.gameserver.model.L2PetLevelData;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.actor.instance.L2PetInstance;
import com.l2jserver.gameserver.model.actor.templates.L2NpcTemplate;
import com.l2jserver.gameserver.model.items.instance.L2ItemInstance;

/**
 * Pet DAO MySQL implementation.
 * @author Zoey76
 */
public class PetDAOMySQLImpl implements PetDAO {
	
	private static final Logger LOG = LoggerFactory.getLogger(PetDAOMySQLImpl.class);
	
	private static final String UPDATE_FOOD = "UPDATE pets SET fed=? WHERE item_obj_id=?";
	
	private static final String DELETE = "DELETE FROM pets WHERE item_obj_id=?";
	
	private static final String INSERT = "INSERT INTO pets (name,level,curHp,curMp,exp,sp,fed,ownerId,restore,item_obj_id) VALUES (?,?,?,?,?,?,?,?,?,?)";
	
	private static final String UPDATE = "UPDATE pets SET name=?,level=?,curHp=?,curMp=?,exp=?,sp=?,fed=?,ownerId=?,restore=? WHERE item_obj_id=?";
	
	@Override
	public void updateFood(L2PcInstance player, int petId) {
		if ((player.getControlItemId() != 0) && (petId != 0)) {
			try (var con = ConnectionFactory.getInstance().getConnection();
				var ps = con.prepareStatement(UPDATE_FOOD)) {
				ps.setInt(1, player.getCurrentFeed());
				ps.setInt(2, player.getControlItemId());
				ps.executeUpdate();
				player.setControlItemId(0);
			} catch (Exception e) {
				LOG.error("Failed to store Pet [NpcId: {}] data {}", petId, e);
			}
		}
	}
	
	@Override
	public void delete(L2PetInstance pet) {
		try (var con = ConnectionFactory.getInstance().getConnection();
			var ps = con.prepareStatement(DELETE)) {
			ps.setInt(1, pet.getControlObjectId());
			ps.execute();
		} catch (Exception e) {
			LOG.error("Failed to delete pet {}!", pet, e);
		}
	}
	
	@Override
	public L2PetInstance load(L2ItemInstance control, L2NpcTemplate template, L2PcInstance owner) {
		try (var con = ConnectionFactory.getInstance().getConnection();
			var ps = con.prepareStatement("SELECT item_obj_id, name, level, curHp, curMp, exp, sp, fed FROM pets WHERE item_obj_id=?")) {
			ps.setInt(1, control.getObjectId());
			try (var rset = ps.executeQuery()) {
				L2PetInstance pet;
				if (!rset.next()) {
					return new L2PetInstance(template, owner, control);
				}
				
				pet = new L2PetInstance(template, owner, control, rset.getByte("level"));
				pet.setRespawned(true);
				pet.setName(rset.getString("name"));
				
				long exp = rset.getLong("exp");
				L2PetLevelData info = PetDataTable.getInstance().getPetLevelData(pet.getId(), pet.getLevel());
				// DS: update experience based by level
				// Avoiding pet delevels due to exp per level values changed.
				if ((info != null) && (exp < info.getPetMaxExp())) {
					exp = info.getPetMaxExp();
				}
				
				pet.setExp(exp);
				pet.setSp(rset.getInt("sp"));
				
				pet.getStatus().setCurrentHp(rset.getInt("curHp"));
				pet.getStatus().setCurrentMp(rset.getInt("curMp"));
				pet.getStatus().setCurrentCp(pet.getMaxCp());
				if (rset.getDouble("curHp") < 1) {
					pet.setIsDead(true);
					pet.stopHpMpRegeneration();
				}
				
				pet.setCurrentFed(rset.getInt("fed"));
				return pet;
			}
		} catch (Exception e) {
			LOG.error("Could not restore pet data for owner: {}, {}", owner, e);
		}
		return null;
	}
	
	@Override
	public void insert(L2PetInstance pet) {
		insertOrUpdate(pet, INSERT);
	}
	
	@Override
	public void update(L2PetInstance pet) {
		insertOrUpdate(pet, UPDATE);
	}
	
	private static void insertOrUpdate(L2PetInstance pet, String query) {
		try (var con = ConnectionFactory.getInstance().getConnection();
			var ps = con.prepareStatement(query)) {
			ps.setString(1, pet.getName());
			ps.setInt(2, pet.getLevel());
			ps.setDouble(3, pet.getStatus().getCurrentHp());
			ps.setDouble(4, pet.getStatus().getCurrentMp());
			ps.setLong(5, pet.getExp());
			ps.setInt(6, pet.getSp());
			ps.setInt(7, pet.getCurrentFed());
			ps.setInt(8, pet.getOwner().getObjectId());
			ps.setString(9, String.valueOf(pet.isRestoreSummon())); // True restores pet on login
			ps.setInt(10, pet.getControlObjectId());
			ps.executeUpdate();
		} catch (Exception e) {
			LOG.error("Failed to store pet {} data!", pet, e);
		}
	}
}
