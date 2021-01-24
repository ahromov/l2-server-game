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
package com.l2jserver.gameserver.model.skills.targets;

import com.l2jserver.gameserver.model.L2Object;
import com.l2jserver.gameserver.model.actor.L2Character;
import com.l2jserver.gameserver.model.actor.L2Npc;

/**
 * Affect Object.
 * @author Zoey76
 * @version 2.6.2.0
 */
public enum AffectObject {
	ALL {
		@Override
		public boolean affectObject(L2Character caster, L2Object object) {
			return true;
		}
	},
	CLAN {
		@Override
		public boolean affectObject(L2Character caster, L2Object object) {
			if (caster.isPlayable()) {
				final var clanId = caster.getClanId();
				if (clanId == 0) {
					return false;
				}
				
				if (!object.isPlayable()) {
					return false;
				}
				
				final var creature = (L2Character) object;
				return clanId == creature.getClanId();
			} else if (caster.isNpc()) {
				// TODO(Zoey76): Implement.
			}
			return false;
		}
	},
	FRIEND {
		@Override
		public boolean affectObject(L2Character caster, L2Object object) {
			return !object.isAutoAttackable(caster);
		}
	},
	HIDDEN_PLACE {
		@Override
		public boolean affectObject(L2Character caster, L2Object object) {
			// TODO(Zoey76): Implement.
			return false;
		}
	},
	INVISIBLE {
		@Override
		public boolean affectObject(L2Character caster, L2Object object) {
			return object.isInvisible();
		}
	},
	NONE {
		@Override
		public boolean affectObject(L2Character caster, L2Object object) {
			return false;
		}
	},
	NOT_FRIEND {
		@Override
		public boolean affectObject(L2Character caster, L2Object object) {
			return object.isAutoAttackable(caster);
		}
	},
	OBJECT_DEAD_NPC_BODY {
		@Override
		public boolean affectObject(L2Character caster, L2Object object) {
			if (!object.isNpc()) {
				return false;
			}
			
			final var npc = (L2Npc) object;
			return npc.isDead();
		}
	},
	UNDEAD_REAL_ENEMY {
		@Override
		public boolean affectObject(L2Character caster, L2Object object) {
			if (!object.isNpc()) {
				return false;
			}
			
			final var npc = (L2Npc) object;
			return npc.isUndead();
		}
	},
	WYVERN_OBJECT {
		@Override
		public boolean affectObject(L2Character caster, L2Object object) {
			if (!object.isNpc()) {
				return false;
			}
			return WYVERN_ID == object.getId();
		}
	};
	
	private static final int WYVERN_ID = 12621;
	
	public abstract boolean affectObject(L2Character caster, L2Object object);
}
