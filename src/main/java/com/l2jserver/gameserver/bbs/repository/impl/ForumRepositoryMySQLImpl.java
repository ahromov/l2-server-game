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
package com.l2jserver.gameserver.bbs.repository.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.l2jserver.commons.database.ConnectionFactory;
import com.l2jserver.gameserver.bbs.model.Forum;
import com.l2jserver.gameserver.bbs.model.ForumType;
import com.l2jserver.gameserver.bbs.model.ForumVisibility;
import com.l2jserver.gameserver.bbs.repository.ForumRepository;
import com.l2jserver.gameserver.bbs.service.ForumsBBSManager;
import com.l2jserver.gameserver.dao.factory.impl.DAOFactory;

/**
 * Forum repository MySQL implementation.
 * @author Zoey76
 * @version 2.6.2.0
 */
public class ForumRepositoryMySQLImpl implements ForumRepository {
	
	private static final Logger LOG = LoggerFactory.getLogger(ForumRepositoryMySQLImpl.class);
	
	private static final String SELECT_FORUM = "SELECT forum_name, forum_post, forum_type, forum_perm, forum_owner_id FROM forums WHERE forum_id=?";
	
	private static final String INSERT_FORUM = "INSERT INTO forums (forum_id,forum_name,forum_parent,forum_post,forum_type,forum_perm,forum_owner_id) VALUES (?,?,?,?,?,?,?)";
	
	@Override
	public void findById(Forum forum) {
		try (var con = ConnectionFactory.getInstance().getConnection();
			var ps = con.prepareStatement(SELECT_FORUM)) {
			ps.setInt(1, forum.getId());
			try (var rs = ps.executeQuery()) {
				if (rs.next()) {
					forum.setName(rs.getString("forum_name"));
					forum.setPost(rs.getInt("forum_post"));
					forum.setType(ForumType.values()[rs.getInt("forum_type")]);
					forum.setVisibility(ForumVisibility.values()[rs.getInt("forum_perm")]);
					forum.setOwnerId(rs.getInt("forum_owner_id"));
				}
			}
		} catch (Exception ex) {
			LOG.warn("Could not get from database forum Id {}!", forum.getId(), ex);
		}
		
		DAOFactory.getInstance().getTopicRepository().load(forum);
		
		loadChildren(forum);
	}
	
	private void loadChildren(Forum forum) {
		try (var con = ConnectionFactory.getInstance().getConnection();
			var ps = con.prepareStatement("SELECT forum_id FROM forums WHERE forum_parent=?")) {
			ps.setInt(1, forum.getId());
			try (var rs = ps.executeQuery()) {
				while (rs.next()) {
					final var childForum = new Forum(rs.getInt("forum_id"), forum);
					forum.getChildren().add(childForum);
					ForumsBBSManager.getInstance().addForum(childForum);
				}
			}
		} catch (Exception e) {
			LOG.warn("Could not get from database child forums for forum Id {}!", forum.getId(), e);
		}
	}
	
	@Override
	public void save(Forum forum) {
		try (var con = ConnectionFactory.getInstance().getConnection();
			var ps = con.prepareStatement(INSERT_FORUM)) {
			ps.setInt(1, forum.getId());
			ps.setString(2, forum.getName());
			ps.setInt(3, forum.getParent().getId());
			ps.setInt(4, forum.getPost());
			ps.setInt(5, forum.getType().ordinal());
			ps.setInt(6, forum.getVisibility().ordinal());
			ps.setInt(7, forum.getOwnerId());
			ps.execute();
		} catch (Exception ex) {
			LOG.error("Could not save forum If {} in database!", forum.getId(), ex);
		}
	}
}
