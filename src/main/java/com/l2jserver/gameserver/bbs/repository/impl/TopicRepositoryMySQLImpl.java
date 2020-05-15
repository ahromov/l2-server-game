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
import com.l2jserver.gameserver.bbs.model.Topic;
import com.l2jserver.gameserver.bbs.model.TopicType;
import com.l2jserver.gameserver.bbs.repository.TopicRepository;
import com.l2jserver.gameserver.bbs.service.TopicBBSManager;

/**
 * Topic Repository MySQL implementation.
 * @author Zoey76
 * @version 2.6.2.0
 */
public class TopicRepositoryMySQLImpl implements TopicRepository {
	
	private static final Logger LOG = LoggerFactory.getLogger(TopicRepositoryMySQLImpl.class);
	
	private static final String SELECT_TOPICS = "SELECT * FROM topic WHERE topic_forum_id=? ORDER BY topic_id DESC";
	
	private static final String DELETE_TOPIC = "DELETE FROM topic WHERE topic_id=? AND topic_forum_id=?";
	
	@Override
	public void load(Forum forum) {
		try (var con = ConnectionFactory.getInstance().getConnection();
			var ps = con.prepareStatement(SELECT_TOPICS)) {
			ps.setInt(1, forum.getId());
			try (var rs = ps.executeQuery()) {
				while (rs.next()) {
					final var topic = new Topic(rs.getInt("topic_id"), //
						rs.getInt("topic_forum_id"), rs.getString("topic_name"), rs.getLong("topic_date"), //
						rs.getString("topic_ownername"), rs.getInt("topic_ownerid"), TopicType.values()[rs.getInt("topic_type")], //
						rs.getInt("topic_reply"));
					
					TopicBBSManager.getInstance().addTopic(topic);
					
					forum.getTopics().put(topic.getId(), topic);
					if (topic.getId() > TopicBBSManager.getInstance().getMaxId(forum)) {
						TopicBBSManager.getInstance().setMaxId(topic.getId(), forum);
					}
				}
			}
		} catch (Exception ex) {
			LOG.warn("Could not get from database topics for forum Id {}!", forum.getId(), ex);
		}
	}
	
	@Override
	public void save(Topic topic) {
		try (var con = ConnectionFactory.getInstance().getConnection();
			var ps = con.prepareStatement("INSERT INTO topic (topic_id,topic_forum_id,topic_name,topic_date,topic_ownername,topic_ownerid,topic_type,topic_reply) values (?,?,?,?,?,?,?,?)")) {
			ps.setInt(1, topic.getId());
			ps.setInt(2, topic.getForumId());
			ps.setString(3, topic.getName());
			ps.setLong(4, topic.getDate());
			ps.setString(5, topic.getOwnerName());
			ps.setInt(6, topic.getOwnerId());
			ps.setInt(7, topic.getType().ordinal());
			ps.setInt(8, topic.getReply());
			ps.execute();
		} catch (Exception e) {
			LOG.warn("Error while saving new Topic to database!", e);
		}
	}
	
	@Override
	public void delete(Topic topic, Forum forum) {
		TopicBBSManager.getInstance().delTopic(topic);
		forum.rmTopicByID(topic.getId());
		try (var con = ConnectionFactory.getInstance().getConnection();
			var ps = con.prepareStatement(DELETE_TOPIC)) {
			ps.setInt(1, topic.getId());
			ps.setInt(2, forum.getId());
			ps.execute();
		} catch (Exception e) {
			LOG.warn("Error while deleting topic ID {} from database!", topic.getId(), e);
		}
	}
}
