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

import java.util.LinkedList;
import java.util.List;

import com.l2jserver.gameserver.agathion.repository.AgathionRepository;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.items.instance.L2ItemInstance;

/**
 * Item List server packet.
 * @author Zoey76
 * @version 2.6.2.0
 */
public final class ItemList extends AbstractItemPacket {
	
	private final L2PcInstance player;
	
	private final List<L2ItemInstance> items = new LinkedList<>();
	
	private final List<L2ItemInstance> agathions = new LinkedList<>();
	
	private final boolean showWindow;
	
	public ItemList(L2PcInstance player, boolean showWindow) {
		this.player = player;
		this.showWindow = showWindow;
		
		for (var item : player.getInventory().getItems()) {
			if (!item.isQuestItem()) {
				items.add(item);
			}
			
			final var agathion = AgathionRepository.getInstance().getByItemId(item.getId());
			if ((agathion != null) && (agathion.getMaxEnergy() > 0)) {
				agathions.add(item);
			}
		}
	}
	
	@Override
	protected void writeImpl() {
		writeC(0x11);
		writeH(showWindow ? 0x01 : 0x00);
		writeH(items.size());
		items.forEach(this::writeItem);
		writeInventoryBlock(player.getInventory());
		if (!agathions.isEmpty()) {
			player.sendPacket(new ExBR_AgathionEnergyInfo(agathions));
		}
	}
	
	@Override
	public void runImpl() {
		getClient().sendPacket(new ExQuestItemList(player));
	}
}
