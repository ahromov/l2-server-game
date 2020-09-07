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

import java.util.List;

import com.l2jserver.gameserver.agathion.repository.AgathionRepository;
import com.l2jserver.gameserver.model.items.instance.L2ItemInstance;

/**
 * Agathion Energy Info packet.
 * @author Zoey76
 * @version 2.6.2.0
 */
public class ExBR_AgathionEnergyInfo extends L2GameServerPacket {
	
	private final List<L2ItemInstance> agathions;
	
	public ExBR_AgathionEnergyInfo(List<L2ItemInstance> agathions) {
		this.agathions = agathions;
	}
	
	@Override
	protected final void writeImpl() {
		writeC(0xFE);
		writeH(0xDE);
		writeD(agathions.size());
		for (var agathion : agathions) {
			writeD(agathion.getObjectId());
			writeD(agathion.getId());
			writeD(0x200000);
			writeD(agathion.getAgathionRemainingEnergy());
			writeD(AgathionRepository.getInstance().getByItemId(agathion.getId()).getEnergy());
		}
	}
}
