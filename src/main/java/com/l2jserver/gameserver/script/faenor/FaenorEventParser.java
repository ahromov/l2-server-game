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
package com.l2jserver.gameserver.script.faenor;

import java.util.Date;

import javax.script.ScriptContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

import com.l2jserver.gameserver.ThreadPoolManager;
import com.l2jserver.gameserver.script.DateRange;
import com.l2jserver.gameserver.script.IntList;
import com.l2jserver.gameserver.script.Parser;
import com.l2jserver.gameserver.script.ParserFactory;
import com.l2jserver.gameserver.script.ScriptEngine;

/**
 * @author Luis Arias
 */
public class FaenorEventParser extends FaenorParser {
	
	private static final Logger LOG = LoggerFactory.getLogger(FaenorEventParser.class);
	
	private DateRange _eventDates = null;
	
	@Override
	public void parseScript(final Node eventNode, ScriptContext context) {
		String id = attribute(eventNode, "ID");
		_eventDates = DateRange.parse(attribute(eventNode, "Active"), DATE_FORMAT);
		
		Date currentDate = new Date();
		if (_eventDates.getEndDate().before(currentDate)) {
			LOG.info("Event Id {} has passed... Ignored.", id);
			return;
		}
		
		if (_eventDates.getStartDate().after(currentDate)) {
			LOG.info("Event Id {} is not active yet... Ignored.", id);
			ThreadPoolManager.getInstance().scheduleGeneral(() -> parseEventDropAndMessage(eventNode), _eventDates.getStartDate().getTime() - currentDate.getTime());
			return;
		}
		
		parseEventDropAndMessage(eventNode);
	}
	
	protected void parseEventDropAndMessage(Node eventNode) {
		for (Node node = eventNode.getFirstChild(); node != null; node = node.getNextSibling()) {
			if (isNodeName(node, "DropList")) {
				parseEventDropList(node);
			} else if (isNodeName(node, "Message")) {
				parseEventMessage(node);
			}
		}
	}
	
	private void parseEventMessage(Node sysMsg) {
		try {
			String type = attribute(sysMsg, "Type");
			String message = attribute(sysMsg, "Msg");
			
			if (type.equalsIgnoreCase("OnJoin")) {
				_bridge.onPlayerLogin(message, _eventDates);
			}
		} catch (Exception ex) {
			LOG.warn("There has been an error in event parser!", ex);
		}
	}
	
	private void parseEventDropList(Node dropList) {
		for (Node node = dropList.getFirstChild(); node != null; node = node.getNextSibling()) {
			if (isNodeName(node, "AllDrop")) {
				parseEventDrop(node);
			}
		}
	}
	
	private void parseEventDrop(Node drop) {
		try {
			int[] items = IntList.parse(attribute(drop, "Items"));
			int[] count = IntList.parse(attribute(drop, "Count"));
			double chance = getPercent(attribute(drop, "Chance"));
			
			_bridge.addEventDrop(items, count, chance, _eventDates);
		} catch (Exception ex) {
			LOG.warn("There has been an error parsing drops!", ex);
		}
	}
	
	static class FaenorEventParserFactory extends ParserFactory {
		@Override
		public Parser create() {
			return (new FaenorEventParser());
		}
	}
	
	static {
		ScriptEngine.parserFactories.put(getParserName("Event"), new FaenorEventParserFactory());
	}
}
