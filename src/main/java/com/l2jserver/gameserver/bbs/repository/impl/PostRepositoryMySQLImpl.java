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

import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.l2jserver.commons.database.ConnectionFactory;
import com.l2jserver.gameserver.bbs.model.Post;
import com.l2jserver.gameserver.bbs.model.Topic;
import com.l2jserver.gameserver.bbs.repository.PostRepository;

/**
 * Post repository MySQL implementation.
 * @author Zoey76
 * @version 2.6.2.0
 */
public class PostRepositoryMySQLImpl implements PostRepository {
	
	private static final Logger LOG = LoggerFactory.getLogger(PostRepositoryMySQLImpl.class);
	
	private static final String INSERT_POST = "INSERT INTO posts (post_id,post_owner_name,post_ownerid,post_date,post_topic_id,post_forum_id,post_txt) values (?,?,?,?,?,?,?)";
	
	private static final String DELETE_POST = "DELETE FROM posts WHERE post_forum_id=? AND post_topic_id=?";
	
	private static final String SELECT_POSTS = "SELECT * FROM posts WHERE post_forum_id=? AND post_topic_id=? ORDER BY post_id";
	
	private static final String UPDATE_POST = "UPDATE posts SET post_txt=? WHERE post_id=? AND post_topic_id=? AND post_forum_id=?";
	
	public void save(Post cp) {
		try (var con = ConnectionFactory.getInstance().getConnection();
			var ps = con.prepareStatement(INSERT_POST)) {
			ps.setInt(1, cp.getId());
			ps.setString(2, cp.getOwnerName());
			ps.setInt(3, cp.getOwnerId());
			ps.setLong(4, cp.getDate());
			ps.setInt(5, cp.getTopicId());
			ps.setInt(6, cp.getForumId());
			ps.setString(7, cp.getTxt());
			ps.execute();
		} catch (Exception ex) {
			LOG.warn("Could not save post Id {} in database!", cp.getId(), ex);
		}
	}
	
	@Override
	public void delete(Topic topic) {
		try (var con = ConnectionFactory.getInstance().getConnection();
			var ps = con.prepareStatement(DELETE_POST)) {
			ps.setInt(1, topic.getForumId());
			ps.setInt(2, topic.getId());
			ps.execute();
		} catch (Exception ex) {
			LOG.warn("Unable to delete post for topic Id {} in forum Id {} from database!", topic.getForumId(), topic.getId(), ex);
		}
	}
	
	@Override
	public List<Post> load(Topic topic) {
		final var posts = new LinkedList<Post>();
		try (var con = ConnectionFactory.getInstance().getConnection();
			var ps = con.prepareStatement(SELECT_POSTS)) {
			ps.setInt(1, topic.getForumId());
			ps.setInt(2, topic.getId());
			try (var rs = ps.executeQuery()) {
				while (rs.next()) {
					final Post cp = new Post(rs.getInt("post_id"), rs.getString("post_owner_name"), rs.getInt("post_ownerid"), //
						rs.getLong("post_date"), rs.getInt("post_topic_id"), rs.getInt("post_forum_id"), rs.getString("post_txt"));
					posts.add(cp);
				}
			}
		} catch (Exception ex) {
			LOG.warn("Unable to get post from topic Id {} in forum Id {} from database!", topic.getForumId(), topic.getId(), ex);
		}
		return posts;
	}
	
	@Override
	public void update(Post cp) {
		try (var con = ConnectionFactory.getInstance().getConnection();
			var ps = con.prepareStatement(UPDATE_POST)) {
			ps.setString(1, cp.getTxt());
			ps.setInt(2, cp.getId());
			ps.setInt(3, cp.getTopicId());
			ps.setInt(4, cp.getForumId());
			ps.execute();
		} catch (Exception ex) {
			LOG.warn("Unable to store post Id {} in database!", cp.getId(), ex);
		}
	}
}
