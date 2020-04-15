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
import com.l2jserver.gameserver.dao.RecipeShopListDAO;
import com.l2jserver.gameserver.model.L2ManufactureItem;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;

/**
 * Recipe Shop List DAO MySQL implementation.
 * @author Zoey76
 */
public class RecipeShopListDAOMySQLImpl implements RecipeShopListDAO {
	
	private static final Logger LOG = LoggerFactory.getLogger(RecipeShopListDAOMySQLImpl.class);
	
	private static final String DELETE = "DELETE FROM character_recipeshoplist WHERE charId=?";
	
	private static final String INSERT = "REPLACE INTO character_recipeshoplist (`charId`, `recipeId`, `price`, `index`) VALUES (?, ?, ?, ?)";
	
	private static final String SELECT = "SELECT * FROM character_recipeshoplist WHERE charId=? ORDER BY `index`";
	
	@Override
	public void load(L2PcInstance player) {
		try (var con = ConnectionFactory.getInstance().getConnection();
			var ps = con.prepareStatement(SELECT)) {
			player.getManufactureItems().clear();
			
			ps.setInt(1, player.getObjectId());
			try (var rs = ps.executeQuery()) {
				while (rs.next()) {
					player.getManufactureItems().put(rs.getInt("recipeId"), new L2ManufactureItem(rs.getInt("recipeId"), rs.getLong("price")));
				}
			}
		} catch (Exception e) {
			LOG.error("Could not restore recipe shop list data for {}, {}", player, e);
		}
	}
	
	@Override
	public void delete(L2PcInstance player) {
		try (var con = ConnectionFactory.getInstance().getConnection();
			var ps = con.prepareStatement(DELETE)) {
			ps.setInt(1, player.getObjectId());
			ps.execute();
		} catch (Exception e) {
			LOG.error("Could not store recipe shop for {}, {}", player, e);
		}
	}
	
	@Override
	public void insert(L2PcInstance player) {
		if (!player.hasManufactureShop()) {
			return;
		}
		
		try (var con = ConnectionFactory.getInstance().getConnection();
			var ps = con.prepareStatement(INSERT)) {
			int i = 1;
			for (L2ManufactureItem item : player.getManufactureItems().values()) {
				ps.setInt(1, player.getObjectId());
				ps.setInt(2, item.getRecipeId());
				ps.setLong(3, item.getCost());
				ps.setInt(4, i++);
				ps.addBatch();
			}
			ps.executeBatch();
		} catch (Exception e) {
			LOG.error("Could not store recipe shop for {}, {}", player, e);
		}
	}
}
