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
package com.l2jserver.gameserver.model;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.apache.logging.log4j.util.Strings;

import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;

/**
 * Party Match Waiting List.
 * @author Zoey76
 */
public class PartyMatchWaitingList {
	
	private final Set<L2PcInstance> _members = ConcurrentHashMap.newKeySet(1);
	
	protected PartyMatchWaitingList() {
		// Do nothing.
	}
	
	public void addPlayer(L2PcInstance player) {
		_members.add(player);
	}
	
	public void removePlayer(L2PcInstance player) {
		_members.remove(player);
	}
	
	public Set<L2PcInstance> getPlayers() {
		return _members;
	}
	
	public List<L2PcInstance> findPlayers(int minLevel, int maxLevel, Set<Integer> classes, String filter) {
		return _members.stream() //
			.filter(p -> p.getLevel() >= minLevel) //
			.filter(p -> p.getLevel() <= maxLevel) //
			.filter(p -> classes.isEmpty() || classes.contains(p.getClassId().getId())) //
			.filter(p -> Strings.isBlank(filter) || p.getName().toLowerCase().contains(filter.toLowerCase())) //
			.filter(L2PcInstance::isPartyWaiting) //
			.collect(Collectors.toList());
	}
	
	public static PartyMatchWaitingList getInstance() {
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder {
		protected static final PartyMatchWaitingList INSTANCE = new PartyMatchWaitingList();
	}
}