/*
 * Copyright © 2004-2021 L2J Server
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
package com.l2jserver.gameserver.bbs.model;

/**
 * Topic.
 * @author Zoey76
 * @version 2.6.2.0
 */
public class Topic {
	
	private final int id;
	
	private final int forumId;
	
	private final String name;
	
	private final long date;
	
	private final String ownerName;
	
	private final int ownerId;
	
	private final TopicType type;
	
	private final int reply;
	
	public Topic(int id, int forumId, String name, long date, String ownerName, int ownerId, TopicType type, int reply) {
		this.id = id;
		this.forumId = forumId;
		this.name = name;
		this.date = date;
		this.ownerName = ownerName;
		this.ownerId = ownerId;
		this.type = type;
		this.reply = reply;
	}
	
	public int getId() {
		return id;
	}
	
	public int getForumId() {
		return forumId;
	}
	
	public String getName() {
		return name;
	}
	
	public long getDate() {
		return date;
	}
	
	public String getOwnerName() {
		return ownerName;
	}
	
	public int getOwnerId() {
		return ownerId;
	}
	
	public TopicType getType() {
		return type;
	}
	
	public int getReply() {
		return reply;
	}
}
