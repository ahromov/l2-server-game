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
package com.l2jserver.gameserver.network.clientpackets;

import static com.l2jserver.gameserver.config.Configuration.character;
import static com.l2jserver.gameserver.network.SystemMessageId.A_USER_CURRENTLY_PARTICIPATING_IN_THE_OLYMPIAD_CANNOT_SEND_PARTY_AND_FRIEND_INVITATIONS;
import static com.l2jserver.gameserver.network.SystemMessageId.BLOCKED_C1;
import static com.l2jserver.gameserver.network.SystemMessageId.C1_IS_BUSY_TRY_LATER;
import static com.l2jserver.gameserver.network.SystemMessageId.S1_ALREADY_IN_FRIENDS_LIST;
import static com.l2jserver.gameserver.network.SystemMessageId.THE_FRIENDS_LIST_OF_THE_PERSON_YOU_ARE_TRYING_TO_ADD_IS_FULL_SO_REGISTRATION_IS_NOT_POSSIBLE;
import static com.l2jserver.gameserver.network.SystemMessageId.THE_USER_YOU_REQUESTED_IS_NOT_IN_GAME;
import static com.l2jserver.gameserver.network.SystemMessageId.YOU_CANNOT_ADD_YOURSELF_TO_OWN_FRIEND_LIST;
import static com.l2jserver.gameserver.network.SystemMessageId.YOU_CAN_ONLY_ENTER_UP_128_NAMES_IN_YOUR_FRIENDS_LIST;
import static com.l2jserver.gameserver.network.SystemMessageId.YOU_REQUESTED_C1_TO_BE_FRIEND;

import com.l2jserver.gameserver.model.BlockList;
import com.l2jserver.gameserver.model.L2World;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.network.serverpackets.FriendAddRequest;
import com.l2jserver.gameserver.network.serverpackets.SystemMessage;

public final class RequestFriendInvite extends L2GameClientPacket {
	private static final String _C__77_REQUESTFRIENDINVITE = "[C] 77 RequestFriendInvite";
	
	private String _name;
	
	@Override
	protected void readImpl() {
		_name = readS();
	}
	
	@Override
	protected void runImpl() {
		final L2PcInstance activeChar = getActiveChar();
		if (activeChar == null) {
			return;
		}
		
		final L2PcInstance friend = L2World.getInstance().getPlayer(_name);
		
		// Target is not found in the game.
		if ((friend == null) || !friend.isOnline() || friend.isInvisible()) {
			activeChar.sendPacket(THE_USER_YOU_REQUESTED_IS_NOT_IN_GAME);
			return;
		}
		
		// You cannot add yourself to your own friend list.
		if (friend == activeChar) {
			activeChar.sendPacket(YOU_CANNOT_ADD_YOURSELF_TO_OWN_FRIEND_LIST);
			return;
		}
		
		if (activeChar.getFriends().size() >= character().getFriendListLimit()) {
			activeChar.sendPacket(YOU_CAN_ONLY_ENTER_UP_128_NAMES_IN_YOUR_FRIENDS_LIST);
			return;
		}
		
		if (friend.getFriends().size() >= character().getFriendListLimit()) {
			activeChar.sendPacket(THE_FRIENDS_LIST_OF_THE_PERSON_YOU_ARE_TRYING_TO_ADD_IS_FULL_SO_REGISTRATION_IS_NOT_POSSIBLE);
			return;
		}
		
		// Target is in olympiad.
		if (activeChar.isInOlympiadMode() || friend.isInOlympiadMode()) {
			activeChar.sendPacket(A_USER_CURRENTLY_PARTICIPATING_IN_THE_OLYMPIAD_CANNOT_SEND_PARTY_AND_FRIEND_INVITATIONS);
			return;
		}
		
		// Target blocked active player.
		if (BlockList.isBlocked(friend, activeChar)) {
			activeChar.sendMessage("You are in target's block list.");
			return;
		}
		
		// Target is blocked.
		if (BlockList.isBlocked(activeChar, friend)) {
			final var sm = SystemMessage.getSystemMessage(BLOCKED_C1);
			sm.addCharName(friend);
			activeChar.sendPacket(sm);
			return;
		}
		
		// Target already in friend list.
		if (activeChar.isFriend(friend.getObjectId())) {
			final var sm = SystemMessage.getSystemMessage(S1_ALREADY_IN_FRIENDS_LIST);
			sm.addString(_name);
			activeChar.sendPacket(sm);
			return;
		}
		
		// Target is busy.
		if (friend.isProcessingRequest()) {
			final var sm = SystemMessage.getSystemMessage(C1_IS_BUSY_TRY_LATER);
			sm.addString(_name);
			activeChar.sendPacket(sm);
			return;
		}
		
		// Friend request sent.
		activeChar.onTransactionRequest(friend);
		friend.sendPacket(new FriendAddRequest(activeChar.getName()));
		final var sm = SystemMessage.getSystemMessage(YOU_REQUESTED_C1_TO_BE_FRIEND);
		sm.addString(_name);
		activeChar.sendPacket(sm);
	}
	
	@Override
	public String getType() {
		return _C__77_REQUESTFRIENDINVITE;
	}
}