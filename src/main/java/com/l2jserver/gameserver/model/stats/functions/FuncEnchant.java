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
package com.l2jserver.gameserver.model.stats.functions;

import static com.l2jserver.gameserver.config.Configuration.olympiad;

import com.l2jserver.gameserver.model.actor.L2Character;
import com.l2jserver.gameserver.model.conditions.Condition;
import com.l2jserver.gameserver.model.items.L2Item;
import com.l2jserver.gameserver.model.items.instance.L2ItemInstance;
import com.l2jserver.gameserver.model.items.type.WeaponType;
import com.l2jserver.gameserver.model.skills.Skill;
import com.l2jserver.gameserver.model.stats.Stats;

public class FuncEnchant extends AbstractFunction {
	public FuncEnchant(Stats stat, int order, Object owner, double value, Condition applyCond) {
		super(stat, order, owner, value, applyCond);
	}
	
	@SuppressWarnings("incomplete-switch")
	@Override
	public double calc(L2Character effector, L2Character effected, Skill skill, double initVal) {
		double value = initVal;
		if ((getApplyCond() != null) && !getApplyCond().test(effector, effected, skill)) {
			return value;
		}
		
		L2ItemInstance item = (L2ItemInstance) getFuncOwner();
		int enchant = item.getEnchantLevel();
		if (enchant <= 0) {
			return value;
		}
		
		int overEnchant = 0;
		if (enchant > 3) {
			overEnchant = enchant - 3;
			enchant = 3;
		}
		
		if (effector.isPlayer()) {
			if (effector.getActingPlayer().isInOlympiadMode() && (olympiad().getEnchantLimit() >= 0) && ((enchant + overEnchant) > olympiad().getEnchantLimit())) {
				if (olympiad().getEnchantLimit() > 3) {
					overEnchant = olympiad().getEnchantLimit() - 3;
				} else {
					overEnchant = 0;
					enchant = olympiad().getEnchantLimit();
				}
			}
		}
		
		if ((getStat() == Stats.MAGIC_DEFENCE) || (getStat() == Stats.POWER_DEFENCE)) {
			return value + enchant + (3 * overEnchant);
		}
		
		if (getStat() == Stats.MAGIC_ATTACK) {
			// M. Atk. increases by 4 for all weapons.
			// Starting at +4, M. Atk. bonus double.
			// M. Atk. increases by 3 for all weapons.
			// Starting at +4, M. Atk. bonus double.
			// M. Atk. increases by 2 for all weapons. Starting at +4, M. Atk. bonus double.
			// Starting at +4, M. Atk. bonus double.
			switch (item.getItem().getItemGradeSPlus()) {
				case S -> value += (4 * enchant) + (8 * overEnchant);
				case A, B, C -> value += (3 * enchant) + (6 * overEnchant);
				case D, NONE -> value += (2 * enchant) + (4 * overEnchant);
			}
			return value;
		}
		
		if (item.isWeapon()) {
			final WeaponType type = (WeaponType) item.getItemType();
			switch (item.getItem().getItemGradeSPlus()) {
				case S:
					if (item.getWeaponItem().getBodyPart() == L2Item.SLOT_LR_HAND) {
						if ((type == WeaponType.BOW) || (type == WeaponType.CROSSBOW)) {
							// P. Atk. increases by 10 for bows.
							// Starting at +4, P. Atk. bonus double.
							value += (10 * enchant) + (20 * overEnchant);
						} else {
							// P. Atk. increases by 6 for two-handed swords, two-handed blunts, dualswords, and two-handed combat weapons.
							// Starting at +4, P. Atk. bonus double.
							value += (6 * enchant) + (12 * overEnchant);
						}
					} else {
						// P. Atk. increases by 5 for one-handed swords, one-handed blunts, daggers, spears, and other weapons.
						// Starting at +4, P. Atk. bonus double.
						value += (5 * enchant) + (10 * overEnchant);
					}
					break;
				case A:
					if (item.getWeaponItem().getBodyPart() == L2Item.SLOT_LR_HAND) {
						if ((type == WeaponType.BOW) || (type == WeaponType.CROSSBOW)) {
							// P. Atk. increases by 8 for bows.
							// Starting at +4, P. Atk. bonus double.
							value += (8 * enchant) + (16 * overEnchant);
						} else {
							// P. Atk. increases by 5 for two-handed swords, two-handed blunts, dualswords, and two-handed combat weapons.
							// Starting at +4, P. Atk. bonus double.
							value += (5 * enchant) + (10 * overEnchant);
						}
					} else {
						// P. Atk. increases by 4 for one-handed swords, one-handed blunts, daggers, spears, and other weapons.
						// Starting at +4, P. Atk. bonus double.
						value += (4 * enchant) + (8 * overEnchant);
					}
					break;
				case B:
				case C:
					if (item.getWeaponItem().getBodyPart() == L2Item.SLOT_LR_HAND) {
						if ((type == WeaponType.BOW) || (type == WeaponType.CROSSBOW)) {
							// P. Atk. increases by 6 for bows.
							// Starting at +4, P. Atk. bonus double.
							value += (6 * enchant) + (12 * overEnchant);
						} else {
							// P. Atk. increases by 4 for two-handed swords, two-handed blunts, dualswords, and two-handed combat weapons.
							// Starting at +4, P. Atk. bonus double.
							value += (4 * enchant) + (8 * overEnchant);
						}
					} else {
						// P. Atk. increases by 3 for one-handed swords, one-handed blunts, daggers, spears, and other weapons.
						// Starting at +4, P. Atk. bonus double.
						value += (3 * enchant) + (6 * overEnchant);
					}
					break;
				case D:
				case NONE:
					// P. Atk. increases by 2 for all weapons with the exception of bows.
					// Starting at +4, P. Atk. bonus double.
					switch (type) {
						case BOW, CROSSBOW -> {
							// Bows increase by 4.
							// Starting at +4, P. Atk. bonus double.
							value += (4 * enchant) + (8 * overEnchant);
						}
						default -> value += (2 * enchant) + (4 * overEnchant);
					}
					break;
			}
		}
		return value;
	}
}
