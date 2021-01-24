/*
 * Copyright © 2004-2021 L2J Server
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

import static com.l2jserver.gameserver.network.SystemMessageId.CANNOT_USE_ON_YOURSELF;
import static com.l2jserver.gameserver.network.SystemMessageId.S1_CANNOT_BE_USED;

import com.l2jserver.gameserver.model.actor.L2Character;
import com.l2jserver.gameserver.model.items.L2Item;
import com.l2jserver.gameserver.model.skills.Skill;
import com.l2jserver.gameserver.network.serverpackets.SystemMessage;

/**
 * Condition Target My Party.
 * @author Zoey76
 * @version 2.6.2.0
 */
public class ConditionTargetMyParty extends Condition {
	
	private static final String EXCEPT_ME = "EXCEPT_ME";
	
	private final String type;
	
	public ConditionTargetMyParty(String type) {
		this.type = type;
	}
	
	@Override
	public boolean testImpl(L2Character effector, L2Character effected, Skill skill, L2Item item) {
		final var player = effector.getActingPlayer();
		if (player == null) {
			return false;
		}
		
		if (EXCEPT_ME.equals(type) && (player == effected)) {
			effector.sendPacket(CANNOT_USE_ON_YOURSELF);
			return false;
		}
		
		if (player.isInParty()) {
			if (!player.isInPartyWith(effected)) {
				effector.sendPacket(SystemMessage.getSystemMessage(S1_CANNOT_BE_USED).addSkillName(skill));
				return false;
			}
		} else {
			final var effectedPlayer = effected.getActingPlayer();
			if (player != effectedPlayer) {
				effector.sendPacket(SystemMessage.getSystemMessage(S1_CANNOT_BE_USED).addSkillName(skill));
				return false;
			}
		}
		return true;
	}
}
