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

import com.l2jserver.gameserver.model.items.L2Item;
import com.l2jserver.gameserver.model.items.instance.L2ItemInstance;

public final class EquipUpdate extends L2GameServerPacket {
	private final L2ItemInstance _item;
	private final int _change;
	
	public EquipUpdate(L2ItemInstance item, int change) {
		_item = item;
		_change = change;
	}
	
	@Override
	protected void writeImpl() {
		int bodyPart = 0;
		writeC(0x4b);
		writeD(_change);
		writeD(_item.getObjectId());
		switch (_item.getItem().getBodyPart()) {
			case L2Item.SLOT_L_EAR -> bodyPart = 0x01;
			case L2Item.SLOT_R_EAR -> bodyPart = 0x02;
			case L2Item.SLOT_NECK -> bodyPart = 0x03;
			case L2Item.SLOT_R_FINGER -> bodyPart = 0x04;
			case L2Item.SLOT_L_FINGER -> bodyPart = 0x05;
			case L2Item.SLOT_HEAD -> bodyPart = 0x06;
			case L2Item.SLOT_R_HAND -> bodyPart = 0x07;
			case L2Item.SLOT_L_HAND -> bodyPart = 0x08;
			case L2Item.SLOT_GLOVES -> bodyPart = 0x09;
			case L2Item.SLOT_CHEST -> bodyPart = 0x0a;
			case L2Item.SLOT_LEGS -> bodyPart = 0x0b;
			case L2Item.SLOT_FEET -> bodyPart = 0x0c;
			case L2Item.SLOT_BACK -> bodyPart = 0x0d;
			case L2Item.SLOT_LR_HAND -> bodyPart = 0x0e;
			case L2Item.SLOT_HAIR -> bodyPart = 0x0f;
			case L2Item.SLOT_BELT -> bodyPart = 0x10;
		}
		writeD(bodyPart);
	}
}
