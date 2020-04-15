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
import com.l2jserver.gameserver.dao.PetSkillSaveDAO;
import com.l2jserver.gameserver.data.sql.impl.SummonEffectsTable;
import com.l2jserver.gameserver.datatables.SkillData;
import com.l2jserver.gameserver.model.actor.instance.L2PetInstance;
import com.l2jserver.gameserver.model.skills.AbnormalType;
import com.l2jserver.gameserver.model.skills.BuffInfo;
import com.l2jserver.gameserver.model.skills.EffectScope;
import com.l2jserver.gameserver.model.skills.Skill;

/**
 * Pet Skill Save MySQL implementation.
 * @author Zoey76
 */
public class PetSkillSaveDAOMySQL implements PetSkillSaveDAO {
	
	private static final Logger LOG = LoggerFactory.getLogger(PetSkillSaveDAOMySQL.class);
	
	private static final String ADD_SKILL_SAVE = "INSERT INTO character_pet_skills_save (petObjItemId,skill_id,skill_level,remaining_time,buff_index) VALUES (?,?,?,?,?)";
	
	private static final String RESTORE_SKILL_SAVE = "SELECT petObjItemId,skill_id,skill_level,remaining_time,buff_index FROM character_pet_skills_save WHERE petObjItemId=? ORDER BY buff_index ASC";
	
	private static final String DELETE_SKILL_SAVE = "DELETE FROM character_pet_skills_save WHERE petObjItemId=?";
	
	@Override
	public void insert(L2PetInstance pet, boolean storeEffects) {
		try (var con = ConnectionFactory.getInstance().getConnection();
			var ps1 = con.prepareStatement(DELETE_SKILL_SAVE);
			var ps2 = con.prepareStatement(ADD_SKILL_SAVE)) {
			// Delete all current stored effects for summon to avoid dupe
			ps1.setInt(1, pet.getControlObjectId());
			ps1.execute();
			
			int buff_index = 0;
			
			final List<Integer> storedSkills = new LinkedList<>();
			
			// Store all effect data along with calculated remaining
			if (storeEffects) {
				for (BuffInfo info : pet.getEffectList().getEffects()) {
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
					
					ps2.setInt(1, pet.getControlObjectId());
					ps2.setInt(2, skill.getId());
					ps2.setInt(3, skill.getLevel());
					ps2.setInt(4, info.getTime());
					ps2.setInt(5, ++buff_index);
					ps2.execute();
					
					SummonEffectsTable.getInstance().addPetEffect(pet.getControlObjectId(), skill, info.getTime());
				}
			}
		} catch (Exception e) {
			LOG.warn("Could not store pet effect data!", e);
		}
	}
	
	@Override
	public void load(L2PetInstance pet) {
		try (var con = ConnectionFactory.getInstance().getConnection();
			var ps1 = con.prepareStatement(RESTORE_SKILL_SAVE);
			var ps2 = con.prepareStatement(DELETE_SKILL_SAVE)) {
			if (!SummonEffectsTable.getInstance().containsPetId(pet.getControlObjectId())) {
				ps1.setInt(1, pet.getControlObjectId());
				try (var rs = ps1.executeQuery()) {
					while (rs.next()) {
						int effectCurTime = rs.getInt("remaining_time");
						
						final Skill skill = SkillData.getInstance().getSkill(rs.getInt("skill_id"), rs.getInt("skill_level"));
						if (skill == null) {
							continue;
						}
						
						if (skill.hasEffects(EffectScope.GENERAL)) {
							SummonEffectsTable.getInstance().addPetEffect(pet.getControlObjectId(), skill, effectCurTime);
						}
					}
				}
			}
			
			ps2.setInt(1, pet.getControlObjectId());
			ps2.executeUpdate();
		} catch (Exception e) {
			LOG.warn("Could not restore {} active effect data!", pet, e);
		}
	}
	
}
