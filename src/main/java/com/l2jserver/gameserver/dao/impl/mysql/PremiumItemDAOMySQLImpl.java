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
package com.l2jserver.gameserver.dao.impl.mysql;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.l2jserver.commons.database.ConnectionFactory;
import com.l2jserver.gameserver.dao.PremiumItemDAO;
import com.l2jserver.gameserver.model.L2PremiumItem;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;

/**
 * Premium Item DAO MySQL implementation.
 * @author Zoey76
 */
public class PremiumItemDAOMySQLImpl implements PremiumItemDAO {
	
	private static final Logger LOG = LoggerFactory.getLogger(PremiumItemDAOMySQLImpl.class);
	
	private static final String GET_PREMIUM_ITEMS = "SELECT itemNum, itemId, itemCount, itemSender FROM character_premium_items WHERE charId=?";
	
	@Override
	public void load(L2PcInstance player) {
		try (var con = ConnectionFactory.getInstance().getConnection();
			var ps = con.prepareStatement(GET_PREMIUM_ITEMS)) {
			ps.setInt(1, player.getObjectId());
			try (var rset = ps.executeQuery()) {
				while (rset.next()) {
					int itemNum = rset.getInt("itemNum");
					int itemId = rset.getInt("itemId");
					long itemCount = rset.getLong("itemCount");
					String itemSender = rset.getString("itemSender");
					player.getPremiumItemList().put(itemNum, new L2PremiumItem(itemId, itemCount, itemSender));
				}
			}
		} catch (Exception e) {
			LOG.error("Could not restore premium items: {}", e);
		}
	}
	
	@Override
	public void update(L2PcInstance player, int itemNum, long newcount) {
		try (var con = ConnectionFactory.getInstance().getConnection();
			var ps = con.prepareStatement("UPDATE character_premium_items SET itemCount=? WHERE charId=? AND itemNum=? ")) {
			ps.setLong(1, newcount);
			ps.setInt(2, player.getObjectId());
			ps.setInt(3, itemNum);
			ps.execute();
		} catch (Exception e) {
			LOG.error("Could not update premium items: {}", e);
		}
	}
	
	@Override
	public void delete(L2PcInstance player, int itemNum) {
		try (var con = ConnectionFactory.getInstance().getConnection();
			var ps = con.prepareStatement("DELETE FROM character_premium_items WHERE charId=? AND itemNum=? ")) {
			ps.setInt(1, player.getObjectId());
			ps.setInt(2, itemNum);
			ps.execute();
		} catch (Exception e) {
			LOG.error("Could not delete premium item: {}" + e);
		}
	}
}
