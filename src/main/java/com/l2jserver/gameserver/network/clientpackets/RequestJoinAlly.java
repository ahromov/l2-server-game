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

import static com.l2jserver.gameserver.network.SystemMessageId.S2_ALLIANCE_LEADER_OF_S1_REQUESTED_ALLIANCE;
import static com.l2jserver.gameserver.network.SystemMessageId.YOU_ARE_NOT_A_CLAN_MEMBER;
import static com.l2jserver.gameserver.network.SystemMessageId.YOU_HAVE_INVITED_THE_WRONG_TARGET;

import com.l2jserver.gameserver.model.L2World;
import com.l2jserver.gameserver.network.serverpackets.AskJoinAlly;
import com.l2jserver.gameserver.network.serverpackets.SystemMessage;

/**
 * @since 2005/03/27 15:29:30
 */
public final class RequestJoinAlly extends L2GameClientPacket {
	private static final String _C__8C_REQUESTJOINALLY = "[C] 8C RequestJoinAlly";
	
	private int _id;
	
	@Override
	protected void readImpl() {
		_id = readD();
	}
	
	@Override
	protected void runImpl() {
		final var player = getClient().getActiveChar();
		if (player == null) {
			return;
		}
		
		final var targetPlayer = L2World.getInstance().getPlayer(_id);
		if (targetPlayer == null) {
			player.sendPacket(YOU_HAVE_INVITED_THE_WRONG_TARGET);
			return;
		}
		
		final var clan = player.getClan();
		if (clan == null) {
			player.sendPacket(YOU_ARE_NOT_A_CLAN_MEMBER);
			return;
		}
		
		if (!clan.checkAllyJoinCondition(player, targetPlayer)) {
			return;
		}
		
		if (!player.getRequest().setRequest(targetPlayer, this)) {
			return;
		}
		
		final var sm = SystemMessage.getSystemMessage(S2_ALLIANCE_LEADER_OF_S1_REQUESTED_ALLIANCE);
		sm.addString(player.getClan().getAllyName());
		sm.addString(player.getName());
		targetPlayer.sendPacket(sm);
		targetPlayer.sendPacket(new AskJoinAlly(player.getObjectId(), player.getClan().getAllyName()));
	}
	
	@Override
	public String getType() {
		return _C__8C_REQUESTJOINALLY;
	}
}
