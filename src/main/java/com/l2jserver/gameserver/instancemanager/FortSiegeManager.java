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

import static com.l2jserver.gameserver.config.Configuration.fortSiege;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.l2jserver.commons.database.ConnectionFactory;
import com.l2jserver.gameserver.model.CombatFlag;
import com.l2jserver.gameserver.model.FortSiegeSpawn;
import com.l2jserver.gameserver.model.L2Clan;
import com.l2jserver.gameserver.model.L2Object;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.entity.Fort;
import com.l2jserver.gameserver.model.entity.FortSiege;
import com.l2jserver.gameserver.model.items.instance.L2ItemInstance;
import com.l2jserver.gameserver.model.skills.CommonSkill;
import com.l2jserver.gameserver.network.SystemMessageId;
import com.l2jserver.gameserver.network.serverpackets.SystemMessage;

public final class FortSiegeManager {
	
	private static final Logger _log = Logger.getLogger(FortSiegeManager.class.getName());
	
	// Fort Siege settings
	private Map<Integer, List<FortSiegeSpawn>> _commanderSpawnList;
	private Map<Integer, List<CombatFlag>> _flagList;
	private final List<FortSiege> _sieges = new ArrayList<>();
	
	protected FortSiegeManager() {
		load();
	}
	
	public final void addSiegeSkills(L2PcInstance character) {
		character.addSkill(CommonSkill.SEAL_OF_RULER.getSkill(), false);
		character.addSkill(CommonSkill.BUILD_HEADQUARTERS.getSkill(), false);
	}
	
	/**
	 * @param clan The L2Clan of the player
	 * @param fortid
	 * @return true if the clan is registered or owner of a fort
	 */
	public final boolean checkIsRegistered(L2Clan clan, int fortid) {
		if (clan == null) {
			return false;
		}
		
		boolean register = false;
		try (var con = ConnectionFactory.getInstance().getConnection();
			var ps = con.prepareStatement("SELECT clan_id FROM fortsiege_clans where clan_id=? and fort_id=?")) {
			ps.setInt(1, clan.getId());
			ps.setInt(2, fortid);
			try (var rs = ps.executeQuery()) {
				while (rs.next()) {
					register = true;
					break;
				}
			}
		} catch (Exception e) {
			_log.log(Level.WARNING, "Exception: checkIsRegistered(): " + e.getMessage(), e);
		}
		return register;
	}
	
	public final void removeSiegeSkills(L2PcInstance character) {
		character.removeSkill(CommonSkill.SEAL_OF_RULER.getSkill());
		character.removeSkill(CommonSkill.BUILD_HEADQUARTERS.getSkill());
	}
	
	private final void load() {
		// Siege spawns settings
		_commanderSpawnList = new ConcurrentHashMap<>();
		_flagList = new ConcurrentHashMap<>();
		
		for (Fort fort : FortManager.getInstance().getForts()) {
			List<FortSiegeSpawn> commanderSpawns = new ArrayList<>();
			List<CombatFlag> flagSpawns = new ArrayList<>();
			for (int i = 1; i < 5; i++) {
				final String spawnParams = fortSiege().getProperty(fort.getName().replace(" ", "") + "Commander" + i);
				if (spawnParams == null) {
					break;
				}
				
				final StringTokenizer st = new StringTokenizer(spawnParams.trim(), ",");
				try {
					int x = Integer.parseInt(st.nextToken());
					int y = Integer.parseInt(st.nextToken());
					int z = Integer.parseInt(st.nextToken());
					int heading = Integer.parseInt(st.nextToken());
					int npc_id = Integer.parseInt(st.nextToken());
					commanderSpawns.add(new FortSiegeSpawn(fort.getResidenceId(), x, y, z, heading, npc_id, i));
				} catch (Exception e) {
					_log.warning("Error while loading commander(s) for " + fort.getName() + " fort.");
				}
			}
			
			_commanderSpawnList.put(fort.getResidenceId(), commanderSpawns);
			
			for (int i = 1; i < 4; i++) {
				final String spawnParams = fortSiege().getProperty(fort.getName().replace(" ", "") + "Flag" + i);
				if (spawnParams == null) {
					break;
				}
				final StringTokenizer st = new StringTokenizer(spawnParams.trim(), ",");
				
				try {
					int x = Integer.parseInt(st.nextToken());
					int y = Integer.parseInt(st.nextToken());
					int z = Integer.parseInt(st.nextToken());
					int flag_id = Integer.parseInt(st.nextToken());
					
					flagSpawns.add(new CombatFlag(fort.getResidenceId(), x, y, z, 0, flag_id));
				} catch (Exception e) {
					_log.warning("Error while loading flag(s) for " + fort.getName() + " fort.");
				}
			}
			_flagList.put(fort.getResidenceId(), flagSpawns);
		}
	}
	
	public final List<FortSiegeSpawn> getCommanderSpawnList(int _fortId) {
		return _commanderSpawnList.get(_fortId);
	}
	
	public final List<CombatFlag> getFlagList(int _fortId) {
		return _flagList.get(_fortId);
	}
	
	public final FortSiege getSiege(L2Object activeObject) {
		return getSiege(activeObject.getX(), activeObject.getY(), activeObject.getZ());
	}
	
	public final FortSiege getSiege(int x, int y, int z) {
		for (Fort fort : FortManager.getInstance().getForts()) {
			if (fort.getSiege().checkIfInZone(x, y, z)) {
				return fort.getSiege();
			}
		}
		return null;
	}
	
	public final List<FortSiege> getSieges() {
		return _sieges;
	}
	
	public final void addSiege(FortSiege fortSiege) {
		_sieges.add(fortSiege);
	}
	
	public boolean isCombat(int itemId) {
		return (itemId == 9819);
	}
	
	public boolean activateCombatFlag(L2PcInstance player, L2ItemInstance item) {
		if (!checkIfCanPickup(player)) {
			return false;
		}
		
		final Fort fort = FortManager.getInstance().getFort(player);
		
		final List<CombatFlag> fcf = _flagList.get(fort.getResidenceId());
		for (CombatFlag cf : fcf) {
			if (cf.getCombatFlagInstance() == item) {
				cf.activate(player, item);
			}
		}
		return true;
	}
	
	public boolean checkIfCanPickup(L2PcInstance player) {
		final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.THE_FORTRESS_BATTLE_OF_S1_HAS_FINISHED);
		sm.addItemName(9819);
		// Cannot own 2 combat flag
		if (player.isCombatFlagEquipped()) {
			player.sendPacket(sm);
			return false;
		}
		
		// here check if is siege is in progress
		// here check if is siege is attacker
		final Fort fort = FortManager.getInstance().getFort(player);
		
		if ((fort == null) || (fort.getResidenceId() <= 0)) {
			player.sendPacket(sm);
			return false;
		} else if (!fort.getSiege().isInProgress()) {
			player.sendPacket(sm);
			return false;
		} else if (fort.getSiege().getAttackerClan(player.getClan()) == null) {
			player.sendPacket(sm);
			return false;
		}
		return true;
	}
	
	public void dropCombatFlag(L2PcInstance player, int fortId) {
		final Fort fort = FortManager.getInstance().getFortById(fortId);
		final List<CombatFlag> fcf = _flagList.get(fort.getResidenceId());
		for (CombatFlag cf : fcf) {
			if (cf.getPlayerObjectId() == player.getObjectId()) {
				cf.dropIt();
				if (fort.getSiege().isInProgress()) {
					cf.spawnMe();
				}
			}
		}
	}
	
	public static final FortSiegeManager getInstance() {
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder {
		protected static final FortSiegeManager _instance = new FortSiegeManager();
	}
}
