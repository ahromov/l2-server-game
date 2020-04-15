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
package com.l2jserver.gameserver.model.entity;

import static com.l2jserver.gameserver.config.Configuration.tvt;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.l2jserver.commons.util.Rnd;
import com.l2jserver.gameserver.cache.HtmCache;
import com.l2jserver.gameserver.data.xml.impl.DoorData;
import com.l2jserver.gameserver.datatables.ItemTable;
import com.l2jserver.gameserver.datatables.SpawnTable;
import com.l2jserver.gameserver.instancemanager.AntiFeedManager;
import com.l2jserver.gameserver.instancemanager.InstanceManager;
import com.l2jserver.gameserver.model.L2Spawn;
import com.l2jserver.gameserver.model.L2World;
import com.l2jserver.gameserver.model.Location;
import com.l2jserver.gameserver.model.actor.L2Character;
import com.l2jserver.gameserver.model.actor.L2Npc;
import com.l2jserver.gameserver.model.actor.L2Summon;
import com.l2jserver.gameserver.model.actor.instance.L2DoorInstance;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.actor.instance.L2PetInstance;
import com.l2jserver.gameserver.model.actor.instance.L2ServitorInstance;
import com.l2jserver.gameserver.model.events.EventDispatcher;
import com.l2jserver.gameserver.model.events.impl.events.OnTvTEventFinish;
import com.l2jserver.gameserver.model.events.impl.events.OnTvTEventKill;
import com.l2jserver.gameserver.model.events.impl.events.OnTvTEventRegistrationStart;
import com.l2jserver.gameserver.model.events.impl.events.OnTvTEventStart;
import com.l2jserver.gameserver.model.holders.ItemHolder;
import com.l2jserver.gameserver.model.holders.SkillHolder;
import com.l2jserver.gameserver.model.skills.Skill;
import com.l2jserver.gameserver.network.clientpackets.Say2;
import com.l2jserver.gameserver.network.serverpackets.CreatureSay;
import com.l2jserver.gameserver.network.serverpackets.MagicSkillUse;
import com.l2jserver.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2jserver.gameserver.util.StringUtil;

/**
 * TvT Event.
 * @author HorridoJoho
 */
public class TvTEvent {
	enum EventState {
		INACTIVE,
		INACTIVATING,
		PARTICIPATING,
		STARTING,
		STARTED,
		REWARDING
	}
	
	protected static final Logger _log = Logger.getLogger(TvTEvent.class.getName());
	/** html path **/
	private static final String HTML_PATH = "com/l2jserver/datapack/custom/events/TvT/TvTManager/";
	/** The teams of the TvTEvent. */
	private static TvTEventTeam[] _teams = new TvTEventTeam[2];
	/** The state of the TvTEvent. */
	private static EventState _state = EventState.INACTIVE;
	/** The spawn of the participation npc. */
	private static L2Spawn _npcSpawn = null;
	/** The npc instance of the participation npc. */
	private static L2Npc _lastNpcSpawn = null;
	/** Instance Id. */
	private static int _TvTEventInstance = 0;
	
	private TvTEvent() {
		// Prevent external initialization.
	}
	
	/** Teams initializing. */
	public static void init() {
		AntiFeedManager.getInstance().registerEvent(AntiFeedManager.TVT_ID);
		_teams[0] = new TvTEventTeam(tvt().getTeam1Name(), tvt().getTeam1Loc());
		_teams[1] = new TvTEventTeam(tvt().getTeam2Name(), tvt().getTeam2Loc());
	}
	
	/**
	 * Starts the participation of the TvTEvent<br>
	 * 1. Get L2NpcTemplate by ParticipationNpcId<br>
	 * 2. Try to spawn a new npc of it
	 * @return true if success, otherwise false
	 */
	public static boolean startParticipation() {
		try {
			_npcSpawn = new L2Spawn(tvt().getParticipationNpcId());
			_npcSpawn.setLocation(tvt().getParticipationNpcLoc());
			_npcSpawn.setAmount(1);
			_npcSpawn.setRespawnDelay(1);
			// later no need to delete spawn from db, we don't store it (false)
			SpawnTable.getInstance().addNewSpawn(_npcSpawn, false);
			_npcSpawn.init();
			_lastNpcSpawn = _npcSpawn.getLastSpawn();
			_lastNpcSpawn.setCurrentHp(_lastNpcSpawn.getMaxHp());
			_lastNpcSpawn.setTitle("TvT Event Participation");
			_lastNpcSpawn.isAggressive();
			_lastNpcSpawn.decayMe();
			_lastNpcSpawn.spawnMe(_npcSpawn.getLastSpawn().getX(), _npcSpawn.getLastSpawn().getY(), _npcSpawn.getLastSpawn().getZ());
			_lastNpcSpawn.broadcastPacket(new MagicSkillUse(_lastNpcSpawn, _lastNpcSpawn, 1034, 1, 1, 1));
		} catch (Exception e) {
			_log.log(Level.WARNING, "TvTEventEngine[TvTEvent.startParticipation()]: exception: " + e.getMessage(), e);
			return false;
		}
		
		setState(EventState.PARTICIPATING);
		EventDispatcher.getInstance().notifyEventAsync(new OnTvTEventRegistrationStart());
		return true;
	}
	
	private static int highestLevelPcInstanceOf(Map<Integer, L2PcInstance> players) {
		int maxLevel = Integer.MIN_VALUE, maxLevelId = -1;
		for (L2PcInstance player : players.values()) {
			if (player.getLevel() >= maxLevel) {
				maxLevel = player.getLevel();
				maxLevelId = player.getObjectId();
			}
		}
		return maxLevelId;
	}
	
	/**
	 * Starts the TvTEvent fight<br>
	 * 1. Set state EventState.STARTING<br>
	 * 2. Close doors specified in configs<br>
	 * 3. Abort if not enough participants(return false)<br>
	 * 4. Set state EventState.STARTED<br>
	 * 5. Teleport all participants to team spot
	 * @return true if success, otherwise false
	 */
	public static boolean startFight() {
		// Set state to STARTING
		setState(EventState.STARTING);
		
		// Randomize and balance team distribution
		Map<Integer, L2PcInstance> allParticipants = new HashMap<>();
		allParticipants.putAll(_teams[0].getParticipatedPlayers());
		allParticipants.putAll(_teams[1].getParticipatedPlayers());
		_teams[0].cleanMe();
		_teams[1].cleanMe();
		
		L2PcInstance player;
		Iterator<L2PcInstance> iter;
		if (needParticipationFee()) {
			iter = allParticipants.values().iterator();
			while (iter.hasNext()) {
				player = iter.next();
				if (!hasParticipationFee(player)) {
					iter.remove();
				}
			}
		}
		
		int balance[] = {
			0,
			0
		}, priority = 0, highestLevelPlayerId;
		L2PcInstance highestLevelPlayer;
		// TODO: allParticipants should be sorted by level instead of using highestLevelPcInstanceOf for every fetch
		while (!allParticipants.isEmpty()) {
			// Priority team gets one player
			highestLevelPlayerId = highestLevelPcInstanceOf(allParticipants);
			highestLevelPlayer = allParticipants.get(highestLevelPlayerId);
			allParticipants.remove(highestLevelPlayerId);
			_teams[priority].addPlayer(highestLevelPlayer);
			balance[priority] += highestLevelPlayer.getLevel();
			// Exiting if no more players
			if (allParticipants.isEmpty()) {
				break;
			}
			// The other team gets one player
			// TODO: Code not dry
			priority = 1 - priority;
			highestLevelPlayerId = highestLevelPcInstanceOf(allParticipants);
			highestLevelPlayer = allParticipants.get(highestLevelPlayerId);
			allParticipants.remove(highestLevelPlayerId);
			_teams[priority].addPlayer(highestLevelPlayer);
			balance[priority] += highestLevelPlayer.getLevel();
			// Recalculating priority
			priority = balance[0] > balance[1] ? 1 : 0;
		}
		
		// Check for enough participants
		if ((_teams[0].getParticipatedPlayerCount() < tvt().getMinPlayersInTeams()) || (_teams[1].getParticipatedPlayerCount() < tvt().getMinPlayersInTeams())) {
			// Set state INACTIVE
			setState(EventState.INACTIVE);
			// Cleanup of teams
			_teams[0].cleanMe();
			_teams[1].cleanMe();
			// Unspawn the event NPC
			unSpawnNpc();
			AntiFeedManager.getInstance().clear(AntiFeedManager.TVT_ID);
			return false;
		}
		
		if (needParticipationFee()) {
			iter = _teams[0].getParticipatedPlayers().values().iterator();
			while (iter.hasNext()) {
				player = iter.next();
				if (!payParticipationFee(player)) {
					iter.remove();
				}
			}
			iter = _teams[1].getParticipatedPlayers().values().iterator();
			while (iter.hasNext()) {
				player = iter.next();
				if (!payParticipationFee(player)) {
					iter.remove();
				}
			}
		}
		
		if (tvt().instanced()) {
			try {
				_TvTEventInstance = InstanceManager.getInstance().createDynamicInstance(tvt().getInstanceFile());
				InstanceManager.getInstance().getInstance(_TvTEventInstance).setAllowSummon(false);
				InstanceManager.getInstance().getInstance(_TvTEventInstance).setPvPInstance(true);
				InstanceManager.getInstance().getInstance(_TvTEventInstance).setEmptyDestroyTime(tvt().getStartLeaveTeleportDelay() + 60000L);
			} catch (Exception e) {
				_TvTEventInstance = 0;
				_log.log(Level.WARNING, "TvTEventEngine[TvTEvent.createDynamicInstance]: exception: " + e.getMessage(), e);
			}
		}
		
		// Opens all doors
		openDoors(tvt().getDoorsToOpen());
		// Closes all doors
		closeDoors(tvt().getDoorsToClose());
		// Set state STARTED
		setState(EventState.STARTED);
		
		// Iterate over all teams
		for (TvTEventTeam team : _teams) {
			// Iterate over all participated player instances in this team
			for (L2PcInstance playerInstance : team.getParticipatedPlayers().values()) {
				if (playerInstance != null) {
					// Disable player revival.
					playerInstance.setCanRevive(false);
					// Teleporter implements Runnable and starts itself
					new TvTEventTeleporter(playerInstance, team.getLocation(), false, false);
				}
			}
		}
		
		// Notify to scripts.
		EventDispatcher.getInstance().notifyEventAsync(new OnTvTEventStart());
		return true;
	}
	
	/**
	 * Calculates the TvTEvent reward<br>
	 * 1. If both teams are at a tie(points equals), send it as system message to all participants, if one of the teams have 0 participants left online abort rewarding<br>
	 * 2. Wait till teams are not at a tie anymore<br>
	 * 3. Set state EvcentState.REWARDING<br>
	 * 4. Reward team with more points<br>
	 * 5. Show win html to wining team participants
	 * @return winning team name
	 */
	public static String calculateRewards() {
		if (_teams[0].getPoints() == _teams[1].getPoints()) {
			// Check if one of the teams have no more players left
			if ((_teams[0].getParticipatedPlayerCount() == 0) || (_teams[1].getParticipatedPlayerCount() == 0)) {
				// set state to rewarding
				setState(EventState.REWARDING);
				// return here, the fight can't be completed
				return "TvT Event: Event has ended. No team won due to inactivity!";
			}
			
			// Both teams have equals points
			sysMsgToAllParticipants("TvT Event: Event has ended, both teams have tied.");
			if (tvt().rewardTeamTie()) {
				rewardTeam(_teams[0]);
				rewardTeam(_teams[1]);
				return "TvT Event: Event has ended with both teams tying.";
			}
			return "TvT Event: Event has ended with both teams tying.";
		}
		
		// Set state REWARDING so nobody can point anymore
		setState(EventState.REWARDING);
		
		// Get team which has more points
		TvTEventTeam team = _teams[_teams[0].getPoints() > _teams[1].getPoints() ? 0 : 1];
		rewardTeam(team);
		
		// Notify to scripts.
		EventDispatcher.getInstance().notifyEventAsync(new OnTvTEventFinish());
		return "TvT Event: Event finish. Team " + team.getName() + " won with " + team.getPoints() + " kills.";
	}
	
	private static void rewardTeam(TvTEventTeam team) {
		for (L2PcInstance player : team.getParticipatedPlayers().values()) {
			if (player == null) {
				continue;
			}
			
			for (ItemHolder item : tvt().getReward()) {
				player.addItem("TvT Reward", item, null, true);
			}
			
			final NpcHtmlMessage npcHtmlMessage = new NpcHtmlMessage();
			npcHtmlMessage.setHtml(HtmCache.getInstance().getHtm(player.getHtmlPrefix(), HTML_PATH + "Reward.html"));
			player.sendPacket(npcHtmlMessage);
		}
	}
	
	/**
	 * Stops the TvTEvent fight<br>
	 * 1. Set state EventState.INACTIVATING<br>
	 * 2. Remove tvt npc from world<br>
	 * 3. Open doors specified in configs<br>
	 * 4. Teleport all participants back to participation npc location<br>
	 * 5. Teams cleaning<br>
	 * 6. Set state EventState.INACTIVE
	 */
	public static void stopFight() {
		// Set state INACTIVATING
		setState(EventState.INACTIVATING);
		// Unspawn event npc
		unSpawnNpc();
		// Opens all doors
		openDoors(tvt().getDoorsToClose());
		// Closes all doors
		closeDoors(tvt().getDoorsToOpen());
		
		for (TvTEventTeam team : _teams) {
			for (L2PcInstance player : team.getParticipatedPlayers().values()) {
				if (player == null) {
					continue;
				}
				
				// Enable player revival.
				player.setCanRevive(true);
				// Teleport back.
				new TvTEventTeleporter(player, tvt().getParticipationNpcLoc(), false, false);
			}
		}
		
		// Cleanup of teams
		_teams[0].cleanMe();
		_teams[1].cleanMe();
		// Set state INACTIVE
		setState(EventState.INACTIVE);
		AntiFeedManager.getInstance().clear(AntiFeedManager.TVT_ID);
	}
	
	/**
	 * Adds a player to a TvTEvent team<br>
	 * 1. Calculate the id of the team in which the player should be added<br>
	 * 2. Add the player to the calculated team
	 * @param playerInstance as L2PcInstance
	 * @return boolean: true if success, otherwise false
	 */
	public static synchronized boolean addParticipant(L2PcInstance playerInstance) {
		if (playerInstance == null) {
			return false;
		}
		
		byte teamId = 0;
		
		// Check to which team the player should be added
		if (_teams[0].getParticipatedPlayerCount() == _teams[1].getParticipatedPlayerCount()) {
			teamId = (byte) (Rnd.get(2));
		} else {
			teamId = (byte) (_teams[0].getParticipatedPlayerCount() > _teams[1].getParticipatedPlayerCount() ? 1 : 0);
		}
		playerInstance.addEventListener(new TvTEventListener(playerInstance));
		return _teams[teamId].addPlayer(playerInstance);
	}
	
	/**
	 * Removes a TvTEvent player from it's team<br>
	 * 1. Get team id of the player<br>
	 * 2. Remove player from it's team
	 * @param playerObjectId
	 * @return true if success, otherwise false
	 */
	public static boolean removeParticipant(int playerObjectId) {
		// Get the teamId of the player
		byte teamId = getParticipantTeamId(playerObjectId);
		
		// Check if the player is participant
		if (teamId != -1) {
			// Remove the player from team
			_teams[teamId].removePlayer(playerObjectId);
			
			final L2PcInstance player = L2World.getInstance().getPlayer(playerObjectId);
			if (player != null) {
				player.removeEventListener(TvTEventListener.class);
			}
			return true;
		}
		
		return false;
	}
	
	public static boolean needParticipationFee() {
		return (tvt().getParticipationFee() != null) && (tvt().getParticipationFee().getId() > 0);
	}
	
	public static boolean hasParticipationFee(L2PcInstance player) {
		return player.getInventory().getInventoryItemCount(tvt().getParticipationFee().getId(), -1) >= tvt().getParticipationFee().getCount();
	}
	
	public static boolean payParticipationFee(L2PcInstance playerInstance) {
		return playerInstance.destroyItemByItemId("TvT Participation Fee", tvt().getParticipationFee().getId(), tvt().getParticipationFee().getCount(), _lastNpcSpawn, true);
	}
	
	public static String getParticipationFee() {
		int itemId = tvt().getParticipationFee().getId();
		long itemNum = tvt().getParticipationFee().getCount();
		
		if ((itemId == 0) || (itemNum == 0)) {
			return "-";
		}
		
		return StringUtil.concat(String.valueOf(itemNum), " ", ItemTable.getInstance().getTemplate(itemId).getName());
	}
	
	/**
	 * Send a SystemMessage to all participated players<br>
	 * 1. Send the message to all players of team number one<br>
	 * 2. Send the message to all players of team number two
	 * @param message the message
	 */
	public static void sysMsgToAllParticipants(String message) {
		for (L2PcInstance playerInstance : _teams[0].getParticipatedPlayers().values()) {
			if (playerInstance != null) {
				playerInstance.sendMessage(message);
			}
		}
		
		for (L2PcInstance playerInstance : _teams[1].getParticipatedPlayers().values()) {
			if (playerInstance != null) {
				playerInstance.sendMessage(message);
			}
		}
	}
	
	private static L2DoorInstance getDoor(int doorId) {
		L2DoorInstance door = null;
		if (_TvTEventInstance <= 0) {
			door = DoorData.getInstance().getDoor(doorId);
		} else {
			final Instance inst = InstanceManager.getInstance().getInstance(_TvTEventInstance);
			if (inst != null) {
				door = inst.getDoor(doorId);
			}
		}
		return door;
	}
	
	/**
	 * Close doors specified in configs
	 * @param doors
	 */
	private static void closeDoors(List<Integer> doors) {
		for (int doorId : doors) {
			final L2DoorInstance doorInstance = getDoor(doorId);
			if (doorInstance != null) {
				doorInstance.closeMe();
			}
		}
	}
	
	/**
	 * Open doors specified in configs
	 * @param doors
	 */
	private static void openDoors(List<Integer> doors) {
		for (int doorId : doors) {
			final L2DoorInstance doorInstance = getDoor(doorId);
			if (doorInstance != null) {
				doorInstance.openMe();
			}
		}
	}
	
	/**
	 * UnSpawns the TvTEvent npc
	 */
	private static void unSpawnNpc() {
		// Delete the npc
		_lastNpcSpawn.deleteMe();
		SpawnTable.getInstance().deleteSpawn(_lastNpcSpawn.getSpawn(), false);
		// Stop respawning of the npc
		_npcSpawn.stopRespawn();
		_npcSpawn = null;
		_lastNpcSpawn = null;
	}
	
	/**
	 * Called when a player logs in.
	 * @param playerInstance the player
	 */
	public static void onLogin(L2PcInstance playerInstance) {
		if ((playerInstance == null) || (!isStarting() && !isStarted())) {
			return;
		}
		
		byte teamId = getParticipantTeamId(playerInstance.getObjectId());
		
		if (teamId == -1) {
			return;
		}
		
		_teams[teamId].addPlayer(playerInstance);
		new TvTEventTeleporter(playerInstance, _teams[teamId].getLocation(), true, false);
	}
	
	/**
	 * Called when a player logs out.
	 * @param playerInstance the player
	 */
	public static void onLogout(L2PcInstance playerInstance) {
		if ((playerInstance != null) && (isStarting() || isStarted() || isParticipating())) {
			if (removeParticipant(playerInstance.getObjectId())) {
				final var loc = tvt().getParticipationNpcLoc();
				playerInstance.setXYZInvisible((loc.getX() + Rnd.get(101)) - 50, (loc.getY() + Rnd.get(101)) - 50, loc.getZ());
			}
		}
	}
	
	/**
	 * Called on every onAction in L2PcIstance.
	 * @param playerInstance
	 * @param targetedPlayerObjectId
	 * @return true if player is allowed to target, otherwise false
	 */
	public static boolean onAction(L2PcInstance playerInstance, int targetedPlayerObjectId) {
		if ((playerInstance == null) || !isStarted()) {
			return true;
		}
		
		if (playerInstance.isGM()) {
			return true;
		}
		
		byte playerTeamId = getParticipantTeamId(playerInstance.getObjectId());
		byte targetedPlayerTeamId = getParticipantTeamId(targetedPlayerObjectId);
		
		if (((playerTeamId != -1) && (targetedPlayerTeamId == -1)) || ((playerTeamId == -1) && (targetedPlayerTeamId != -1))) {
			return false;
		}
		
		if ((playerTeamId != -1) && (targetedPlayerTeamId != -1) && (playerTeamId == targetedPlayerTeamId) && //
			(playerInstance.getObjectId() != targetedPlayerObjectId) && !tvt().allowTargetTeamMember()) {
			return false;
		}
		return true;
	}
	
	/**
	 * Called on every scroll use.
	 * @param playerObjectId
	 * @return true if player is allowed to use scroll, otherwise false
	 */
	public static boolean onScrollUse(int playerObjectId) {
		if (!isStarted()) {
			return true;
		}
		
		if (isPlayerParticipant(playerObjectId) && !tvt().allowScroll()) {
			return false;
		}
		
		return true;
	}
	
	/**
	 * Called on every potion use.
	 * @param playerObjectId
	 * @return true if player is allowed to use potions, otherwise false
	 */
	public static boolean onPotionUse(int playerObjectId) {
		if (!isStarted()) {
			return true;
		}
		
		if (isPlayerParticipant(playerObjectId) && !tvt().allowPotion()) {
			return false;
		}
		
		return true;
	}
	
	/**
	 * Called on every escape use.
	 * @param playerObjectId
	 * @return true if player is not in tvt event, otherwise false
	 */
	public static boolean onEscapeUse(int playerObjectId) {
		if (!isStarted()) {
			return true;
		}
		
		if (isPlayerParticipant(playerObjectId)) {
			return false;
		}
		
		return true;
	}
	
	/**
	 * Called on every summon item use.
	 * @param playerObjectId
	 * @return true if player is allowed to summon by item, otherwise false
	 */
	public static boolean onItemSummon(int playerObjectId) {
		if (!isStarted()) {
			return true;
		}
		
		if (isPlayerParticipant(playerObjectId) && !tvt().allowTargetTeamMember()) {
			return false;
		}
		
		return true;
	}
	
	/**
	 * Is called when a player is killed.
	 * @param killerCharacter as L2Character
	 * @param killedPlayerInstance as L2PcInstance
	 */
	public static void onKill(L2Character killerCharacter, L2PcInstance killedPlayerInstance) {
		if ((killedPlayerInstance == null) || !isStarted()) {
			return;
		}
		
		byte killedTeamId = getParticipantTeamId(killedPlayerInstance.getObjectId());
		
		if (killedTeamId == -1) {
			return;
		}
		
		new TvTEventTeleporter(killedPlayerInstance, _teams[killedTeamId].getLocation(), false, false);
		
		if (killerCharacter == null) {
			return;
		}
		
		L2PcInstance killerPlayerInstance = null;
		
		if ((killerCharacter instanceof L2PetInstance) || (killerCharacter instanceof L2ServitorInstance)) {
			killerPlayerInstance = ((L2Summon) killerCharacter).getOwner();
			
			if (killerPlayerInstance == null) {
				return;
			}
		} else if (killerCharacter instanceof L2PcInstance) {
			killerPlayerInstance = (L2PcInstance) killerCharacter;
		} else {
			return;
		}
		
		byte killerTeamId = getParticipantTeamId(killerPlayerInstance.getObjectId());
		
		if ((killerTeamId != -1) && (killedTeamId != -1) && (killerTeamId != killedTeamId)) {
			TvTEventTeam killerTeam = _teams[killerTeamId];
			
			killerTeam.increasePoints();
			
			CreatureSay cs = new CreatureSay(killerPlayerInstance.getObjectId(), Say2.TELL, killerPlayerInstance.getName(), "I have killed " + killedPlayerInstance.getName() + "!");
			
			for (L2PcInstance playerInstance : _teams[killerTeamId].getParticipatedPlayers().values()) {
				if (playerInstance != null) {
					playerInstance.sendPacket(cs);
				}
			}
			
			// Notify to scripts.
			EventDispatcher.getInstance().notifyEventAsync(new OnTvTEventKill(killerPlayerInstance, killedPlayerInstance, killerTeam));
		}
	}
	
	/**
	 * Called on Appearing packet received (player finished teleporting).
	 * @param playerInstance
	 */
	public static void onTeleported(L2PcInstance playerInstance) {
		if (!isStarted() || (playerInstance == null) || !isPlayerParticipant(playerInstance.getObjectId())) {
			return;
		}
		
		if (playerInstance.isMageClass()) {
			for (SkillHolder skillHolder : tvt().getMageBuffs()) {
				Skill skill = skillHolder.getSkill();
				if (skill != null) {
					skill.applyEffects(playerInstance, playerInstance);
				}
			}
		} else {
			for (SkillHolder skillHolder : tvt().getFighterBuffs()) {
				Skill skill = skillHolder.getSkill();
				if (skill != null) {
					skill.applyEffects(playerInstance, playerInstance);
				}
			}
		}
	}
	
	/**
	 * @param source
	 * @param target
	 * @param skill
	 * @return true if player valid for skill
	 */
	public static final boolean checkForTvTSkill(L2PcInstance source, L2PcInstance target, Skill skill) {
		if (!isStarted()) {
			return true;
		}
		// TvT is started
		final int sourcePlayerId = source.getObjectId();
		final int targetPlayerId = target.getObjectId();
		final boolean isSourceParticipant = isPlayerParticipant(sourcePlayerId);
		final boolean isTargetParticipant = isPlayerParticipant(targetPlayerId);
		
		// both players not participating
		if (!isSourceParticipant && !isTargetParticipant) {
			return true;
		}
		// one player not participating
		if (!(isSourceParticipant && isTargetParticipant)) {
			return false;
		}
		// players in the different teams ?
		if (getParticipantTeamId(sourcePlayerId) != getParticipantTeamId(targetPlayerId)) {
			if (!skill.isBad()) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Sets the TvTEvent state.
	 * @param state as EventState
	 */
	private static void setState(EventState state) {
		synchronized (_state) {
			_state = state;
		}
	}
	
	/**
	 * Is TvTEvent inactive?
	 * @return true if event is inactive(waiting for next event cycle), otherwise false
	 */
	public static boolean isInactive() {
		boolean isInactive;
		
		synchronized (_state) {
			isInactive = _state == EventState.INACTIVE;
		}
		
		return isInactive;
	}
	
	/**
	 * Is TvTEvent in inactivating?.
	 * @return true if event is in inactivating progress, otherwise false
	 */
	public static boolean isInactivating() {
		boolean isInactivating;
		
		synchronized (_state) {
			isInactivating = _state == EventState.INACTIVATING;
		}
		
		return isInactivating;
	}
	
	/**
	 * Is TvTEvent in participation?.
	 * @return true if event is in participation progress, otherwise false
	 */
	public static boolean isParticipating() {
		boolean isParticipating;
		
		synchronized (_state) {
			isParticipating = _state == EventState.PARTICIPATING;
		}
		
		return isParticipating;
	}
	
	/**
	 * Is TvTEvent starting?
	 * @return true if event is starting up(setting up fighting spot, teleport players etc.), otherwise false
	 */
	public static boolean isStarting() {
		boolean isStarting;
		
		synchronized (_state) {
			isStarting = _state == EventState.STARTING;
		}
		
		return isStarting;
	}
	
	/**
	 * Is TvTEvent started?
	 * @return true if event is started, otherwise false
	 */
	public static boolean isStarted() {
		boolean isStarted;
		
		synchronized (_state) {
			isStarted = _state == EventState.STARTED;
		}
		
		return isStarted;
	}
	
	/**
	 * Is TvTEvent rewarding?
	 * @return true if event is currently rewarding, otherwise false
	 */
	public static boolean isRewarding() {
		boolean isRewarding;
		
		synchronized (_state) {
			isRewarding = _state == EventState.REWARDING;
		}
		
		return isRewarding;
	}
	
	/**
	 * Returns the team id of a player, if player is not participant it returns -1
	 * @param playerObjectId
	 * @return team name of the given playerName, if not in event -1
	 */
	public static byte getParticipantTeamId(int playerObjectId) {
		return (byte) (_teams[0].containsPlayer(playerObjectId) ? 0 : (_teams[1].containsPlayer(playerObjectId) ? 1 : -1));
	}
	
	/**
	 * Returns the team of a player, if player is not participant it returns null
	 * @param playerObjectId
	 * @return team of the given playerObjectId, if not in event null
	 */
	public static TvTEventTeam getParticipantTeam(int playerObjectId) {
		return (_teams[0].containsPlayer(playerObjectId) ? _teams[0] : (_teams[1].containsPlayer(playerObjectId) ? _teams[1] : null));
	}
	
	/**
	 * Returns the enemy team of a player, if player is not participant it returns null
	 * @param playerObjectId
	 * @return enemy team of the given playerObjectId, if not in event null
	 */
	public static TvTEventTeam getParticipantEnemyTeam(int playerObjectId) {
		return (_teams[0].containsPlayer(playerObjectId) ? _teams[1] : (_teams[1].containsPlayer(playerObjectId) ? _teams[0] : null));
	}
	
	/**
	 * Returns the team coordinates in which the player is in, if player is not in a team return null
	 * @param playerObjectId
	 * @return coordinates of teams, 2 elements, index 0 for team 1 and index 1 for team 2
	 */
	public static Location getParticipantTeamCoordinates(int playerObjectId) {
		return _teams[0].containsPlayer(playerObjectId) ? _teams[0].getLocation() : (_teams[1].containsPlayer(playerObjectId) ? _teams[1].getLocation() : null);
	}
	
	/**
	 * Is given player participant of the event?
	 * @param playerObjectId
	 * @return true if player is participant, ohterwise false
	 */
	public static boolean isPlayerParticipant(int playerObjectId) {
		if (!isParticipating() && !isStarting() && !isStarted()) {
			return false;
		}
		
		return _teams[0].containsPlayer(playerObjectId) || _teams[1].containsPlayer(playerObjectId);
	}
	
	/**
	 * Returns participated player count.
	 * @return amount of players registered in the event
	 */
	public static int getParticipatedPlayersCount() {
		if (!isParticipating() && !isStarting() && !isStarted()) {
			return 0;
		}
		
		return _teams[0].getParticipatedPlayerCount() + _teams[1].getParticipatedPlayerCount();
	}
	
	/**
	 * Returns teams names.
	 * @return names of teams, 2 elements, index 0 for team 1 and index 1 for team 2
	 */
	public static String[] getTeamNames() {
		return new String[] {
			_teams[0].getName(),
			_teams[1].getName()
		};
	}
	
	/**
	 * Returns player count of both teams.
	 * @return player count of teams, 2 elements, index 0 for team 1 and index 1 for team 2
	 */
	public static int[] getTeamsPlayerCounts() {
		return new int[] {
			_teams[0].getParticipatedPlayerCount(),
			_teams[1].getParticipatedPlayerCount()
		};
	}
	
	/**
	 * Returns points count of both teams.
	 * @return int[]: points of teams, 2 elements, index 0 for team 1 and index 1 for team 2
	 */
	public static int[] getTeamsPoints() {
		return new int[] {
			_teams[0].getPoints(),
			_teams[1].getPoints()
		};
	}
	
	public static int getTvTEventInstance() {
		return _TvTEventInstance;
	}
}
