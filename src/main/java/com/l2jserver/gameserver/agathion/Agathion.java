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
package com.l2jserver.gameserver.agathion;

/**
 * Agathion.
 * @author Zoey76
 * @version 2.6.2.0
 */
public class Agathion {
	
	private final int npcId;
	
	private final int id;
	
	private final int itemId;
	
	private final int energy;
	
	private final int maxEnergy;
	
	public Agathion(int npcId, int id, int itemId, int energy, int maxEnergy) {
		this.npcId = npcId;
		this.id = id;
		this.itemId = itemId;
		this.energy = energy;
		this.maxEnergy = maxEnergy;
	}
	
	public int getNpcId() {
		return npcId;
	}
	
	public int getId() {
		return id;
	}
	
	public int getItemId() {
		return itemId;
	}
	
	public int getEnergy() {
		return energy;
	}
	
	public int getMaxEnergy() {
		return maxEnergy;
	}
}
