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
package com.l2jserver.gameserver.instancemanager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.l2jserver.commons.database.ConnectionFactory;
import com.l2jserver.gameserver.ThreadPoolManager;
import com.l2jserver.gameserver.idfactory.IdFactory;
import com.l2jserver.gameserver.instancemanager.tasks.MessageDeletionTask;
import com.l2jserver.gameserver.model.L2World;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.entity.Message;
import com.l2jserver.gameserver.network.serverpackets.ExNoticePostArrived;

/**
 * Mail Manager.
 * @author Migi, DS
 */
public final class MailManager {
	
	private static final Logger LOG = LoggerFactory.getLogger(MailManager.class);
	
	private final Map<Integer, Message> _messages = new ConcurrentHashMap<>();
	
	protected MailManager() {
		load();
	}
	
	private void load() {
		int count = 0;
		try (var con = ConnectionFactory.getInstance().getConnection();
			var ps = con.createStatement();
			var rs = ps.executeQuery("SELECT * FROM messages ORDER BY expiration")) {
			while (rs.next()) {
				final Message msg = new Message(rs);
				
				int msgId = msg.getId();
				_messages.put(msgId, msg);
				
				count++;
				
				long expiration = msg.getExpiration();
				
				if (expiration < System.currentTimeMillis()) {
					ThreadPoolManager.getInstance().scheduleGeneral(new MessageDeletionTask(msgId), 10000);
				} else {
					ThreadPoolManager.getInstance().scheduleGeneral(new MessageDeletionTask(msgId), expiration - System.currentTimeMillis());
				}
			}
		} catch (Exception ex) {
			LOG.warn("There has been an error loading from database!", ex);
		}
		LOG.info("Successfully loaded {} messages.", count);
	}
	
	public final Message getMessage(int msgId) {
		return _messages.get(msgId);
	}
	
	public final Collection<Message> getMessages() {
		return _messages.values();
	}
	
	public final boolean hasUnreadPost(L2PcInstance player) {
		final int objectId = player.getObjectId();
		for (Message msg : getMessages()) {
			if ((msg != null) && (msg.getReceiverId() == objectId) && msg.isUnread()) {
				return true;
			}
		}
		return false;
	}
	
	public final int getInboxSize(int objectId) {
		int size = 0;
		for (Message msg : getMessages()) {
			if ((msg != null) && (msg.getReceiverId() == objectId) && !msg.isDeletedByReceiver()) {
				size++;
			}
		}
		return size;
	}
	
	public final int getOutboxSize(int objectId) {
		int size = 0;
		for (Message msg : getMessages()) {
			if ((msg != null) && (msg.getSenderId() == objectId) && !msg.isDeletedBySender()) {
				size++;
			}
		}
		return size;
	}
	
	public final List<Message> getInbox(int objectId) {
		final List<Message> inbox = new ArrayList<>();
		for (Message msg : getMessages()) {
			if ((msg != null) && (msg.getReceiverId() == objectId) && !msg.isDeletedByReceiver()) {
				inbox.add(msg);
			}
		}
		return inbox;
	}
	
	public final List<Message> getOutbox(int objectId) {
		final List<Message> outbox = new ArrayList<>();
		for (Message msg : getMessages()) {
			if ((msg != null) && (msg.getSenderId() == objectId) && !msg.isDeletedBySender()) {
				outbox.add(msg);
			}
		}
		return outbox;
	}
	
	public void sendMessage(Message msg) {
		_messages.put(msg.getId(), msg);
		try (var con = ConnectionFactory.getInstance().getConnection();
			var ps = Message.getStatement(msg, con)) {
			ps.execute();
		} catch (Exception ex) {
			LOG.warn("There has been an error saving message Id {}!", msg.getId(), ex);
		}
		
		final L2PcInstance receiver = L2World.getInstance().getPlayer(msg.getReceiverId());
		if (receiver != null) {
			receiver.sendPacket(ExNoticePostArrived.valueOf(true));
		}
		
		ThreadPoolManager.getInstance().scheduleGeneral(new MessageDeletionTask(msg.getId()), msg.getExpiration() - System.currentTimeMillis());
	}
	
	public final void markAsReadInDb(int msgId) {
		try (var con = ConnectionFactory.getInstance().getConnection();
			var ps = con.prepareStatement("UPDATE messages SET isUnread = 'false' WHERE messageId = ?")) {
			ps.setInt(1, msgId);
			ps.execute();
		} catch (Exception ex) {
			LOG.warn("There has been an error marking as read message Id {}!", msgId, ex);
		}
	}
	
	public final void markAsDeletedBySenderInDb(int msgId) {
		try (var con = ConnectionFactory.getInstance().getConnection();
			var ps = con.prepareStatement("UPDATE messages SET isDeletedBySender = 'true' WHERE messageId = ?")) {
			ps.setInt(1, msgId);
			ps.execute();
		} catch (Exception ex) {
			LOG.warn("There has been an error marking as deleted by sender message Id {}!", msgId, ex);
		}
	}
	
	public final void markAsDeletedByReceiverInDb(int msgId) {
		try (var con = ConnectionFactory.getInstance().getConnection();
			var ps = con.prepareStatement("UPDATE messages SET isDeletedByReceiver = 'true' WHERE messageId = ?")) {
			ps.setInt(1, msgId);
			ps.execute();
		} catch (Exception ex) {
			LOG.warn("There has been an error marking as deleted by receiver message Id {}!", msgId, ex);
		}
	}
	
	public final void removeAttachmentsInDb(int msgId) {
		try (var con = ConnectionFactory.getInstance().getConnection();
			var ps = con.prepareStatement("UPDATE messages SET hasAttachments = 'false' WHERE messageId = ?")) {
			ps.setInt(1, msgId);
			ps.execute();
		} catch (Exception ex) {
			LOG.warn("There has been an error removing attachments in message Id {}!", msgId, ex);
		}
	}
	
	public final void deleteMessageInDb(int msgId) {
		try (var con = ConnectionFactory.getInstance().getConnection();
			var ps = con.prepareStatement("DELETE FROM messages WHERE messageId = ?")) {
			ps.setInt(1, msgId);
			ps.execute();
		} catch (Exception ex) {
			LOG.warn("There has been an error deleting message Id {}!", msgId, ex);
		}
		
		_messages.remove(msgId);
		IdFactory.getInstance().releaseId(msgId);
	}
	
	public static MailManager getInstance() {
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder {
		protected static final MailManager INSTANCE = new MailManager();
	}
}