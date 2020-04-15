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

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.l2jserver.commons.database.ConnectionFactory;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.announce.Announcement;
import com.l2jserver.gameserver.model.announce.AnnouncementType;
import com.l2jserver.gameserver.model.announce.AutoAnnouncement;
import com.l2jserver.gameserver.model.announce.IAnnouncement;
import com.l2jserver.gameserver.network.clientpackets.Say2;
import com.l2jserver.gameserver.network.serverpackets.CreatureSay;

/**
 * Loads announcements from database.
 * @author UnAfraid
 */
public final class AnnouncementsTable {
	
	private static final Logger LOG = LoggerFactory.getLogger(AnnouncementsTable.class);
	
	private final Map<Integer, IAnnouncement> _announcements = new ConcurrentSkipListMap<>();
	
	protected AnnouncementsTable() {
		load();
	}
	
	private void load() {
		_announcements.clear();
		try (var con = ConnectionFactory.getInstance().getConnection();
			var st = con.createStatement();
			var rs = st.executeQuery("SELECT `id`, `type`, `initial`, `delay`, `repeat`, `author`, `content` FROM announcements")) {
			while (rs.next()) {
				final AnnouncementType type = AnnouncementType.findById(rs.getInt("type"));
				final var author = rs.getString("author");
				final var content = rs.getString("content");
				final Announcement announce;
				switch (type) {
					case NORMAL:
					case CRITICAL: {
						announce = new Announcement(rs.getInt("id"), type, content, author);
						break;
					}
					case AUTO_NORMAL:
					case AUTO_CRITICAL: {
						announce = new AutoAnnouncement(type, content, author, rs.getLong("initial"), rs.getLong("delay"), rs.getInt("repeat"));
						break;
					}
					default: {
						continue;
					}
				}
				_announcements.put(announce.getId(), announce);
			}
		} catch (Exception e) {
			LOG.warn("Failed loading announcements:", e);
		}
	}
	
	/**
	 * Sending all announcements to the player
	 * @param player
	 */
	public void showAnnouncements(L2PcInstance player) {
		sendAnnouncements(player, AnnouncementType.NORMAL);
		sendAnnouncements(player, AnnouncementType.CRITICAL);
		sendAnnouncements(player, AnnouncementType.EVENT);
	}
	
	/**
	 * Sends all announcements to the player by the specified type
	 * @param player
	 * @param type
	 */
	public void sendAnnouncements(L2PcInstance player, AnnouncementType type) {
		for (IAnnouncement announce : _announcements.values()) {
			if (announce.isValid() && (announce.getType() == type)) {
				player.sendPacket(new CreatureSay(0, //
					type == AnnouncementType.CRITICAL ? Say2.CRITICAL_ANNOUNCE : Say2.ANNOUNCEMENT, //
					player.getName(), announce.getContent()));
			}
		}
	}
	
	/**
	 * Adds announcement
	 * @param announce
	 */
	public void addAnnouncement(IAnnouncement announce) {
		if (announce.storeMe()) {
			_announcements.put(announce.getId(), announce);
		}
	}
	
	/**
	 * Removes announcement by id
	 * @param id
	 * @return {@code true} if announcement exists and was deleted successfully, {@code false} otherwise.
	 */
	public boolean deleteAnnouncement(int id) {
		final IAnnouncement announce = _announcements.remove(id);
		return (announce != null) && announce.deleteMe();
	}
	
	/**
	 * @param id
	 * @return {@link IAnnouncement} by id
	 */
	public IAnnouncement getAnnounce(int id) {
		return _announcements.get(id);
	}
	
	/**
	 * @return {@link Collection} containing all announcements
	 */
	public Collection<IAnnouncement> getAllAnnouncements() {
		return _announcements.values();
	}
	
	/**
	 * @return Single instance of {@link AnnouncementsTable}
	 */
	public static AnnouncementsTable getInstance() {
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder {
		protected static final AnnouncementsTable _instance = new AnnouncementsTable();
	}
}
