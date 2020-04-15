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
package com.l2jserver.gameserver.network;

import static com.l2jserver.gameserver.config.Configuration.character;
import static com.l2jserver.gameserver.config.Configuration.customs;
import static com.l2jserver.gameserver.config.Configuration.general;
import static com.l2jserver.gameserver.config.Configuration.mmo;
import static com.l2jserver.gameserver.model.PcCondOverride.SEE_ALL_PLAYERS;
import static java.util.concurrent.TimeUnit.DAYS;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.l2jserver.commons.database.ConnectionFactory;
import com.l2jserver.gameserver.LoginServerThread;
import com.l2jserver.gameserver.LoginServerThread.SessionKey;
import com.l2jserver.gameserver.ThreadPoolManager;
import com.l2jserver.gameserver.data.sql.impl.CharNameTable;
import com.l2jserver.gameserver.data.sql.impl.ClanTable;
import com.l2jserver.gameserver.data.xml.impl.SecondaryAuthData;
import com.l2jserver.gameserver.instancemanager.AntiFeedManager;
import com.l2jserver.gameserver.model.CharSelectInfoPackage;
import com.l2jserver.gameserver.model.L2Clan;
import com.l2jserver.gameserver.model.L2World;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.entity.L2Event;
import com.l2jserver.gameserver.model.olympiad.OlympiadManager;
import com.l2jserver.gameserver.model.zone.ZoneId;
import com.l2jserver.gameserver.network.serverpackets.ActionFailed;
import com.l2jserver.gameserver.network.serverpackets.L2GameServerPacket;
import com.l2jserver.gameserver.network.serverpackets.ServerClose;
import com.l2jserver.gameserver.security.SecondaryPasswordAuth;
import com.l2jserver.gameserver.util.FloodProtectors;
import com.l2jserver.gameserver.util.Util;
import com.l2jserver.mmocore.MMOClient;
import com.l2jserver.mmocore.MMOConnection;
import com.l2jserver.mmocore.ReceivablePacket;

/**
 * Represents a client connected on Game Server.
 * @author KenM
 */
public final class L2GameClient extends MMOClient<MMOConnection<L2GameClient>> implements Runnable {
	
	private static final Logger LOG = LoggerFactory.getLogger(L2GameClient.class);
	
	private static final Logger LOG_ACCOUNTING = LoggerFactory.getLogger("accounting");
	
	private static final Logger LOG_AUDIT = LoggerFactory.getLogger("audit");
	
	/**
	 * @author KenM
	 */
	public static enum GameClientState {
		/** Client has just connected . */
		CONNECTED,
		/** Client has authed but doesn't has character attached to it yet. */
		AUTHED,
		/** Client has selected a character, but it hasn't joined the server yet. */
		JOINING,
		/** Client has selected a char and is in game. */
		IN_GAME
	}
	
	private GameClientState _state;
	
	// Info
	private final InetAddress _addr;
	private String _accountName;
	private SessionKey _sessionId;
	private L2PcInstance _activeChar;
	private final ReentrantLock _activeCharLock = new ReentrantLock();
	private SecondaryPasswordAuth _secondaryAuth;
	
	private boolean _isAuthedGG;
	private final long _connectionStartTime;
	private List<CharSelectInfoPackage> _charSlotMapping = null;
	
	// flood protectors
	private final FloodProtectors _floodProtectors = new FloodProtectors(this);
	
	// Task
	protected final ScheduledFuture<?> _autoSaveInDB;
	protected ScheduledFuture<?> _cleanupTask = null;
	
	private L2GameServerPacket _aditionalClosePacket;
	
	// Crypt
	private final GameCrypt _crypt;
	
	private final ClientStats _stats;
	
	private boolean _isDetached = false;
	
	private boolean _protocol;
	
	private final ArrayBlockingQueue<ReceivablePacket<L2GameClient>> _packetQueue;
	
	private final ReentrantLock _queueLock = new ReentrantLock();
	
	private int[][] trace;
	
	public L2GameClient(MMOConnection<L2GameClient> con) {
		super(con);
		_state = GameClientState.CONNECTED;
		_connectionStartTime = System.currentTimeMillis();
		_crypt = new GameCrypt();
		_stats = new ClientStats();
		
		final var capacity = Math.max(general().getClientPacketQueueSize(), mmo().getMaxReadPerPass() + 2);
		_packetQueue = new ArrayBlockingQueue<>(capacity);
		
		if (general().getCharacterDataStoreInterval() > 0) {
			_autoSaveInDB = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new AutoSaveTask(), 300000L, general().getCharacterDataStoreInterval());
		} else {
			_autoSaveInDB = null;
		}
		
		try {
			_addr = con != null ? con.getInetAddress() : InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			throw new Error("Unable to determine localhost address.");
		}
	}
	
	public byte[] enableCrypt() {
		byte[] key = BlowFishKeygen.getRandomKey();
		_crypt.setKey(key);
		return key;
	}
	
	public GameClientState getState() {
		return _state;
	}
	
	public void setState(GameClientState pState) {
		if (_state != pState) {
			_state = pState;
			_packetQueue.clear();
		}
	}
	
	public ClientStats getStats() {
		return _stats;
	}
	
	/**
	 * For loaded offline traders returns localhost address.
	 * @return cached connection IP address, for checking detached clients.
	 */
	public InetAddress getConnectionAddress() {
		return _addr;
	}
	
	public long getConnectionStartTime() {
		return _connectionStartTime;
	}
	
	@Override
	public boolean decrypt(ByteBuffer buf, int size) {
		_crypt.decrypt(buf.array(), buf.position(), size);
		return true;
	}
	
	@Override
	public boolean encrypt(final ByteBuffer buf, final int size) {
		_crypt.encrypt(buf.array(), buf.position(), size);
		buf.position(buf.position() + size);
		return true;
	}
	
	public L2PcInstance getActiveChar() {
		return _activeChar;
	}
	
	public void setActiveChar(L2PcInstance pActiveChar) {
		_activeChar = pActiveChar;
	}
	
	public ReentrantLock getActiveCharLock() {
		return _activeCharLock;
	}
	
	public FloodProtectors getFloodProtectors() {
		return _floodProtectors;
	}
	
	public void setGameGuardOk(boolean val) {
		_isAuthedGG = val;
	}
	
	public boolean isAuthedGG() {
		return _isAuthedGG;
	}
	
	public void setAccountName(String pAccountName) {
		_accountName = pAccountName;
		
		if (SecondaryAuthData.getInstance().isEnabled()) {
			_secondaryAuth = new SecondaryPasswordAuth(this);
		}
	}
	
	public String getAccountName() {
		return _accountName;
	}
	
	public void setSessionId(SessionKey sk) {
		_sessionId = sk;
	}
	
	public SessionKey getSessionId() {
		return _sessionId;
	}
	
	public void sendPacket(L2GameServerPacket gsp) {
		if (_isDetached || (gsp == null)) {
			return;
		}
		
		// Packets from invisible chars sends only to GMs
		if (gsp.isInvisible() && (getActiveChar() != null) && !getActiveChar().canOverrideCond(SEE_ALL_PLAYERS)) {
			return;
		}
		
		getConnection().sendPacket(gsp);
		gsp.runImpl();
	}
	
	public boolean isDetached() {
		return _isDetached;
	}
	
	public void setDetached(boolean b) {
		_isDetached = b;
	}
	
	/**
	 * Method to handle character deletion
	 * @param charslot
	 * @return a byte:
	 *         <li>-1: Error: No char was found for such charslot, caught exception, etc...
	 *         <li>0: character is not member of any clan, proceed with deletion
	 *         <li>1: character is member of a clan, but not clan leader
	 *         <li>2: character is clan leader
	 */
	public byte markToDeleteChar(int charslot) {
		int objid = getObjectIdForSlot(charslot);
		
		if (objid < 0) {
			return -1;
		}
		
		try (var con = ConnectionFactory.getInstance().getConnection();
			var ps = con.prepareStatement("SELECT clanId FROM characters WHERE charId=?")) {
			ps.setInt(1, objid);
			byte answer = 0;
			try (var rs = ps.executeQuery()) {
				int clanId = rs.next() ? rs.getInt(1) : 0;
				if (clanId != 0) {
					L2Clan clan = ClanTable.getInstance().getClan(clanId);
					
					if (clan == null) {
						answer = 0; // jeezes!
					} else if (clan.getLeaderId() == objid) {
						answer = 2;
					} else {
						answer = 1;
					}
				}
				
				// Setting delete time
				if (answer == 0) {
					if (character().getDeleteCharAfterDays() == 0) {
						deleteCharByObjId(objid);
					} else {
						try (var ps2 = con.prepareStatement("UPDATE characters SET deletetime=? WHERE charId=?")) {
							ps2.setLong(1, System.currentTimeMillis() + DAYS.toMillis(character().getDeleteCharAfterDays()));
							ps2.setInt(2, objid);
							ps2.execute();
						}
					}
					LOG_ACCOUNTING.info("Deleted character Id {} and client {}.", objid, this);
				}
			}
			return answer;
		} catch (Exception ex) {
			LOG.error("There has been an error updating delete time of character!", ex);
			return -1;
		}
	}
	
	/**
	 * Save the L2PcInstance to the database.
	 */
	public void saveCharToDisk() {
		try {
			if (getActiveChar() != null) {
				getActiveChar().storeMe();
				getActiveChar().storeRecommendations();
				if (general().updateItemsOnCharStore()) {
					getActiveChar().getInventory().updateDatabase();
					getActiveChar().getWarehouse().updateDatabase();
				}
			}
		} catch (Exception ex) {
			LOG.error("There has been an error saving character!", ex);
		}
	}
	
	public void markRestoredChar(int charslot) {
		final int objid = getObjectIdForSlot(charslot);
		if (objid < 0) {
			return;
		}
		
		try (var con = ConnectionFactory.getInstance().getConnection();
			var ps = con.prepareStatement("UPDATE characters SET deletetime=0 WHERE charId=?")) {
			ps.setInt(1, objid);
			ps.execute();
		} catch (Exception ex) {
			LOG.error("There has been an error restoring character!", ex);
		}
		
		LOG_ACCOUNTING.info("Restoring Character Id {}, client {}.", objid, this);
	}
	
	public static void deleteCharByObjId(int objid) {
		if (objid < 0) {
			return;
		}
		
		CharNameTable.getInstance().removeName(objid);
		
		try (var con = ConnectionFactory.getInstance().getConnection()) {
			try (var ps = con.prepareStatement("DELETE FROM character_contacts WHERE charId=? OR contactId=?")) {
				ps.setInt(1, objid);
				ps.setInt(2, objid);
				ps.execute();
			}
			
			try (var ps = con.prepareStatement("DELETE FROM character_friends WHERE charId=? OR friendId=?")) {
				ps.setInt(1, objid);
				ps.setInt(2, objid);
				ps.execute();
			}
			
			try (var ps = con.prepareStatement("DELETE FROM character_hennas WHERE charId=?")) {
				ps.setInt(1, objid);
				ps.execute();
			}
			
			try (var ps = con.prepareStatement("DELETE FROM character_macroses WHERE charId=?")) {
				ps.setInt(1, objid);
				ps.execute();
			}
			
			try (var ps = con.prepareStatement("DELETE FROM character_quests WHERE charId=?")) {
				ps.setInt(1, objid);
				ps.execute();
			}
			
			try (var ps = con.prepareStatement("DELETE FROM character_quest_global_data WHERE charId=?")) {
				ps.setInt(1, objid);
				ps.executeUpdate();
			}
			
			try (var ps = con.prepareStatement("DELETE FROM character_recipebook WHERE charId=?")) {
				ps.setInt(1, objid);
				ps.execute();
			}
			
			try (var ps = con.prepareStatement("DELETE FROM character_shortcuts WHERE charId=?")) {
				ps.setInt(1, objid);
				ps.execute();
			}
			
			try (var ps = con.prepareStatement("DELETE FROM character_skills WHERE charId=?")) {
				ps.setInt(1, objid);
				ps.execute();
			}
			
			try (var ps = con.prepareStatement("DELETE FROM character_skills_save WHERE charId=?")) {
				ps.setInt(1, objid);
				ps.execute();
			}
			
			try (var ps = con.prepareStatement("DELETE FROM character_subclasses WHERE charId=?")) {
				ps.setInt(1, objid);
				ps.execute();
			}
			
			try (var ps = con.prepareStatement("DELETE FROM heroes WHERE charId=?")) {
				ps.setInt(1, objid);
				ps.execute();
			}
			
			try (var ps = con.prepareStatement("DELETE FROM olympiad_nobles WHERE charId=?")) {
				ps.setInt(1, objid);
				ps.execute();
			}
			
			try (var ps = con.prepareStatement("DELETE FROM seven_signs WHERE charId=?")) {
				ps.setInt(1, objid);
				ps.execute();
			}
			
			try (var ps = con.prepareStatement("DELETE FROM pets WHERE item_obj_id IN (SELECT object_id FROM items WHERE items.owner_id=?)")) {
				ps.setInt(1, objid);
				ps.execute();
			}
			
			try (var ps = con.prepareStatement("DELETE FROM item_attributes WHERE itemId IN (SELECT object_id FROM items WHERE items.owner_id=?)")) {
				ps.setInt(1, objid);
				ps.execute();
			}
			
			try (var ps = con.prepareStatement("DELETE FROM items WHERE owner_id=?")) {
				ps.setInt(1, objid);
				ps.execute();
			}
			
			try (var ps = con.prepareStatement("DELETE FROM merchant_lease WHERE player_id=?")) {
				ps.setInt(1, objid);
				ps.execute();
			}
			
			try (var ps = con.prepareStatement("DELETE FROM character_raid_points WHERE charId=?")) {
				ps.setInt(1, objid);
				ps.execute();
			}
			
			try (var ps = con.prepareStatement("DELETE FROM character_reco_bonus WHERE charId=?")) {
				ps.setInt(1, objid);
				ps.execute();
			}
			
			try (var ps = con.prepareStatement("DELETE FROM character_instance_time WHERE charId=?")) {
				ps.setInt(1, objid);
				ps.execute();
			}
			
			try (var ps = con.prepareStatement("DELETE FROM character_variables WHERE charId=?")) {
				ps.setInt(1, objid);
				ps.execute();
			}
			
			try (var ps = con.prepareStatement("DELETE FROM characters WHERE charId=?")) {
				ps.setInt(1, objid);
				ps.execute();
			}
			
			if (customs().allowWedding()) {
				try (var ps = con.prepareStatement("DELETE FROM mods_wedding WHERE player1Id = ? OR player2Id = ?")) {
					ps.setInt(1, objid);
					ps.setInt(2, objid);
					ps.execute();
				}
			}
		} catch (Exception ex) {
			LOG.warn("There has been an error deleting character Id {}!", objid, ex);
		}
	}
	
	public L2PcInstance loadCharFromDisk(int charslot) {
		final int objId = getObjectIdForSlot(charslot);
		if (objId < 0) {
			return null;
		}
		
		L2PcInstance character = L2World.getInstance().getPlayer(objId);
		if (character != null) {
			// exploit prevention, should not happens in normal way
			LOG.error("Attempt of double login {}, account {}!", character, getAccountName());
			if (character.getClient() != null) {
				character.getClient().closeNow();
			} else {
				character.deleteMe();
			}
			return null;
		}
		
		character = L2PcInstance.load(objId);
		if (character != null) {
			character.setRunning();
			character.standUp();
			character.refreshOverloaded();
			character.refreshExpertisePenalty();
			character.setOnlineStatus(true, false);
		} else {
			LOG.error("Could not restore in slot {}!", charslot);
		}
		return character;
	}
	
	public void setCharSelection(List<CharSelectInfoPackage> list) {
		_charSlotMapping = list;
	}
	
	public CharSelectInfoPackage getCharSelection(int charslot) {
		if ((_charSlotMapping == null) || (charslot < 0) || (charslot >= _charSlotMapping.size())) {
			return null;
		}
		return _charSlotMapping.get(charslot);
	}
	
	public SecondaryPasswordAuth getSecondaryAuth() {
		return _secondaryAuth;
	}
	
	public void close(L2GameServerPacket gsp) {
		if (getConnection() == null) {
			return; // ofline shop
		}
		if (_aditionalClosePacket != null) {
			getConnection().close(new L2GameServerPacket[] {
				_aditionalClosePacket,
				gsp
			});
		} else {
			getConnection().close(gsp);
		}
	}
	
	public void close(L2GameServerPacket[] gspArray) {
		if (getConnection() == null) {
			return; // ofline shop
		}
		getConnection().close(gspArray);
	}
	
	private int getObjectIdForSlot(int charSlot) {
		final CharSelectInfoPackage info = getCharSelection(charSlot);
		if (info == null) {
			LOG.warn("{} tried to delete character in slot {} but no characters exits at that slot!", this, charSlot);
			return -1;
		}
		return info.getObjectId();
	}
	
	@Override
	protected void onForcedDisconnection() {
		LOG_ACCOUNTING.warn("Client {} disconnected abnormally!", this);
	}
	
	@Override
	protected void onDisconnection() {
		// no long running tasks here, do it async
		try {
			ThreadPoolManager.getInstance().executeGeneral(new DisconnectTask(this));
		} catch (RejectedExecutionException e) {
			// server is closing
		}
	}
	
	/**
	 * Close client connection with {@link ServerClose} packet
	 */
	public void closeNow() {
		_isDetached = true; // prevents more packets execution
		close(ServerClose.STATIC_PACKET);
		synchronized (this) {
			if (_cleanupTask != null) {
				cancelCleanup();
			}
			_cleanupTask = ThreadPoolManager.getInstance().scheduleGeneral(new CleanupTask(), 0); // instant
		}
	}
	
	/**
	 * Produces the best possible string representation of this client.
	 */
	@Override
	public String toString() {
		try {
			final InetAddress address = getConnection().getInetAddress();
			switch (getState()) {
				case CONNECTED:
					return "[IP: " + (address == null ? "disconnected" : address.getHostAddress()) + "]";
				case AUTHED:
					return "[Account: " + getAccountName() + " - IP: " + (address == null ? "disconnected" : address.getHostAddress()) + "]";
				case JOINING:
				case IN_GAME:
					return "[Character: " + (getActiveChar() == null ? "disconnected" : getActiveChar().getName() + "[" + getActiveChar().getObjectId() + "]") + " - Account: " + getAccountName() + " - IP: " + (address == null ? "disconnected" : address.getHostAddress()) + "]";
				default:
					throw new IllegalStateException("Missing state on switch");
			}
		} catch (NullPointerException e) {
			return "[Character read failed due to disconnect]";
		}
	}
	
	protected class DisconnectTask implements Runnable {
		
		private final L2GameClient client;
		
		public DisconnectTask(L2GameClient client) {
			this.client = client;
		}
		
		@Override
		public void run() {
			boolean fast = true;
			try {
				final L2PcInstance player = client.getActiveChar();
				if ((player != null) && !isDetached()) {
					setDetached(true);
					if (offlineMode(player)) {
						player.leaveParty();
						OlympiadManager.getInstance().unRegisterNoble(player);
						
						// If the L2PcInstance has Pet, unsummon it
						if (player.hasSummon()) {
							player.getSummon().setRestoreSummon(true);
							
							player.getSummon().unSummon(player);
							// Dead pet wasn't unsummoned, broadcast npcinfo changes (pet will be without owner name - means owner offline)
							if (player.getSummon() != null) {
								player.getSummon().broadcastNpcInfo(0);
							}
						}
						
						if (customs().offlineSetNameColor()) {
							player.getAppearance().setNameColor(customs().getOfflineNameColor());
							player.broadcastUserInfo();
						}
						
						if (player.getOfflineStartTime() == 0) {
							player.setOfflineStartTime(System.currentTimeMillis());
						}
						LOG_ACCOUNTING.info("Client {} entering offline mode.", client);
						return;
					}
					fast = !player.isInCombat() && !player.isLocked();
				}
				cleanMe(fast);
			} catch (Exception ex) {
				LOG.warn("There has been an error while disconnecting client {}!", client, ex);
			}
		}
	}
	
	/**
	 * @param player the player to be check.
	 * @return {@code true} if the player is allowed to remain as off-line shop.
	 */
	protected boolean offlineMode(L2PcInstance player) {
		if (player.isInOlympiadMode() || player.isFestivalParticipant() || player.isBlockedFromExit() || player.isJailed() || (player.getVehicle() != null)) {
			return false;
		}
		
		boolean canSetShop = false;
		switch (player.getPrivateStoreType()) {
			case SELL:
			case PACKAGE_SELL:
			case BUY: {
				canSetShop = customs().offlineTradeEnable();
				break;
			}
			case MANUFACTURE: {
				canSetShop = customs().offlineTradeEnable();
				break;
			}
			default: {
				canSetShop = customs().offlineCraftEnable() && player.isInCraftMode();
				break;
			}
		}
		
		if (customs().offlineModeInPeaceZone() && !player.isInsideZone(ZoneId.PEACE)) {
			canSetShop = false;
		}
		return canSetShop;
	}
	
	public void cleanMe(boolean fast) {
		try {
			synchronized (this) {
				if (_cleanupTask == null) {
					_cleanupTask = ThreadPoolManager.getInstance().scheduleGeneral(new CleanupTask(), fast ? 5 : 15000L);
				}
			}
		} catch (Exception ex) {
			LOG.warn("There has been an error during cleanup!", ex);
		}
	}
	
	protected class CleanupTask implements Runnable {
		@Override
		public void run() {
			try {
				// we are going to manually save the char bellow thus we can force the cancel
				if (_autoSaveInDB != null) {
					_autoSaveInDB.cancel(true);
					// ThreadPoolManager.getInstance().removeGeneral((Runnable) _autoSaveInDB);
				}
				
				// this should only happen on connection loss
				if (getActiveChar() != null) {
					if (getActiveChar().isLocked()) {
						LOG.warn("Player {} still performing subclass actions during disconnect!", getActiveChar());
					}
					
					// we store all data from players who are disconnected while in an event in order to restore it in the next login
					if (L2Event.isParticipant(getActiveChar())) {
						L2Event.savePlayerEventStatus(getActiveChar());
					}
					
					// prevent closing again
					getActiveChar().setClient(null);
					
					if (getActiveChar().isOnline()) {
						getActiveChar().deleteMe();
						AntiFeedManager.getInstance().onDisconnect(L2GameClient.this);
					}
				}
				setActiveChar(null);
			} catch (Exception ex) {
				LOG.warn("There has been  an error while cleanup client!", ex);
			} finally {
				LoginServerThread.getInstance().sendLogout(getAccountName());
			}
		}
	}
	
	protected class AutoSaveTask implements Runnable {
		@Override
		public void run() {
			try {
				final var player = getActiveChar();
				// Safety precaution.
				if ((player != null) && player.isOnline()) {
					saveCharToDisk();
					if (player.hasSummon()) {
						player.getSummon().storeMe();
					}
				}
			} catch (Exception ex) {
				LOG.error("There has been an error on AutoSaveTask!", ex);
			}
		}
	}
	
	public boolean isProtocolOk() {
		return _protocol;
	}
	
	public void setProtocolOk(boolean b) {
		_protocol = b;
	}
	
	public boolean handleCheat(String punishment) {
		if (_activeChar != null) {
			Util.handleIllegalPlayerAction(_activeChar, toString() + ": " + punishment);
			return true;
		}
		
		LOG_AUDIT.warn("Client {} kicked for {}!", this, punishment);
		closeNow();
		return false;
	}
	
	/**
	 * True if detached, or flood detected, or queue overflow detected and queue still not empty.
	 * @return false if client can receive packets.
	 */
	public boolean dropPacket() {
		if (_isDetached) {
			return true;
		}
		
		// Flood protection.
		if (getStats().countPacket(_packetQueue.size())) {
			sendPacket(ActionFailed.STATIC_PACKET);
			return true;
		}
		
		return getStats().dropPacket();
	}
	
	/**
	 * Counts buffer underflow exceptions.
	 */
	public void onBufferUnderflow() {
		if (getStats().countUnderflowException()) {
			LOG.error("Client {} disconnected, too many buffer underflow exceptions!", this);
			closeNow();
			return;
		}
		
		// In CONNECTED state kick client immediately.
		if (_state == GameClientState.CONNECTED) {
			if (general().packetHandlerDebug()) {
				LOG.error("Client {} disconnected, too many buffer underflows in non-authed state!", this);
			}
			closeNow();
		}
	}
	
	/**
	 * Counts unknown packets
	 */
	public void onUnknownPacket() {
		if (getStats().countUnknownPacket()) {
			LOG.error("Client {} disconnected, too many unknown packets!", this);
			closeNow();
			return;
		}
		
		// In CONNECTED state kick client immediately.
		if (_state == GameClientState.CONNECTED) {
			if (general().packetHandlerDebug()) {
				LOG.error("Client {} disconnected, too many unknown packets in non-authed state!", this);
			}
			closeNow();
		}
	}
	
	/**
	 * Add packet to the queue and start worker thread if needed
	 * @param packet
	 */
	public void execute(ReceivablePacket<L2GameClient> packet) {
		if (getStats().countFloods()) {
			LOG.error("Client {} disconnected, too many floods {} long and {} short!", this, getStats().longFloods, getStats().shortFloods);
			closeNow();
			return;
		}
		
		if (!_packetQueue.offer(packet)) {
			if (getStats().countQueueOverflow()) {
				LOG.error("Client {} disconnected, too many queue overflows!", this);
				closeNow();
			} else {
				sendPacket(ActionFailed.STATIC_PACKET);
			}
			return;
		}
		
		if (_queueLock.isLocked()) {
			return;
		}
		
		try {
			if (_state == GameClientState.CONNECTED) {
				if (getStats().processedPackets > 3) {
					if (general().packetHandlerDebug()) {
						LOG.error("Client {} disconnected, too many packets in non-authed state!", this);
					}
					closeNow();
					return;
				}
				ThreadPoolManager.getInstance().executeIOPacket(this);
			} else {
				ThreadPoolManager.getInstance().executePacket(this);
			}
		} catch (RejectedExecutionException ex) {
			// If the server is shutdown we ignore.
			if (!ThreadPoolManager.getInstance().isShutdown()) {
				LOG.error("Failed executing {}, for client {}!", packet.getClass().getSimpleName(), toString(), ex);
			}
		}
	}
	
	@Override
	public void run() {
		if (!_queueLock.tryLock()) {
			return;
		}
		
		try {
			int count = 0;
			ReceivablePacket<L2GameClient> packet;
			while (true) {
				packet = _packetQueue.poll();
				if (packet == null) {
					return;
				}
				
				// Clear queue immediately after detach.
				if (_isDetached) {
					_packetQueue.clear();
					return;
				}
				
				try {
					packet.run();
				} catch (Exception ex) {
					LOG.error("Exception during execution {}, client {}!", packet.getClass().getSimpleName(), this, ex);
				}
				
				count++;
				if (getStats().countBurst(count)) {
					return;
				}
			}
		} finally {
			_queueLock.unlock();
		}
	}
	
	public void setClientTracert(int[][] tracert) {
		trace = tracert;
	}
	
	public int[][] getTrace() {
		return trace;
	}
	
	private boolean cancelCleanup() {
		final Future<?> task = _cleanupTask;
		if (task != null) {
			_cleanupTask = null;
			return task.cancel(true);
		}
		return false;
	}
	
	public void setAditionalClosePacket(L2GameServerPacket aditionalClosePacket) {
		_aditionalClosePacket = aditionalClosePacket;
	}
}
