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
package com.l2jserver.gameserver.bbs.service;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.l2jserver.commons.database.ConnectionFactory;
import com.l2jserver.gameserver.bbs.model.Forum;
import com.l2jserver.gameserver.bbs.model.ForumType;
import com.l2jserver.gameserver.bbs.model.ForumVisibility;
import com.l2jserver.gameserver.dao.factory.impl.DAOFactory;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;

/**
 * Forums BBS Manager.
 * @author Zoey76
 * @version 2.6.2.0
 */
public class ForumsBBSManager extends BaseBBSManager {
	
	private static final Logger LOG = LoggerFactory.getLogger(ForumsBBSManager.class);
	
	private final List<Forum> table = new CopyOnWriteArrayList<>();
	
	private int lastId = 1;
	
	protected ForumsBBSManager() {
		try (var con = ConnectionFactory.getInstance().getConnection();
			var s = con.createStatement();
			var rs = s.executeQuery("SELECT forum_id FROM forums WHERE forum_type = 0")) {
			while (rs.next()) {
				addForum(new Forum(rs.getInt("forum_id"), null));
			}
		} catch (Exception ex) {
			LOG.warn("Data error on Forum (root)!", ex);
		}
	}
	
	public void initRoot() {
		table.forEach(f -> DAOFactory.getInstance().getForumRepository().findById(f));
		LOG.info("Loaded " + table.size() + " forums. Last forum id used: " + lastId);
	}
	
	public void addForum(Forum forum) {
		if (forum == null) {
			return;
		}
		
		table.add(forum);
		
		if (forum.getId() > lastId) {
			lastId = forum.getId();
		}
	}
	
	@Override
	public void parsecmd(String command, L2PcInstance activeChar) {
	}
	
	public Forum getForumByName(String name) {
		return table.stream().filter(f -> f.getName().equals(name)).findFirst().orElse(null);
	}
	
	public Forum createNewForum(String name, Forum parent, ForumType type, ForumVisibility visibility, int ownerId) {
		final var id = ForumsBBSManager.getInstance().getANewID();
		final var forum = new Forum(id, name, parent, type, visibility, ownerId);
		
		parent.addChildren(forum);
		ForumsBBSManager.getInstance().addForum(forum);
		
		DAOFactory.getInstance().getForumRepository().save(forum);
		return forum;
	}
	
	public int getANewID() {
		return ++lastId;
	}
	
	public Forum getForumByID(int id) {
		return table.stream().filter(f -> f.getId() == id).findFirst().orElse(null);
	}
	
	@Override
	public void parsewrite(String ar1, String ar2, String ar3, String ar4, String ar5, L2PcInstance activeChar) {
		
	}
	
	public static ForumsBBSManager getInstance() {
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder {
		protected static final ForumsBBSManager _instance = new ForumsBBSManager();
	}
}