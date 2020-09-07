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
package com.l2jserver.gameserver.model.conditions;

import static com.l2jserver.gameserver.model.itemcontainer.Inventory.PAPERDOLL_LBRACELET;

import com.l2jserver.gameserver.agathion.repository.AgathionRepository;
import com.l2jserver.gameserver.model.actor.L2Character;
import com.l2jserver.gameserver.model.items.L2Item;
import com.l2jserver.gameserver.model.skills.Skill;

/**
 * Condition agathion energy.
 * @author Zoey76
 * @version 2.6.2.0
 */
public class ConditionPlayerAgathionEnergy extends Condition {
	
	private final int energy;
	
	public ConditionPlayerAgathionEnergy(int energy) {
		this.energy = energy;
	}
	
	@Override
	public boolean testImpl(L2Character effector, L2Character effected, Skill skill, L2Item item) {
		if (!effector.isPlayer()) {
			return false;
		}
		
		final var player = effector.getActingPlayer();
		final var agathionInfo = AgathionRepository.getInstance().getByNpcId(player.getAgathionId());
		if ((agathionInfo == null) || (agathionInfo.getMaxEnergy() <= 0)) {
			return false;
		}
		
		final var agathionItem = player.getInventory().getPaperdollItem(PAPERDOLL_LBRACELET);
		if ((agathionItem == null) || (agathionInfo.getItemId() != agathionItem.getId())) {
			return false;
		}
		return agathionItem.getAgathionRemainingEnergy() >= energy;
	}
}
