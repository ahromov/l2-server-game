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

import com.l2jserver.gameserver.data.sql.impl.ClanTable;
import com.l2jserver.gameserver.model.L2Clan;
import com.l2jserver.gameserver.model.L2SiegeClan;
import com.l2jserver.gameserver.model.entity.Castle;

/**
 * Populates the Siege Defender List in the SiegeInfo Window<BR>
 * <BR>
 * c = 0xcb<BR>
 * d = CastleID<BR>
 * d = unknown (0x00)<BR>
 * d = unknown (0x01)<BR>
 * d = unknown (0x00)<BR>
 * d = Number of Defending Clans?<BR>
 * d = Number of Defending Clans<BR>
 * { //repeats<BR>
 * d = ClanID<BR>
 * S = ClanName<BR>
 * S = ClanLeaderName<BR>
 * d = ClanCrestID<BR>
 * d = signed time (seconds)<BR>
 * d = Type -> Owner = 0x01 || Waiting = 0x02 || Accepted = 0x03<BR>
 * d = AllyID<BR>
 * S = AllyName<BR>
 * S = AllyLeaderName<BR>
 * d = AllyCrestID<BR>
 * @author KenM
 */
public final class SiegeDefenderList extends L2GameServerPacket {
	private final Castle _castle;
	
	public SiegeDefenderList(Castle castle) {
		_castle = castle;
	}
	
	@Override
	protected void writeImpl() {
		writeC(0xcb);
		writeD(_castle.getResidenceId());
		writeD(0x00); // 0
		writeD(0x01); // 1
		writeD(0x00); // 0
		int size = _castle.getSiege().getDefenderClans().size() + _castle.getSiege().getDefenderWaitingClans().size();
		if (size > 0) {
			L2Clan clan;
			
			writeD(size);
			writeD(size);
			// Listing the Lord and the approved clans
			for (L2SiegeClan siegeClan : _castle.getSiege().getDefenderClans()) {
				clan = ClanTable.getInstance().getClan(siegeClan.getClanId());
				if (clan == null) {
					continue;
				}
				
				writeD(clan.getId());
				writeS(clan.getName());
				writeS(clan.getLeaderName());
				writeD(clan.getCrestId());
				writeD(0x00); // signed time (seconds) (not stored by L2J)
				switch (siegeClan.getType()) {
					case OWNER -> writeD(0x01); // owner
					case DEFENDER_PENDING -> writeD(0x02); // approved
					case DEFENDER -> writeD(0x03); // waiting approved
					default -> writeD(0x00);
				}
				writeD(clan.getAllyId());
				writeS(clan.getAllyName());
				writeS(""); // AllyLeaderName
				writeD(clan.getAllyCrestId());
			}
			for (L2SiegeClan siegeClan : _castle.getSiege().getDefenderWaitingClans()) {
				clan = ClanTable.getInstance().getClan(siegeClan.getClanId());
				writeD(clan.getId());
				writeS(clan.getName());
				writeS(clan.getLeaderName());
				writeD(clan.getCrestId());
				writeD(0x00); // signed time (seconds) (not stored by L2J)
				writeD(0x02); // waiting approval
				writeD(clan.getAllyId());
				writeS(clan.getAllyName());
				writeS(""); // AllyLeaderName
				writeD(clan.getAllyCrestId());
			}
		} else {
			writeD(0x00);
			writeD(0x00);
		}
	}
}
