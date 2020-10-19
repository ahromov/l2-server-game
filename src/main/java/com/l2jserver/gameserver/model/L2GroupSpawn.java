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
package com.l2jserver.gameserver.model;

import static com.l2jserver.gameserver.config.Configuration.general;

import java.util.logging.Level;

import com.l2jserver.commons.util.Rnd;
import com.l2jserver.gameserver.data.sql.impl.TerritoryTable;
import com.l2jserver.gameserver.model.actor.L2Npc;
import com.l2jserver.gameserver.model.actor.instance.L2ControllableMobInstance;
import com.l2jserver.gameserver.model.actor.templates.L2NpcTemplate;

/**
 * @author littlecrow
 */
public class L2GroupSpawn extends L2Spawn {
	private final L2NpcTemplate _template;
	
	public L2GroupSpawn(L2NpcTemplate mobTemplate) throws SecurityException, ClassNotFoundException, NoSuchMethodException {
		super(mobTemplate);
		_template = mobTemplate;
		
		setAmount(1);
	}
	
	public L2Npc doGroupSpawn() {
		try {
			if (_template.isType("L2Pet") || _template.isType("L2Minion")) {
				return null;
			}
			
			int newLocX = 0;
			int newLocY = 0;
			int newLocZ = 0;
			
			if ((getX() == 0) && (getY() == 0)) {
				if (getLocationId() == 0) {
					return null;
				}
				
				final Location location = TerritoryTable.getInstance().getRandomPoint(getLocationId());
				if (location != null) {
					newLocX = location.getX();
					newLocY = location.getY();
					newLocZ = location.getZ();
				}
			} else {
				newLocX = getX();
				newLocY = getY();
				newLocZ = getZ();
			}
			
			final L2Npc mob = new L2ControllableMobInstance(_template);
			mob.setCurrentHpMp(mob.getMaxHp(), mob.getMaxMp());
			
			if (getHeading() == -1) {
				mob.setHeading(Rnd.nextInt(61794));
			} else {
				mob.setHeading(getHeading());
			}
			
			mob.setSpawn(this);
			mob.spawnMe(newLocX, newLocY, newLocZ);
			mob.onSpawn();
			
			if (general().debug()) {
				_log.finest("Spawned Mob Id: " + _template.getId() + " ,at: X: " + mob.getX() + " Y: " + mob.getY() + " Z: " + mob.getZ());
			}
			return mob;
			
		} catch (Exception e) {
			_log.log(Level.WARNING, "NPC class not found: " + e.getMessage(), e);
			return null;
		}
	}
}