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
package com.l2jserver.gameserver.custom;

import static com.l2jserver.gameserver.config.Configuration.customs;

import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;

/**
 * Auto Loot Extension.
 * @author Maneco2
 * @version 2.6.2.0
 */
public class AutoLootExtension {
	public static void onLogin(L2PcInstance player) {
		if (customs().autoLootVoiceRestore()) {
			if (player.getVariables().hasVariable("AutoLoot")) {
				player.setAutoLoot(true);
			}
		} else if (!customs().autoLootVoiceRestore()) {
			if (player.getVariables().hasVariable("AutoLoot")) {
				player.getVariables().remove("AutoLoot");
			}
		}
		
		if (customs().autoLootItemsVoiceRestore()) {
			if (player.getVariables().hasVariable("AutoLootItems")) {
				player.setAutoLootItem(true);
			}
		} else if (!customs().autoLootItemsVoiceRestore()) {
			if (player.getVariables().hasVariable("AutoLootItems")) {
				player.getVariables().remove("AutoLootItems");
			}
		}
		
		if (customs().autoLootHerbsVoiceRestore()) {
			if (player.getVariables().hasVariable("AutoLootHerbs")) {
				player.setAutoLootHerbs(true);
			}
		} else if (!customs().autoLootHerbsVoiceRestore()) {
			if (player.getVariables().hasVariable("AutoLootHerbs")) {
				player.getVariables().remove("AutoLootHerbs");
			}
		}
	}
}