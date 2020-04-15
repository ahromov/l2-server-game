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
package com.l2jserver.gameserver.network.serverpackets;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.l2jserver.gameserver.data.sql.impl.CharNameTable;
import com.l2jserver.gameserver.model.L2World;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;

/**
 * Support for "Chat with Friends" dialog. <br />
 * This packet is sent only at login.
 * @author Tempy
 */
public class FriendList extends L2GameServerPacket {
	private List<FriendInfo> _info;
	
	private static class FriendInfo {
		int _objId;
		String _name;
		boolean _online;
		
		public FriendInfo(int objId, String name, boolean online) {
			_objId = objId;
			_name = name;
			_online = online;
		}
	}
	
	public FriendList(L2PcInstance player) {
		if (!player.hasFriends()) {
			_info = Collections.emptyList();
			return;
		}
		
		_info = new ArrayList<>(player.getFriends().size());
		for (int objId : player.getFriends()) {
			final String name = CharNameTable.getInstance().getNameById(objId);
			final L2PcInstance friend = L2World.getInstance().getPlayer(objId);
			final boolean online = (friend != null) && friend.isOnline();
			_info.add(new FriendInfo(objId, name, online));
		}
	}
	
	@Override
	protected final void writeImpl() {
		writeC(0x75);
		writeD(_info.size());
		for (FriendInfo info : _info) {
			writeD(info._objId); // character id
			writeS(info._name);
			writeD(info._online ? 0x01 : 0x00); // online
			writeD(info._online ? info._objId : 0x00); // object id if online
		}
	}
}
