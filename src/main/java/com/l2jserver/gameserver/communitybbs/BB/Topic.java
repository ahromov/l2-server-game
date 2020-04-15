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
package com.l2jserver.gameserver.communitybbs.BB;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.l2jserver.commons.database.ConnectionFactory;
import com.l2jserver.gameserver.communitybbs.Manager.TopicBBSManager;

public class Topic {
	
	private static final Logger LOG = LoggerFactory.getLogger(Topic.class);
	
	public static final int MORMAL = 0;
	public static final int MEMO = 1;
	
	private final int _id;
	private final int _forumId;
	private final String _topicName;
	private final long _date;
	private final String _ownerName;
	private final int _ownerId;
	private final int _type;
	private final int _cReply;
	
	public Topic(ConstructorType ct, int id, int fid, String name, long date, String oname, int oid, int type, int Creply) {
		_id = id;
		_forumId = fid;
		_topicName = name;
		_date = date;
		_ownerName = oname;
		_ownerId = oid;
		_type = type;
		_cReply = Creply;
		TopicBBSManager.getInstance().addTopic(this);
		
		if (ct == ConstructorType.CREATE) {
			insertindb();
		}
	}
	
	public void insertindb() {
		try (var con = ConnectionFactory.getInstance().getConnection();
			var ps = con.prepareStatement("INSERT INTO topic (topic_id,topic_forum_id,topic_name,topic_date,topic_ownername,topic_ownerid,topic_type,topic_reply) values (?,?,?,?,?,?,?,?)")) {
			ps.setInt(1, _id);
			ps.setInt(2, _forumId);
			ps.setString(3, _topicName);
			ps.setLong(4, _date);
			ps.setString(5, _ownerName);
			ps.setInt(6, _ownerId);
			ps.setInt(7, _type);
			ps.setInt(8, _cReply);
			ps.execute();
		} catch (Exception e) {
			LOG.warn("Error while saving new Topic to database!", e);
		}
	}
	
	public enum ConstructorType {
		RESTORE,
		CREATE
	}
	
	public int getID() {
		return _id;
	}
	
	public int getForumID() {
		return _forumId;
	}
	
	public String getName() {
		return _topicName;
	}
	
	public String getOwnerName() {
		return _ownerName;
	}
	
	public long getDate() {
		return _date;
	}
	
	public void deleteme(Forum f) {
		TopicBBSManager.getInstance().delTopic(this);
		f.rmTopicByID(getID());
		try (var con = ConnectionFactory.getInstance().getConnection();
			var ps = con.prepareStatement("DELETE FROM topic WHERE topic_id=? AND topic_forum_id=?")) {
			ps.setInt(1, getID());
			ps.setInt(2, f.getID());
			ps.execute();
		} catch (Exception e) {
			LOG.warn("Error while deleting topic ID {} from database!", getID(), e);
		}
	}
}
