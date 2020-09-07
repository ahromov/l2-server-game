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

import static com.l2jserver.gameserver.bbs.model.ForumType.CLAN;
import static com.l2jserver.gameserver.bbs.model.ForumVisibility.CLAN_MEMBER_ONLY;
import static com.l2jserver.gameserver.config.Configuration.general;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.l2jserver.gameserver.bbs.model.Forum;
import com.l2jserver.gameserver.bbs.model.ForumType;
import com.l2jserver.gameserver.bbs.model.ForumVisibility;
import com.l2jserver.gameserver.dao.factory.impl.DAOFactory;
import com.l2jserver.gameserver.model.L2Clan;

/**
 * Forums BBS Manager.
 * @author Zoey76
 * @version 2.6.2.0
 */
public class ForumsBBSManager {
	
	private static final Logger LOG = LoggerFactory.getLogger(ForumsBBSManager.class);
	
	private static final Map<String, Forum> FORUMS_BY_NAME = new ConcurrentHashMap<>();
	
	private static final Map<Integer, Forum> FORUMS_BY_ID = new ConcurrentHashMap<>();
	
	protected ForumsBBSManager() {
		// Do nothing.
	}
	
	public void load() {
		FORUMS_BY_NAME.putAll(DAOFactory.getInstance().getForumRepository().getForums());
		FORUMS_BY_ID.putAll(FORUMS_BY_NAME.values().stream().collect(Collectors.toMap(Forum::getId, f -> f)));
		LOG.info("Loaded {} forums.", FORUMS_BY_NAME.size());
	}
	
	public Forum getForumByName(String name) {
		return FORUMS_BY_NAME.get(name);
	}
	
	public Forum getForumById(Integer id) {
		return FORUMS_BY_ID.get(id);
	}
	
	public Forum create(String name, Forum parent, ForumType type, ForumVisibility visibility, int ownerId) {
		final var forum = new Forum(0, name, parent, type, visibility, ownerId);
		parent.addChild(forum);
		
		DAOFactory.getInstance().getForumRepository().save(forum);
		
		FORUMS_BY_NAME.put(forum.getName(), forum);
		FORUMS_BY_ID.put(forum.getId(), forum);
		return forum;
	}
	
	public Forum load(int id, String name, Forum parent, ForumType type, ForumVisibility visibility, int ownerId) {
		final var forum = new Forum(id, name, parent, type, visibility, ownerId);
		parent.addChild(forum);
		FORUMS_BY_NAME.put(forum.getName(), forum);
		FORUMS_BY_ID.put(forum.getId(), forum);
		return forum;
	}
	
	public void onClanLevel(L2Clan clan) {
		if ((clan.getLevel() >= 2) && general().enableCommunityBoard()) {
			final var clanRootForum = ForumsBBSManager.getInstance().getForumByName("ClanRoot");
			if (clanRootForum != null) {
				var forum = clanRootForum.getChildByName(clan.getName());
				if (forum == null) {
					ForumsBBSManager.getInstance().create(clan.getName(), clanRootForum, CLAN, CLAN_MEMBER_ONLY, clan.getId());
				}
			}
		}
	}
	
	public static ForumsBBSManager getInstance() {
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder {
		protected static final ForumsBBSManager INSTANCE = new ForumsBBSManager();
	}
}