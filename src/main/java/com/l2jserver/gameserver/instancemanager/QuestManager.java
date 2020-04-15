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
package com.l2jserver.gameserver.instancemanager;

import static com.l2jserver.gameserver.config.Configuration.general;
import static com.l2jserver.gameserver.config.Configuration.server;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.l2jserver.commons.util.Util;
import com.l2jserver.gameserver.model.quest.Quest;
import com.l2jserver.gameserver.scripting.ScriptEngineManager;
import com.l2jserver.gameserver.scripting.ScriptManager;

/**
 * Quests and scripts manager.
 * @author Zoey76
 */
public final class QuestManager extends ScriptManager<Quest> {
	
	private static final Logger LOG = LoggerFactory.getLogger(QuestManager.class);
	
	/** Map containing all the quests. */
	private final Map<String, Quest> _quests = new ConcurrentHashMap<>();
	/** Map containing all the scripts. */
	private final Map<String, Quest> _scripts = new ConcurrentHashMap<>();
	
	protected QuestManager() {
		// Prevent initialization.
	}
	
	public boolean reload(String questFolder) {
		final Quest q = getQuest(questFolder);
		if (q == null) {
			return false;
		}
		return q.reload();
	}
	
	/**
	 * Reloads a the quest by ID.
	 * @param questId the ID of the quest to be reloaded
	 * @return {@code true} if reload was successful, {@code false} otherwise
	 */
	public boolean reload(int questId) {
		final Quest q = getQuest(questId);
		if (q == null) {
			return false;
		}
		return q.reload();
	}
	
	/**
	 * Reload all quests and scripts.<br>
	 * Unload all quests and scripts and load scripts.cfg.
	 */
	public void reloadAllScripts() {
		LOG.info("Reloading all server scripts.");
		
		// Unload quests.
		for (Quest quest : _quests.values()) {
			if (quest != null) {
				quest.unload(false);
			}
		}
		
		_quests.clear();
		
		// Unload scripts.
		for (Quest script : _scripts.values()) {
			if (script != null) {
				script.unload(false);
			}
		}
		_scripts.clear();
		
		try {
			ScriptEngineManager.getInstance().executeScriptList(new File(server().getDatapackRoot(), "data/scripts.cfg"));
		} catch (Exception ex) {
			LOG.warn("Failed loading scripts.cfg, no script going to be loaded!", ex);
		}
		
		report();
	}
	
	public void report() {
		LOG.info("Loaded {} quests.", _quests.size());
		LOG.info("Loaded {} scripts.", _scripts.size());
	}
	
	/**
	 * Calls {@link Quest#saveGlobalData()} in all quests and scripts.
	 */
	public void save() {
		// Save quests.
		for (Quest quest : _quests.values()) {
			quest.saveGlobalData();
		}
		
		// Save scripts.
		for (Quest script : _scripts.values()) {
			script.saveGlobalData();
		}
	}
	
	/**
	 * Gets a quest by name.<br>
	 * <i>For backwards compatibility, verifies scripts with the given name if the quest is not found.</i>
	 * @param name the quest name
	 * @return the quest
	 */
	public Quest getQuest(String name) {
		if (_quests.containsKey(name)) {
			return _quests.get(name);
		}
		return _scripts.get(name);
	}
	
	/**
	 * Gets a quest by ID.
	 * @param questId the ID of the quest to get
	 * @return if found, the quest, {@code null} otherwise
	 */
	public Quest getQuest(int questId) {
		for (Quest q : _quests.values()) {
			if (q.getId() == questId) {
				return q;
			}
		}
		return null;
	}
	
	/**
	 * Adds a new quest.
	 * @param quest the quest to be added
	 */
	public void addQuest(Quest quest) {
		if (quest == null) {
			throw new IllegalArgumentException("Quest argument cannot be null");
		}
		
		// FIXME: unloading the old quest at this point is a tad too late.
		// the new quest has already initialized itself and read the data, starting
		// an unpredictable number of tasks with that data. The old quest will now
		// save data which will never be read.
		// However, requesting the newQuest to re-read the data is not necessarily a
		// good option, since the newQuest may have already started timers, spawned NPCs
		// or taken any other action which it might re-take by re-reading the data.
		// the current solution properly closes the running tasks of the old quest but
		// ignores the data; perhaps the least of all evils...
		final Quest old = _quests.put(quest.getName(), quest);
		if (old != null) {
			old.unload();
			LOG.info("Replaced quest {} with a new version.", old);
			
		}
		
		if (general().showQuestsLoadInLogs()) {
			final String questName = quest.getName().contains("_") ? quest.getName().substring(quest.getName().indexOf('_') + 1) : quest.getName();
			LOG.info("Loaded quest {}.", Util.splitWords(questName));
		}
	}
	
	/**
	 * Removes a script.
	 * @param script the script to remove
	 * @return {@code true} if the script was removed, {@code false} otherwise
	 */
	public boolean removeScript(Quest script) {
		if (_quests.containsKey(script.getName())) {
			_quests.remove(script.getName());
			return true;
		} else if (_scripts.containsKey(script.getName())) {
			_scripts.remove(script.getName());
			return true;
		}
		return false;
	}
	
	public Map<String, Quest> getQuests() {
		return _quests;
	}
	
	@Override
	public boolean unload(Quest ms) {
		ms.saveGlobalData();
		return removeScript(ms);
	}
	
	@Override
	public String getScriptManagerName() {
		return getClass().getSimpleName();
	}
	
	/**
	 * Gets all the registered scripts.
	 * @return all the scripts
	 */
	@Override
	public Map<String, Quest> getScripts() {
		return _scripts;
	}
	
	/**
	 * Adds a script.
	 * @param script the script to be added
	 */
	public void addScript(Quest script) {
		final Quest old = _scripts.put(script.getClass().getSimpleName(), script);
		if (old != null) {
			old.unload();
			LOG.info("Replaced script {} with a new version.", old);
		}
		
		if (general().showQuestsLoadInLogs()) {
			LOG.info("Loaded script {}.", Util.splitWords(script.getClass().getSimpleName()));
		}
	}
	
	public static QuestManager getInstance() {
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder {
		protected static final QuestManager INSTANCE = new QuestManager();
	}
}
