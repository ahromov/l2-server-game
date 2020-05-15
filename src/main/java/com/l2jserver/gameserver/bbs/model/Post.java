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
package com.l2jserver.gameserver.bbs.model;

/**
 * Post.
 * @author Zoey76
 * @version 2.6.2.0
 */
public class Post {
	
	private int id;
	
	private String ownerName;
	
	private int ownerId;
	
	private long date;
	
	private int topicId;
	
	private int forumId;
	
	private String txt;
	
	public Post(int id, String ownerName, int ownerId, long date, int topicId, int forumId, String txt) {
		this.id = id;
		this.ownerName = ownerName;
		this.ownerId = ownerId;
		this.date = date;
		this.topicId = topicId;
		this.forumId = forumId;
		this.txt = txt;
	}
	
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public String getOwnerName() {
		return ownerName;
	}
	
	public void setOwnerName(String ownerName) {
		this.ownerName = ownerName;
	}
	
	public int getOwnerId() {
		return ownerId;
	}
	
	public void setOwnerId(int ownerId) {
		this.ownerId = ownerId;
	}
	
	public long getDate() {
		return date;
	}
	
	public void setDate(long date) {
		this.date = date;
	}
	
	public int getTopicId() {
		return topicId;
	}
	
	public void setTopicId(int topicId) {
		this.topicId = topicId;
	}
	
	public int getForumId() {
		return forumId;
	}
	
	public void setForumId(int forumId) {
		this.forumId = forumId;
	}
	
	public String getTxt() {
		return txt;
	}
	
	public void setTxt(String txt) {
		this.txt = txt;
	}
}