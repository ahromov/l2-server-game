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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Forum.
 * @author Zoey76
 * @version 2.6.2.0
 */
public final class Forum {
	
	private int id;
	private String name;
	private ForumType type;
	private int post;
	private ForumVisibility visibility;
	private final Forum parent;
	private int ownerId;
	private final Map<String, Forum> children = new ConcurrentHashMap<>();
	private final Map<Integer, Topic> topics = new ConcurrentHashMap<>();
	
	public Forum(int id, String name, Forum parent, ForumType type, ForumVisibility visibility, int ownerId) {
		this.name = name;
		this.id = id;
		this.type = type;
		this.post = 0;
		this.visibility = visibility;
		this.parent = parent;
		this.ownerId = ownerId;
	}
	
	public int getTopicSize() {
		return topics.size();
	}
	
	public Topic getTopic(int topicId) {
		return topics.get(topicId);
	}
	
	public void addTopic(Topic t) {
		topics.put(t.getId(), t);
	}
	
	public void removeTopic(int topicId) {
		topics.remove(topicId);
	}
	
	public Forum getChildByName(String name) {
		return children.get(name);
	}
	
	public void addChild(Forum child) {
		children.put(child.getName(), child);
	}
	
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public ForumType getType() {
		return type;
	}
	
	public void setType(ForumType type) {
		this.type = type;
	}
	
	public int getPost() {
		return post;
	}
	
	public void setPost(int post) {
		this.post = post;
	}
	
	public ForumVisibility getVisibility() {
		return visibility;
	}
	
	public void setVisibility(ForumVisibility visibility) {
		this.visibility = visibility;
	}
	
	public int getOwnerId() {
		return ownerId;
	}
	
	public void setOwnerId(int ownerId) {
		this.ownerId = ownerId;
	}
	
	public Forum getParent() {
		return parent;
	}
	
	public Map<String, Forum> getChildren() {
		return children;
	}
	
	public Map<Integer, Topic> getTopics() {
		return topics;
	}
}