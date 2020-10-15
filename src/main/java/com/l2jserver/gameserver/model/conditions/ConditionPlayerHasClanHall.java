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

import java.util.List;

import com.l2jserver.gameserver.model.L2Clan;
import com.l2jserver.gameserver.model.actor.L2Character;
import com.l2jserver.gameserver.model.items.L2Item;
import com.l2jserver.gameserver.model.skills.Skill;

/**
 * Has clan hall condition.
 * @author MrPoke
 */
public final class ConditionPlayerHasClanHall extends Condition {
	private final List<Integer> _clanHalls;
	
	public ConditionPlayerHasClanHall(List<Integer> clanHalls) {
		_clanHalls = clanHalls;
	}
	
	@Override
	public boolean testImpl(L2Character effector, L2Character effected, Skill skill, L2Item item) {
		if (effector.getActingPlayer() == null) {
			return false;
		}
		
		final L2Clan clan = effector.getActingPlayer().getClan();
		if (clan == null) {
			return ((_clanHalls.size() == 1) && (_clanHalls.get(0) == 0));
		}
		
		// All Clan Hall
		if ((_clanHalls.size() == 1) && (_clanHalls.get(0) == -1)) {
			return clan.getHideoutId() > 0;
		}
		return _clanHalls.contains(clan.getHideoutId());
	}
}
