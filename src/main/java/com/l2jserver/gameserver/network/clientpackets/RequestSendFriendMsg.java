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

import static com.l2jserver.gameserver.config.Configuration.general;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.l2jserver.gameserver.model.L2World;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.network.SystemMessageId;
import com.l2jserver.gameserver.network.serverpackets.L2FriendSay;

/**
 * Recieve Private (Friend) Message - 0xCC Format: c SS S: Message S: Receiving Player
 * @author Tempy
 */
public final class RequestSendFriendMsg extends L2GameClientPacket {
	
	private static final String _C__6B_REQUESTSENDMSG = "[C] 6B RequestSendFriendMsg";
	
	private static final Logger LOG_CHAT = LoggerFactory.getLogger("chat");
	
	private String _message;
	private String _reciever;
	
	@Override
	protected void readImpl() {
		_message = readS();
		_reciever = readS();
	}
	
	@Override
	protected void runImpl() {
		final L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null) {
			return;
		}
		
		if ((_message == null) || _message.isEmpty() || (_message.length() > 300)) {
			return;
		}
		
		final L2PcInstance targetPlayer = L2World.getInstance().getPlayer(_reciever);
		if ((targetPlayer == null) || !targetPlayer.isFriend(activeChar.getObjectId())) {
			activeChar.sendPacket(SystemMessageId.TARGET_IS_NOT_FOUND_IN_THE_GAME);
			return;
		}
		
		if (general().logChat()) {
			LOG_CHAT.info("PRIV_MSG {} says [{}] to {}.", activeChar.getName(), _message, _reciever);
		}
		
		targetPlayer.sendPacket(new L2FriendSay(activeChar.getName(), _reciever, _message));
	}
	
	@Override
	public String getType() {
		return _C__6B_REQUESTSENDMSG;
	}
}