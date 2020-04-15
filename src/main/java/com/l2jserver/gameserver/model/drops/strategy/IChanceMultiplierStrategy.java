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
package com.l2jserver.gameserver.model.drops.strategy;

import static com.l2jserver.gameserver.config.Configuration.customs;
import static com.l2jserver.gameserver.config.Configuration.rates;

import com.l2jserver.gameserver.datatables.ItemTable;
import com.l2jserver.gameserver.model.actor.L2Character;
import com.l2jserver.gameserver.model.drops.GeneralDropItem;
import com.l2jserver.gameserver.model.itemcontainer.Inventory;

/**
 * @author Battlecruiser
 */
public interface IChanceMultiplierStrategy {
	public static final IChanceMultiplierStrategy DROP = DEFAULT_STRATEGY(rates().getDeathDropChanceMultiplier());
	public static final IChanceMultiplierStrategy SPOIL = DEFAULT_STRATEGY(rates().getCorpseDropChanceMultiplier());
	public static final IChanceMultiplierStrategy STATIC = (item, victim) -> 1;
	
	public static final IChanceMultiplierStrategy QUEST = (item, victim) -> {
		double championmult;
		if ((item.getItemId() == Inventory.ADENA_ID) || (item.getItemId() == Inventory.ANCIENT_ADENA_ID)) {
			championmult = customs().getChampionAdenasRewardsChance();
		} else {
			championmult = customs().getChampionRewardsChance();
		}
		
		return (customs().championEnable() && (victim != null) && victim.isChampion()) ? (rates().getRateQuestDrop() * championmult) : rates().getRateQuestDrop();
	};
	
	public static IChanceMultiplierStrategy DEFAULT_STRATEGY(final double defaultMultiplier) {
		return (item, victim) -> {
			float multiplier = 1;
			if (victim.isChampion()) {
				multiplier *= item.getItemId() != Inventory.ADENA_ID ? customs().getChampionRewardsChance() : customs().getChampionAdenasRewardsChance();
			}
			Float dropChanceMultiplier = rates().getDropChanceMultiplierByItemId().get(item.getItemId());
			if (dropChanceMultiplier != null) {
				multiplier *= dropChanceMultiplier;
			} else if (ItemTable.getInstance().getTemplate(item.getItemId()).hasExImmediateEffect()) {
				multiplier *= rates().getHerbDropChanceMultiplier();
			} else if (victim.isRaid()) {
				multiplier *= rates().getRaidDropChanceMultiplier();
			} else {
				multiplier *= defaultMultiplier;
			}
			return multiplier;
		};
	}
	
	public double getChanceMultiplier(GeneralDropItem item, L2Character victim);
}
