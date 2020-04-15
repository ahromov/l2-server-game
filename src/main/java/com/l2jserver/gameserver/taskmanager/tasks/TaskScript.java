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

import static com.l2jserver.gameserver.config.Configuration.server;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.l2jserver.gameserver.scripting.ScriptEngineManager;
import com.l2jserver.gameserver.taskmanager.Task;
import com.l2jserver.gameserver.taskmanager.TaskManager.ExecutedTask;

/**
 * @author janiii
 */
public class TaskScript extends Task {
	
	private static final Logger LOG = LoggerFactory.getLogger(TaskScript.class);
	
	private static final String NAME = "script";
	
	@Override
	public String getName() {
		return NAME;
	}
	
	@Override
	public void onTimeElapsed(ExecutedTask task) {
		final File file = new File(server().getScriptRoot(), "com/l2jserver/datapack/cron/" + task.getParams()[2]);
		if (!file.isFile()) {
			LOG.warn("File not found {}!", task.getParams()[2]);
			return;
		}
		
		try {
			ScriptEngineManager.getInstance().compileScript(file);
		} catch (Exception ex) {
			LOG.warn("Failed loading {}!", task.getParams()[2]);
		}
	}
}
