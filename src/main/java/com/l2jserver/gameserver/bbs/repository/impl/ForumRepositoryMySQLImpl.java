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

import static java.sql.Statement.RETURN_GENERATED_KEYS;

import java.util.HashMap;
import java.util.Map;

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
	
	private static final String SELECT_FORUMS = "SELECT forum_id, forum_name, forum_post, forum_type, forum_perm, forum_owner_id FROM forums WHERE forum_type = 0";
	
	private static final String SELECT_FORUM_CHILDREN = "SELECT forum_id, forum_name, forum_post, forum_type, forum_perm, forum_owner_id FROM forums WHERE forum_parent=?";
	
	private static final String INSERT_FORUM = "INSERT INTO forums (forum_name, forum_parent, forum_post, forum_type, forum_perm, forum_owner_id) VALUES (?,?,?,?,?,?)";
	
	@Override
	public Map<String, Forum> getForums() {
		final var forums = new HashMap<String, Forum>();
		try (var con = ConnectionFactory.getInstance().getConnection();
			var s = con.createStatement();
			var rs = s.executeQuery(SELECT_FORUMS)) {
			while (rs.next()) {
				final var forum = new Forum(rs.getInt("forum_id"), //
					rs.getString("forum_name"), //
					null, //
					ForumType.values()[rs.getInt("forum_type")], //
					ForumVisibility.values()[rs.getInt("forum_perm")], //
					rs.getInt("forum_owner_id"));
				forums.put(forum.getName(), forum);
				
				// Load topic
				DAOFactory.getInstance().getTopicRepository().load(forum);
				
				// Load children
				loadChildren(forum);
			}
		} catch (Exception ex) {
			LOG.warn("Data error on Forum (root)!", ex);
		}
		return forums;
	}
	
	private void loadChildren(Forum parent) {
		try (var con = ConnectionFactory.getInstance().getConnection();
			var ps = con.prepareStatement(SELECT_FORUM_CHILDREN)) {
			ps.setInt(1, parent.getId());
			try (var rs = ps.executeQuery()) {
				while (rs.next()) {
					ForumsBBSManager.getInstance().load(rs.getInt("forum_id"), //
						rs.getString("forum_name"), //
						parent, //
						ForumType.values()[rs.getInt("forum_type")], //
						ForumVisibility.values()[rs.getInt("forum_perm")], //
						rs.getInt("forum_owner_id"));
				}
			}
		} catch (Exception ex) {
			LOG.warn("Error loading child forums for forum Id {}!", parent.getId(), ex);
		}
	}
	
	@Override
	public void save(Forum forum) {
		try (var con = ConnectionFactory.getInstance().getConnection();
			var ps = con.prepareStatement(INSERT_FORUM, RETURN_GENERATED_KEYS)) {
			ps.setString(1, forum.getName());
			ps.setInt(2, forum.getParent().getId());
			ps.setInt(3, forum.getPost());
			ps.setInt(4, forum.getType().ordinal());
			ps.setInt(5, forum.getVisibility().ordinal());
			ps.setInt(6, forum.getOwnerId());
			ps.executeUpdate();
			
			try (var rs = ps.getGeneratedKeys()) {
				if (rs.next()) {
					forum.setId(rs.getInt(1));
				}
			}
		} catch (Exception ex) {
			LOG.error("Error saving forum Id {} in database!", forum.getId(), ex);
		}
	}
}
