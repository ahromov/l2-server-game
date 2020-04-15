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

import static com.l2jserver.gameserver.config.Configuration.character;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.l2jserver.commons.database.ConnectionFactory;
import com.l2jserver.gameserver.dao.PlayerSkillSaveDAO;
import com.l2jserver.gameserver.datatables.SkillData;
import com.l2jserver.gameserver.model.TimeStamp;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.skills.AbnormalType;
import com.l2jserver.gameserver.model.skills.BuffInfo;
import com.l2jserver.gameserver.model.skills.Skill;

/**
 * Player Skill Save DAO MySQL implementation.
 * @author Zoey76
 */
public class PlayerSkillSaveDAOMySQLImpl implements PlayerSkillSaveDAO {
	
	private static final Logger LOG = LoggerFactory.getLogger(PlayerSkillSaveDAOMySQLImpl.class);
	
	private static final String INSERT = "INSERT INTO character_skills_save (charId,skill_id,skill_level,remaining_time,reuse_delay,systime,restore_type,class_index,buff_index) VALUES (?,?,?,?,?,?,?,?,?)";
	
	private static final String SELECT = "SELECT skill_id,skill_level,remaining_time, reuse_delay, systime, restore_type FROM character_skills_save WHERE charId=? AND class_index=? ORDER BY buff_index ASC";
	
	private static final String DELETE = "DELETE FROM character_skills_save WHERE charId=? AND class_index=?";
	
	@Override
	public void delete(L2PcInstance player, int classIndex) {
		try (var con = ConnectionFactory.getInstance().getConnection();
			var ps = con.prepareStatement(DELETE)) {
			ps.setInt(1, player.getObjectId());
			ps.setInt(2, classIndex);
			ps.execute();
		} catch (Exception e) {
			LOG.error("Could not delete all effect data!", player, e);
		}
	}
	
	@Override
	public void delete(L2PcInstance player) {
		delete(player, player.getClassIndex());
	}
	
	@Override
	public void insert(L2PcInstance player, boolean storeEffects) {
		try (var con = ConnectionFactory.getInstance().getConnection();
			var ps = con.prepareStatement(INSERT)) {
			int buff_index = 0;
			final List<Integer> storedSkills = new ArrayList<>();
			
			// Store all effect data along with calculated remaining
			// reuse delays for matching skills. 'restore_type'= 0.
			if (storeEffects) {
				for (BuffInfo info : player.getEffectList().getEffects()) {
					if (info == null) {
						continue;
					}
					
					final Skill skill = info.getSkill();
					// Do not save heals.
					if (skill.getAbnormalType() == AbnormalType.LIFE_FORCE_OTHERS) {
						continue;
					}
					
					if (skill.isToggle()) {
						continue;
					}
					
					// Dances and songs are not kept in retail.
					if (skill.isDance() && !character().storeDances()) {
						continue;
					}
					
					if (storedSkills.contains(skill.getReuseHashCode())) {
						continue;
					}
					
					storedSkills.add(skill.getReuseHashCode());
					
					ps.setInt(1, player.getObjectId());
					ps.setInt(2, skill.getId());
					ps.setInt(3, skill.getLevel());
					ps.setInt(4, info.getTime());
					
					final TimeStamp t = player.getSkillReuseTimeStamp(skill.getReuseHashCode());
					ps.setLong(5, (t != null) && t.hasNotPassed() ? t.getReuse() : 0);
					ps.setLong(6, (t != null) && t.hasNotPassed() ? t.getStamp() : 0);
					
					ps.setInt(7, 0); // Store type 0, active buffs/debuffs.
					ps.setInt(8, player.getClassIndex());
					ps.setInt(9, ++buff_index);
					ps.execute();
				}
			}
			
			// Skills under reuse.
			final Map<Integer, TimeStamp> reuseTimeStamps = player.getSkillReuseTimeStamps();
			if (reuseTimeStamps != null) {
				for (Entry<Integer, TimeStamp> ts : reuseTimeStamps.entrySet()) {
					final int hash = ts.getKey();
					if (storedSkills.contains(hash)) {
						continue;
					}
					
					final TimeStamp t = ts.getValue();
					if ((t != null) && t.hasNotPassed()) {
						storedSkills.add(hash);
						
						ps.setInt(1, player.getObjectId());
						ps.setInt(2, t.getSkillId());
						ps.setInt(3, t.getSkillLvl());
						ps.setInt(4, -1);
						ps.setLong(5, t.getReuse());
						ps.setLong(6, t.getStamp());
						ps.setInt(7, 1); // Restore type 1, skill reuse.
						ps.setInt(8, player.getClassIndex());
						ps.setInt(9, ++buff_index);
						ps.execute();
					}
				}
			}
		} catch (Exception e) {
			LOG.error("Could not store {} effect data!", player, e);
		}
	}
	
	@Override
	public void load(L2PcInstance player) {
		try (var con = ConnectionFactory.getInstance().getConnection();
			var ps = con.prepareStatement(SELECT)) {
			ps.setInt(1, player.getObjectId());
			ps.setInt(2, player.getClassIndex());
			try (var rs = ps.executeQuery()) {
				while (rs.next()) {
					int remainingTime = rs.getInt("remaining_time");
					long reuseDelay = rs.getLong("reuse_delay");
					long systime = rs.getLong("systime");
					int restoreType = rs.getInt("restore_type");
					
					final Skill skill = SkillData.getInstance().getSkill(rs.getInt("skill_id"), rs.getInt("skill_level"));
					if (skill == null) {
						continue;
					}
					
					final long time = systime - System.currentTimeMillis();
					if (time > 10) {
						player.disableSkill(skill, time);
						player.addTimeStamp(skill, reuseDelay, systime);
					}
					
					// Restore Type 1 The remaining skills lost effect upon logout but were still under a high reuse delay.
					if (restoreType > 0) {
						continue;
					}
					
					// Restore Type 0 These skill were still in effect on the character upon logout.
					// Some of which were self casted and might still have had a long reuse delay which also is restored.
					skill.applyEffects(player, player, false, remainingTime);
				}
			}
		} catch (Exception e) {
			LOG.error("Could not restore {} active effect data!", player, e);
		}
	}
}
