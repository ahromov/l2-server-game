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
package com.l2jserver.gameserver.data.json;

import static com.l2jserver.gameserver.config.Configuration.server;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

/**
 * @author Zealar
 * @since 2.6.0.0
 */
public final class ExperienceData {
	private static final Logger LOG = LoggerFactory.getLogger(ExperienceData.class);
	
	private static final Gson GSON = new Gson();
	private static final Type TYPE_MAP_INTEGER_LONG = new TypeToken<Map<Integer, Long>>() {
	}.getType();
	private final Map<Integer, Long> _expTable = new HashMap<>();
	
	ExperienceData() {
		load();
	}
	
	public void load() {
		_expTable.clear();
		try (JsonReader reader = new JsonReader(new FileReader(new File(server().getDatapackRoot(), "data/stats/expData.json")))) {
			_expTable.putAll(GSON.fromJson(reader, TYPE_MAP_INTEGER_LONG));
		} catch (FileNotFoundException fnfe) {
			LOG.warn("data/stats/expData.json not found!");
		} catch (IOException ioe) {
			LOG.warn("Failed to load expData.json for: ", ioe);
		}
	}
	
	/**
	 * Gets the exp for level.
	 * @param level the level required.
	 * @return the experience points required to reach the given level.
	 */
	public long getExpForLevel(int level) {
		return _expTable.get(level);
	}
	
	public float getPercentFromCurrentLevel(long exp, int level) {
		long expPerLevel = getExpForLevel(level);
		long expPerLevel2 = getExpForLevel(level + 1);
		return (float) (exp - expPerLevel) / (expPerLevel2 - expPerLevel);
	}
	
	/**
	 * Gets the single instance of ExperienceTable.
	 * @return single instance of ExperienceTable
	 */
	public static ExperienceData getInstance() {
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder {
		static final ExperienceData _instance = new ExperienceData();
	}
}
