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
package com.l2jserver.gameserver.engines;

import static com.l2jserver.gameserver.config.Configuration.general;
import static com.l2jserver.gameserver.config.Configuration.server;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.l2jserver.gameserver.datatables.SkillData;
import com.l2jserver.gameserver.engines.items.DocumentItem;
import com.l2jserver.gameserver.engines.skills.DocumentSkill;
import com.l2jserver.gameserver.model.items.L2Item;
import com.l2jserver.gameserver.model.skills.Skill;
import com.l2jserver.gameserver.util.file.filter.XMLFilter;

/**
 * Document Engine.
 * @author mkizub
 */
public class DocumentEngine {
	
	private static final Logger LOG = LoggerFactory.getLogger(DocumentEngine.class);
	
	private final List<File> _itemFiles = new ArrayList<>();
	
	private final List<File> _skillFiles = new ArrayList<>();
	
	public static DocumentEngine getInstance() {
		return SingletonHolder._instance;
	}
	
	protected DocumentEngine() {
		hashFiles("data/stats/items", _itemFiles);
		if (general().customItemsLoad()) {
			hashFiles("data/stats/items/custom", _itemFiles);
		}
		hashFiles("data/stats/skills", _skillFiles);
		if (general().customSkillsLoad()) {
			hashFiles("data/stats/skills/custom", _skillFiles);
		}
	}
	
	private void hashFiles(String dirname, List<File> hash) {
		final var dir = new File(server().getDatapackRoot(), dirname);
		if (!dir.exists()) {
			LOG.warn("Directory {} does not exists!", dir.getAbsolutePath());
			return;
		}
		
		final var files = dir.listFiles(new XMLFilter());
		if (files != null) {
			for (File f : files) {
				hash.add(f);
			}
		}
	}
	
	public List<Skill> loadSkills(File file) {
		if (file == null) {
			LOG.warn("Skill file not found!");
			return null;
		}
		DocumentSkill doc = new DocumentSkill(file);
		doc.parse();
		return doc.getSkills();
	}
	
	public void loadAllSkills(final Map<Integer, Skill> allSkills) {
		int count = 0;
		for (File file : _skillFiles) {
			List<Skill> s = loadSkills(file);
			if (s == null) {
				continue;
			}
			for (Skill skill : s) {
				allSkills.put(SkillData.getSkillHashCode(skill), skill);
				count++;
			}
		}
		LOG.info("Loaded {} skill templates from XML files.", count);
	}
	
	/**
	 * Return created items
	 * @return List of {@link L2Item}
	 */
	public List<L2Item> loadItems() {
		List<L2Item> list = new ArrayList<>();
		for (File f : _itemFiles) {
			DocumentItem document = new DocumentItem(f);
			document.parse();
			list.addAll(document.getItemList());
		}
		return list;
	}
	
	private static class SingletonHolder {
		protected static final DocumentEngine _instance = new DocumentEngine();
	}
}
