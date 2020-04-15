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
package com.l2jserver.gameserver.data.sql.impl;

import static com.l2jserver.gameserver.config.Configuration.customs;
import static com.l2jserver.gameserver.enums.PrivateStoreType.NONE;

import java.util.Calendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.l2jserver.commons.database.ConnectionFactory;
import com.l2jserver.gameserver.LoginServerThread;
import com.l2jserver.gameserver.enums.PrivateStoreType;
import com.l2jserver.gameserver.model.L2ManufactureItem;
import com.l2jserver.gameserver.model.L2World;
import com.l2jserver.gameserver.model.TradeItem;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.network.L2GameClient;
import com.l2jserver.gameserver.network.L2GameClient.GameClientState;

public class OfflineTradersTable {
	
	private static final Logger LOG = LoggerFactory.getLogger(OfflineTradersTable.class);
	
	private static final String SAVE_OFFLINE_STATUS = "INSERT INTO character_offline_trade (`charId`,`time`,`type`,`title`) VALUES (?,?,?,?)";
	
	private static final String SAVE_ITEMS = "INSERT INTO character_offline_trade_items (`charId`,`item`,`count`,`price`) VALUES (?,?,?,?)";
	
	private static final String CLEAR_OFFLINE_TABLE = "DELETE FROM character_offline_trade";
	
	private static final String CLEAR_OFFLINE_TABLE_ITEMS = "DELETE FROM character_offline_trade_items";
	
	private static final String LOAD_OFFLINE_STATUS = "SELECT * FROM character_offline_trade";
	
	private static final String LOAD_OFFLINE_ITEMS = "SELECT * FROM character_offline_trade_items WHERE charId = ?";
	
	public void storeOffliners() {
		try (var con = ConnectionFactory.getInstance().getConnection();
			var stm1 = con.prepareStatement(CLEAR_OFFLINE_TABLE);
			var stm2 = con.prepareStatement(CLEAR_OFFLINE_TABLE_ITEMS);
			var stm3 = con.prepareStatement(SAVE_OFFLINE_STATUS);
			var stm_items = con.prepareStatement(SAVE_ITEMS)) {
			stm1.execute();
			stm2.execute();
			con.setAutoCommit(false); // avoid halfway done
			
			for (L2PcInstance pc : L2World.getInstance().getPlayers()) {
				try {
					if ((pc.getPrivateStoreType() != NONE) && pc.isInOfflineMode()) {
						stm3.setInt(1, pc.getObjectId());
						stm3.setLong(2, pc.getOfflineStartTime());
						stm3.setInt(3, pc.getPrivateStoreType().getId());
						String title = null;
						
						switch (pc.getPrivateStoreType()) {
							case BUY:
								if (!customs().offlineTradeEnable()) {
									continue;
								}
								title = pc.getBuyList().getTitle();
								for (TradeItem i : pc.getBuyList().getItems()) {
									stm_items.setInt(1, pc.getObjectId());
									stm_items.setInt(2, i.getItem().getId());
									stm_items.setLong(3, i.getCount());
									stm_items.setLong(4, i.getPrice());
									stm_items.executeUpdate();
									stm_items.clearParameters();
								}
								break;
							case SELL:
							case PACKAGE_SELL:
								if (!customs().offlineTradeEnable()) {
									continue;
								}
								title = pc.getSellList().getTitle();
								for (TradeItem i : pc.getSellList().getItems()) {
									stm_items.setInt(1, pc.getObjectId());
									stm_items.setInt(2, i.getObjectId());
									stm_items.setLong(3, i.getCount());
									stm_items.setLong(4, i.getPrice());
									stm_items.executeUpdate();
									stm_items.clearParameters();
								}
								break;
							case MANUFACTURE:
								if (!customs().offlineCraftEnable()) {
									continue;
								}
								title = pc.getStoreName();
								for (L2ManufactureItem i : pc.getManufactureItems().values()) {
									stm_items.setInt(1, pc.getObjectId());
									stm_items.setInt(2, i.getRecipeId());
									stm_items.setLong(3, 0);
									stm_items.setLong(4, i.getCost());
									stm_items.executeUpdate();
									stm_items.clearParameters();
								}
						}
						stm3.setString(4, title);
						stm3.executeUpdate();
						stm3.clearParameters();
						con.commit(); // flush
					}
				} catch (Exception ex) {
					LOG.warn("There has been an error while saving offline trader {}!", pc, ex);
				}
			}
			LOG.info("Offline traders stored.");
		} catch (Exception ex) {
			LOG.warn("There has been an error while saving offline traders!", ex);
		}
	}
	
	public void restoreOfflineTraders() {
		LOG.info("Loading offline traders...");
		int nTraders = 0;
		try (var con = ConnectionFactory.getInstance().getConnection();
			var stm = con.createStatement();
			var rs = stm.executeQuery(LOAD_OFFLINE_STATUS)) {
			while (rs.next()) {
				long time = rs.getLong("time");
				if (customs().getOfflineMaxDays() > 0) {
					Calendar cal = Calendar.getInstance();
					cal.setTimeInMillis(time);
					cal.add(Calendar.DAY_OF_YEAR, customs().getOfflineMaxDays());
					if (cal.getTimeInMillis() <= System.currentTimeMillis()) {
						continue;
					}
				}
				
				final var type = PrivateStoreType.findById(rs.getInt("type"));
				if (type == null) {
					LOG.warn("PrivateStoreType with Id {} could not be found!", rs.getInt("type"));
					continue;
				}
				
				if (type == NONE) {
					continue;
				}
				
				L2PcInstance player = null;
				
				try {
					L2GameClient client = new L2GameClient(null);
					client.setDetached(true);
					player = L2PcInstance.load(rs.getInt("charId"));
					client.setActiveChar(player);
					player.setOnlineStatus(true, false);
					client.setAccountName(player.getAccountNamePlayer());
					L2World.getInstance().addPlayerToWorld(player);
					client.setState(GameClientState.IN_GAME);
					player.setClient(client);
					player.setOfflineStartTime(time);
					player.spawnMe(player.getX(), player.getY(), player.getZ());
					LoginServerThread.getInstance().addGameServerLogin(player.getAccountName(), client);
					try (var stm_items = con.prepareStatement(LOAD_OFFLINE_ITEMS)) {
						stm_items.setInt(1, player.getObjectId());
						try (var items = stm_items.executeQuery()) {
							switch (type) {
								case BUY:
									while (items.next()) {
										if (player.getBuyList().addItemByItemId(items.getInt(2), items.getLong(3), items.getLong(4)) == null) {
											throw new NullPointerException();
										}
									}
									player.getBuyList().setTitle(rs.getString("title"));
									break;
								case SELL:
								case PACKAGE_SELL:
									while (items.next()) {
										if (player.getSellList().addItem(items.getInt(2), items.getLong(3), items.getLong(4)) == null) {
											throw new NullPointerException();
										}
									}
									player.getSellList().setTitle(rs.getString("title"));
									player.getSellList().setPackaged(type == PrivateStoreType.PACKAGE_SELL);
									break;
								case MANUFACTURE:
									while (items.next()) {
										player.getManufactureItems().put(items.getInt(2), new L2ManufactureItem(items.getInt(2), items.getLong(4)));
									}
									player.setStoreName(rs.getString("title"));
									break;
							}
						}
					}
					player.sitDown();
					if (customs().offlineSetNameColor()) {
						player.getAppearance().setNameColor(customs().getOfflineNameColor());
					}
					player.setPrivateStoreType(type);
					player.setOnlineStatus(true, true);
					player.restoreEffects();
					player.broadcastUserInfo();
					nTraders++;
				} catch (Exception ex) {
					LOG.warn("There has been an error loading trader {}!", player, ex);
					if (player != null) {
						player.deleteMe();
					}
				}
			}
			
			LOG.info("Loaded  {} offline trader(s).", nTraders);
			
			try (var stm1 = con.createStatement()) {
				stm1.execute(CLEAR_OFFLINE_TABLE);
				stm1.execute(CLEAR_OFFLINE_TABLE_ITEMS);
			}
		} catch (Exception ex) {
			LOG.warn("There has been an error while loading offline traders!", ex);
		}
	}
	
	public static OfflineTradersTable getInstance() {
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder {
		protected static final OfflineTradersTable INSTANCE = new OfflineTradersTable();
	}
}
