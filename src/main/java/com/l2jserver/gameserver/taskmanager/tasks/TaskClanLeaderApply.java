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

import static com.l2jserver.gameserver.config.Configuration.character;
import static com.l2jserver.gameserver.taskmanager.TaskTypes.TYPE_GLOBAL_TASK;
import static java.util.Calendar.DAY_OF_WEEK;

import java.util.Calendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.l2jserver.gameserver.data.sql.impl.ClanTable;
import com.l2jserver.gameserver.model.L2Clan;
import com.l2jserver.gameserver.model.L2ClanMember;
import com.l2jserver.gameserver.taskmanager.Task;
import com.l2jserver.gameserver.taskmanager.TaskManager;
import com.l2jserver.gameserver.taskmanager.TaskManager.ExecutedTask;

/**
 * @author UnAfraid
 */
public class TaskClanLeaderApply extends Task {
	
	private static final Logger LOG = LoggerFactory.getLogger(TaskClanLeaderApply.class);
	
	private static final String NAME = "clanleaderapply";
	
	@Override
	public String getName() {
		return NAME;
	}
	
	@Override
	public void onTimeElapsed(ExecutedTask task) {
		Calendar cal = Calendar.getInstance();
		if (cal.get(DAY_OF_WEEK) == character().getClanLeaderDateChange()) {
			for (L2Clan clan : ClanTable.getInstance().getClans()) {
				if (clan.getNewLeaderId() != 0) {
					final L2ClanMember member = clan.getClanMember(clan.getNewLeaderId());
					if (member == null) {
						continue;
					}
					
					clan.setNewLeader(member);
				}
			}
			LOG.info("Task launched.");
		}
	}
	
	@Override
	public void initializate() {
		TaskManager.addUniqueTask(NAME, TYPE_GLOBAL_TASK, "1", character().getClanLeaderHourChange(), "");
	}
}
