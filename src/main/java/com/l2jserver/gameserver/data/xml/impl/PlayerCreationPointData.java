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
package com.l2jserver.gameserver.data.xml.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.l2jserver.commons.util.Rnd;
import com.l2jserver.gameserver.model.Location;
import com.l2jserver.gameserver.model.base.ClassId;
import com.l2jserver.gameserver.util.IXmlReader;

/**
 * @author Michael
 */
public class PlayerCreationPointData implements IXmlReader {
	
	private static final Logger LOG = LoggerFactory.getLogger(PlayerCreationPointData.class);
	
	private final Map<ClassId, Location[]> _creationPointData = new HashMap<>();
	
	protected PlayerCreationPointData() {
		load();
	}
	
	@Override
	public void load() {
		_creationPointData.clear();
		parseDatapackFile("data/stats/chars/pcCreationPoints.xml");
		LOG.info("Loaded {} character creation points.", _creationPointData.values().stream().mapToInt(array -> array.length).sum());
	}
	
	/**
	 * @return random Location of created character spawn.
	 */
	public Location getCreationPoint(ClassId classId) {
		return Rnd.randomElement(_creationPointData.get(classId));
	}
	
	@Override
	public void parseDocument(Document doc) {
		NamedNodeMap attrs;
		for (Node n = doc.getFirstChild(); n != null; n = n.getNextSibling()) {
			if ("list".equalsIgnoreCase(n.getNodeName())) {
				for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling()) {
					if ("startpoints".equalsIgnoreCase(d.getNodeName())) {
						List<Location> creationPoints = new ArrayList<>();
						for (Node c = d.getFirstChild(); c != null; c = c.getNextSibling()) {
							if ("spawn".equalsIgnoreCase(c.getNodeName())) {
								attrs = c.getAttributes();
								creationPoints.add(new Location(parseInteger(attrs, "x"), parseInteger(attrs, "y"), parseInteger(attrs, "z")));
							} else if ("classid".equalsIgnoreCase(c.getNodeName())) {
								_creationPointData.put(ClassId.getClassId(Integer.parseInt(c.getTextContent())), creationPoints.toArray(new Location[0]));
							}
						}
					}
				}
			}
		}
	}
	
	public static final PlayerCreationPointData getInstance() {
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder {
		protected static final PlayerCreationPointData INSTANCE = new PlayerCreationPointData();
	}
}
