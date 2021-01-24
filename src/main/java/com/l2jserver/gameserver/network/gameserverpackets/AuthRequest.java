/*
 * Copyright © 2004-2021 L2J Server
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
package com.l2jserver.gameserver.network.gameserverpackets;

import java.util.List;

import com.l2jserver.commons.network.BaseSendablePacket;

/**
 * Auth Request packet.
 * 
 * <pre>
 * Format: CCCCHDDBD[S]
 * C protocol
 * C desired Id
 * C accept alternative Id
 * C reserve Host
 * H port
 * D max players
 * D hex Id size
 * B hex Id
 * D subnet size
 * [S] subnets
 * </pre>
 * 
 * @author Zoey76
 */
public class AuthRequest extends BaseSendablePacket {
	
	private static final int VERSION = 14;
	
	public AuthRequest(int id, boolean acceptAlternate, byte[] hexid, int port, boolean reserveHost, int maxplayer, List<String> subnets, List<String> hosts) {
		writeC(0x01);
		writeC(VERSION);
		writeC(id);
		writeC(acceptAlternate ? 0x01 : 0x00);
		writeC(reserveHost ? 0x01 : 0x00);
		writeH(port);
		writeD(maxplayer);
		writeD(hexid.length);
		writeB(hexid);
		writeD(subnets.size());
		for (int i = 0; i < subnets.size(); i++) {
			writeS(subnets.get(i));
			writeS(hosts.get(i));
		}
	}
	
	@Override
	public byte[] getContent() {
		return getBytes();
	}
}