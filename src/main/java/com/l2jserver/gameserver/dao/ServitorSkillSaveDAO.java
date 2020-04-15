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

import com.l2jserver.gameserver.model.actor.instance.L2ServitorInstance;

/**
 * Servitor Skill Save DAO interface.
 * @author Zoey76
 */
public interface ServitorSkillSaveDAO {
	/**
	 * Stores the servitor skills in the database.
	 * @param servitor the servitor
	 * @param storeEffects if {@code true} effects will be stored
	 */
	void insert(L2ServitorInstance servitor, boolean storeEffects);
	
	/**
	 * Restores the servitor skills from the database.
	 * @param servitor the servitor.
	 */
	void load(L2ServitorInstance servitor);
}
