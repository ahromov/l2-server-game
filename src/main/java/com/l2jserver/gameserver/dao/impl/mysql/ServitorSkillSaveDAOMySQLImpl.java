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

import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.l2jserver.commons.database.ConnectionFactory;
import com.l2jserver.gameserver.dao.ServitorSkillSaveDAO;
import com.l2jserver.gameserver.data.sql.impl.SummonEffectsTable;
import com.l2jserver.gameserver.datatables.SkillData;
import com.l2jserver.gameserver.model.actor.instance.L2ServitorInstance;
import com.l2jserver.gameserver.model.skills.AbnormalType;
import com.l2jserver.gameserver.model.skills.BuffInfo;
import com.l2jserver.gameserver.model.skills.EffectScope;
import com.l2jserver.gameserver.model.skills.Skill;

/**
 * Servitor Skill Save DAO MySQL implementation.
 * @author Zoey76
 */
public class ServitorSkillSaveDAOMySQLImpl implements ServitorSkillSaveDAO {
	
	private static final Logger LOG = LoggerFactory.getLogger(ServitorSkillSaveDAOMySQLImpl.class);
	
	private static final String ADD_SKILL_SAVE = "INSERT INTO character_summon_skills_save (ownerId,ownerClassIndex,summonSkillId,skill_id,skill_level,remaining_time,buff_index) VALUES (?,?,?,?,?,?,?)";
	
	private static final String RESTORE_SKILL_SAVE = "SELECT skill_id,skill_level,remaining_time,buff_index FROM character_summon_skills_save WHERE ownerId=? AND ownerClassIndex=? AND summonSkillId=? ORDER BY buff_index ASC";
	
	private static final String DELETE_SKILL_SAVE = "DELETE FROM character_summon_skills_save WHERE ownerId=? AND ownerClassIndex=? AND summonSkillId=?";
	
	@Override
	public void insert(L2ServitorInstance servitor, boolean storeEffects) {
		try (var con = ConnectionFactory.getInstance().getConnection();
			var ps = con.prepareStatement(DELETE_SKILL_SAVE)) {
			con.setAutoCommit(false);
			// Delete all current stored effects for summon to avoid dupe
			ps.setInt(1, servitor.getOwner().getObjectId());
			ps.setInt(2, servitor.getOwner().getClassIndex());
			ps.setInt(3, servitor.getReferenceSkill());
			ps.execute();
			
			int buff_index = 0;
			
			final List<Integer> storedSkills = new LinkedList<>();
			
			// Store all effect data along with calculated remaining
			if (storeEffects) {
				try (var ps2 = con.prepareStatement(ADD_SKILL_SAVE)) {
					for (BuffInfo info : servitor.getEffectList().getEffects()) {
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
						
						ps2.setInt(1, servitor.getOwner().getObjectId());
						ps2.setInt(2, servitor.getOwner().getClassIndex());
						ps2.setInt(3, servitor.getReferenceSkill());
						ps2.setInt(4, skill.getId());
						ps2.setInt(5, skill.getLevel());
						ps2.setInt(6, info.getTime());
						ps2.setInt(7, ++buff_index);
						ps2.addBatch();
						
						SummonEffectsTable.getInstance().addServitorEffect(servitor.getOwner(), servitor.getReferenceSkill(), skill, info.getTime());
					}
					ps2.executeBatch();
				}
			}
			con.commit();
		} catch (Exception e) {
			LOG.error("Could not store summon effect data for owner {},  class index {}, skill {}!", servitor.getOwner().getObjectId(), servitor.getOwner().getClassIndex(), servitor.getReferenceSkill(), e);
		}
	}
	
	@Override
	public void load(L2ServitorInstance servitor) {
		try (var con = ConnectionFactory.getInstance().getConnection()) {
			if (!SummonEffectsTable.getInstance().containsSkill(servitor.getOwner(), servitor.getReferenceSkill())) {
				try (var ps = con.prepareStatement(RESTORE_SKILL_SAVE)) {
					ps.setInt(1, servitor.getOwner().getObjectId());
					ps.setInt(2, servitor.getOwner().getClassIndex());
					ps.setInt(3, servitor.getReferenceSkill());
					try (var rs = ps.executeQuery()) {
						while (rs.next()) {
							int effectCurTime = rs.getInt("remaining_time");
							
							final Skill skill = SkillData.getInstance().getSkill(rs.getInt("skill_id"), rs.getInt("skill_level"));
							if (skill == null) {
								continue;
							}
							
							if (skill.hasEffects(EffectScope.GENERAL)) {
								SummonEffectsTable.getInstance().addServitorEffect(servitor.getOwner(), servitor.getReferenceSkill(), skill, effectCurTime);
							}
						}
					}
				}
			}
			
			try (var ps = con.prepareStatement(DELETE_SKILL_SAVE)) {
				ps.setInt(1, servitor.getOwner().getObjectId());
				ps.setInt(2, servitor.getOwner().getClassIndex());
				ps.setInt(3, servitor.getReferenceSkill());
				ps.executeUpdate();
			}
		} catch (Exception e) {
			LOG.error("Could not restore {} active effect data!", servitor, e);
		}
	}
}
