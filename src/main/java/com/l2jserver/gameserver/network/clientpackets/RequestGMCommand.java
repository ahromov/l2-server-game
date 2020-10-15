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

import com.l2jserver.gameserver.data.sql.impl.ClanTable;
import com.l2jserver.gameserver.model.L2Clan;
import com.l2jserver.gameserver.model.L2World;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.network.serverpackets.GMHennaInfo;
import com.l2jserver.gameserver.network.serverpackets.GMViewCharacterInfo;
import com.l2jserver.gameserver.network.serverpackets.GMViewItemList;
import com.l2jserver.gameserver.network.serverpackets.GMViewPledgeInfo;
import com.l2jserver.gameserver.network.serverpackets.GMViewSkillInfo;
import com.l2jserver.gameserver.network.serverpackets.GMViewWarehouseWithdrawList;
import com.l2jserver.gameserver.network.serverpackets.GmViewQuestInfo;

/**
 * @since 2005/03/27 15:29:30
 */
public final class RequestGMCommand extends L2GameClientPacket {
	private static final String _C__7E_REQUESTGMCOMMAND = "[C] 7E RequestGMCommand";
	
	private String _targetName;
	private int _command;
	
	@Override
	protected void readImpl() {
		_targetName = readS();
		_command = readD();
		// _unknown = readD();
	}
	
	@Override
	protected void runImpl() {
		// prevent non gm or low level GMs from viewing player stuff
		if (!getClient().getActiveChar().isGM() || !getClient().getActiveChar().getAccessLevel().allowAltG()) {
			return;
		}
		
		L2PcInstance player = L2World.getInstance().getPlayer(_targetName);
		
		L2Clan clan = ClanTable.getInstance().getClanByName(_targetName);
		
		// player name was incorrect?
		if ((player == null) && ((clan == null) || (_command != 6))) {
			return;
		}
		
		switch (_command) {
			// player status
			case 1 -> {
				sendPacket(new GMViewCharacterInfo(player));
				sendPacket(new GMHennaInfo(player));
			}
			// player clan
			case 2 -> {
				if (player.getClan() != null) {
					sendPacket(new GMViewPledgeInfo(player.getClan(), player));
				}
			}
			// player skills
			case 3 -> sendPacket(new GMViewSkillInfo(player));
			// player quests
			case 4 -> sendPacket(new GmViewQuestInfo(player));
			// player inventory
			case 5 -> {
				sendPacket(new GMViewItemList(player));
				sendPacket(new GMHennaInfo(player));
			}
			// player warehouse
			case 6 -> {
				// gm warehouse view to be implemented
				if (player != null) {
					sendPacket(new GMViewWarehouseWithdrawList(player));
					// clan warehouse
				} else {
					sendPacket(new GMViewWarehouseWithdrawList(clan));
				}
			}
		}
	}
	
	@Override
	public String getType() {
		return _C__7E_REQUESTGMCOMMAND;
	}
}
