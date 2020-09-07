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
package com.l2jserver.gameserver.network.clientpackets;

import java.util.HashSet;
import java.util.Set;

import com.l2jserver.gameserver.network.serverpackets.ExListPartyMatchingWaitingRoom;

/**
 * RequestListPartyMatchingWaitingRoom client packet.
 * @author Zoey76
 */
public class RequestListPartyMatchingWaitingRoom extends L2GameClientPacket {
	
	private static final String _C__D0_31_REQUESTLISTPARTYMATCHINGWAITINGROOM = "[C] D0:31 RequestListPartyMatchingWaitingRoom";
	
	private int page;
	
	private int minLevel;
	
	private int maxLevel;
	
	private Set<Integer> classes;
	
	private String filter;
	
	@Override
	protected void readImpl() {
		page = readD();
		minLevel = readD();
		maxLevel = readD();
		final var size = readD();
		classes = new HashSet<>(size);
		for (int i = 0; i < size; i++) {
			classes.add(readD());
		}
		filter = hasRemaining() ? readS() : "";
	}
	
	@Override
	protected void runImpl() {
		final var player = getClient().getActiveChar();
		if (player == null) {
			return;
		}
		player.sendPacket(new ExListPartyMatchingWaitingRoom(page, minLevel, maxLevel, classes, filter));
	}
	
	@Override
	public String getType() {
		return _C__D0_31_REQUESTLISTPARTYMATCHINGWAITINGROOM;
	}
}