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
						int reputation = 0;
						switch (entry.getValue()) {
							case 1:
								reputation = clan().get1stRaidRankingPoints();
								break;
							case 2:
								reputation = clan().get2ndRaidRankingPoints();
								break;
							case 3:
								reputation = clan().get3rdRaidRankingPoints();
								break;
							case 4:
								reputation = clan().get4thRaidRankingPoints();
								break;
							case 5:
								reputation = clan().get5thRaidRankingPoints();
								break;
							case 6:
								reputation = clan().get6thRaidRankingPoints();
								break;
							case 7:
								reputation = clan().get7thRaidRankingPoints();
								break;
							case 8:
								reputation = clan().get8thRaidRankingPoints();
								break;
							case 9:
								reputation = clan().get9thRaidRankingPoints();
								break;
							case 10:
								reputation = clan().get10thRaidRankingPoints();
								break;
							default:
								if (entry.getValue() <= 50) {
									reputation = clan().getUpTo50thRaidRankingPoints();
								} else {
									reputation = clan().getUpTo100thRaidRankingPoints();
								}
								break;
						}
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
