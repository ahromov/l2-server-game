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
package com.l2jserver.gameserver.model.base;

import static com.l2jserver.gameserver.config.Configuration.character;

import com.l2jserver.gameserver.data.json.ExperienceData;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.actor.stat.PcStat;

/**
 * Character Sub-Class Definition <BR>
 * Used to store key information about a character's sub-class.
 * @author Zealar
 */
public final class SubClass {
	private PlayerClass _class;
	private int _classIndex = 1;
	private final PcStat _stats;
	
	public SubClass(L2PcInstance activeChar) {
		_stats = new PcStat(activeChar);
		_stats.setExp(ExperienceData.getInstance().getExpForLevel(character().getBaseSubclassLevel()));
		_stats.setLevel(character().getBaseSubclassLevel());
	}
	
	public PlayerClass getClassDefinition() {
		return _class;
	}
	
	public void setClassIndex(int classIndex) {
		_classIndex = classIndex;
	}
	
	/**
	 * First Sub-Class is index 1.
	 * @return int _classIndex
	 */
	public int getClassIndex() {
		return _classIndex;
	}
	
	public void setClassId(int classId) {
		_class = PlayerClass.values()[classId];
	}
	
	public int getClassId() {
		return _class.ordinal();
	}
	
	public long getExp() {
		return _stats.getExp();
	}
	
	public int getLevel() {
		return _stats.getLevel();
	}
	
	public int getSp() {
		return _stats.getSp();
	}
	
	public void setSp(int sp) {
		_stats.setSp(sp);
	}
	
	public void setExp(long exp) {
		_stats.setExp(exp);
	}
	
	public void setLevel(int level) {
		_stats.setLevel(level);
	}
	
	public void addExp(long exp) {
		_stats.addExp(exp);
	}
	
	public PcStat getStat() {
		return _stats;
	}
}