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
package com.l2jserver.gameserver.taskmanager.tasks;

import static com.l2jserver.gameserver.config.Configuration.clan;

import java.util.Calendar;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.l2jserver.gameserver.data.sql.impl.ClanTable;
import com.l2jserver.gameserver.instancemanager.RaidBossPointsManager;
import com.l2jserver.gameserver.model.L2Clan;
import com.l2jserver.gameserver.taskmanager.Task;
import com.l2jserver.gameserver.taskmanager.TaskManager;
import com.l2jserver.gameserver.taskmanager.TaskManager.ExecutedTask;
import com.l2jserver.gameserver.taskmanager.TaskTypes;

public class TaskRaidPointsReset extends Task {
	
	private static final Logger LOG = LoggerFactory.getLogger(TaskRaidPointsReset.class);
	
	public static final String NAME = "raid_points_reset";
	
	@Override
	public String getName() {
		return NAME;
	}
	
	@Override
	public void onTimeElapsed(ExecutedTask task) {
		Calendar cal = Calendar.getInstance();
		if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY) {
			// reward clan reputation points
			Map<Integer, Integer> rankList = RaidBossPointsManager.getInstance().getRankList();
			for (L2Clan c : ClanTable.getInstance().getClans()) {
				for (Entry<Integer, Integer> entry : rankList.entrySet()) {
					if ((entry.getValue() <= 100) && c.isMember(entry.getKey())) {
						int reputation = switch (entry.getValue()) {
							case 1 -> clan().get1stRaidRankingPoints();
							case 2 -> clan().get2ndRaidRankingPoints();
							case 3 -> clan().get3rdRaidRankingPoints();
							case 4 -> clan().get4thRaidRankingPoints();
							case 5 -> clan().get5thRaidRankingPoints();
							case 6 -> clan().get6thRaidRankingPoints();
							case 7 -> clan().get7thRaidRankingPoints();
							case 8 -> clan().get8thRaidRankingPoints();
							case 9 -> clan().get9thRaidRankingPoints();
							case 10 -> clan().get10thRaidRankingPoints();
							default -> entry.getValue() <= 50 ? clan().getUpTo50thRaidRankingPoints() : clan().getUpTo100thRaidRankingPoints();
						};
						c.addReputationScore(reputation, true);
					}
				}
			}
			
			RaidBossPointsManager.getInstance().cleanUp();
			LOG.info("Raid points reset global task launched.");
		}
	}
	
	@Override
	public void initializate() {
		TaskManager.addUniqueTask(NAME, TaskTypes.TYPE_GLOBAL_TASK, "1", "00:10:00", "");
	}
}
