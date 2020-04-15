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

import static com.l2jserver.gameserver.config.Configuration.siege;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.l2jserver.commons.database.ConnectionFactory;
import com.l2jserver.gameserver.datatables.SkillData;
import com.l2jserver.gameserver.model.L2Clan;
import com.l2jserver.gameserver.model.L2Object;
import com.l2jserver.gameserver.model.TowerSpawn;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.entity.Castle;
import com.l2jserver.gameserver.model.entity.Siege;
import com.l2jserver.gameserver.model.interfaces.ILocational;
import com.l2jserver.gameserver.model.skills.Skill;

public final class SiegeManager {
	
	private static final Logger LOG = LoggerFactory.getLogger(SiegeManager.class);
	
	private final Map<Integer, List<TowerSpawn>> _controlTowers = new HashMap<>();
	
	private final Map<Integer, List<TowerSpawn>> _flameTowers = new HashMap<>();
	
	private int _attackerMaxClans = siege().getAttackerMaxClans();
	
	private int _attackerRespawnDelay = siege().getAttackerRespawn();
	
	private int _defenderMaxClans = siege().getDefenderMaxClans();
	
	private int _flagMaxCount = siege().getMaxFlags();
	
	private int _siegeClanMinLevel = siege().getClanMinLevel();
	
	private int _siegeLength = siege().getSiegeLength();
	
	private int _bloodAllianceReward = siege().getBloodAllianceReward();
	
	protected SiegeManager() {
		load();
	}
	
	public final void addSiegeSkills(L2PcInstance character) {
		for (Skill sk : SkillData.getInstance().getSiegeSkills(character.isNoble(), character.getClan().getCastleId() > 0)) {
			character.addSkill(sk, false);
		}
	}
	
	/**
	 * @param clan The L2Clan of the player
	 * @param castleid
	 * @return true if the clan is registered or owner of a castle
	 */
	public final boolean checkIsRegistered(L2Clan clan, int castleid) {
		if (clan == null) {
			return false;
		}
		
		if (clan.getCastleId() > 0) {
			return true;
		}
		
		boolean register = false;
		try (var con = ConnectionFactory.getInstance().getConnection();
			var ps = con.prepareStatement("SELECT clan_id FROM siege_clans where clan_id=? and castle_id=?")) {
			ps.setInt(1, clan.getId());
			ps.setInt(2, castleid);
			try (var rs = ps.executeQuery()) {
				while (rs.next()) {
					register = true;
					break;
				}
			}
		} catch (Exception ex) {
			LOG.warn("There has been an error verifying if the clan is registered to the siege!", ex);
		}
		return register;
	}
	
	public final void removeSiegeSkills(L2PcInstance character) {
		for (Skill sk : SkillData.getInstance().getSiegeSkills(character.isNoble(), character.getClan().getCastleId() > 0)) {
			character.removeSkill(sk);
		}
	}
	
	private final void load() {
		// Gludio
		_controlTowers.put(1, List.of(siege().getGludioControlTower1(), //
			siege().getGludioControlTower2(), //
			siege().getGludioControlTower3()));
		MercTicketManager.MERCS_MAX_PER_CASTLE[0] = siege().getGludioMaxMercenaries();
		_flameTowers.put(1, List.of(siege().getGludioFlameTower1(), //
			siege().getGludioFlameTower1()));
		// Dion
		_controlTowers.put(2, List.of(siege().getDionControlTower1(), //
			siege().getDionControlTower2(), //
			siege().getDionControlTower3()));
		_flameTowers.put(2, List.of(siege().getDionFlameTower1(), //
			siege().getDionFlameTower1()));
		MercTicketManager.MERCS_MAX_PER_CASTLE[1] = siege().getDionMaxMercenaries();
		// Giran
		_controlTowers.put(3, List.of(siege().getGiranControlTower1(), //
			siege().getGiranControlTower2(), //
			siege().getGiranControlTower3()));
		_flameTowers.put(3, List.of(siege().getGiranFlameTower1(), //
			siege().getGiranFlameTower1()));
		MercTicketManager.MERCS_MAX_PER_CASTLE[2] = siege().getGiranMaxMercenaries();
		// Oren
		_controlTowers.put(4, List.of(siege().getOrenControlTower1(), //
			siege().getOrenControlTower2(), //
			siege().getOrenControlTower3()));
		_flameTowers.put(4, List.of(siege().getOrenFlameTower1(), //
			siege().getOrenFlameTower1()));
		MercTicketManager.MERCS_MAX_PER_CASTLE[3] = siege().getOrenMaxMercenaries();
		// Aden
		_controlTowers.put(5, List.of(siege().getAdenControlTower1(), //
			siege().getAdenControlTower2(), //
			siege().getAdenControlTower3()));
		_flameTowers.put(5, List.of(siege().getAdenFlameTower1(), //
			siege().getAdenFlameTower1()));
		MercTicketManager.MERCS_MAX_PER_CASTLE[4] = siege().getAdenMaxMercenaries();
		// Innadril
		_controlTowers.put(6, List.of(siege().getInnadrilControlTower1(), //
			siege().getInnadrilControlTower2(), //
			siege().getInnadrilControlTower3()));
		_flameTowers.put(6, List.of(siege().getInnadrilFlameTower1(), //
			siege().getInnadrilFlameTower1()));
		MercTicketManager.MERCS_MAX_PER_CASTLE[5] = siege().getInnadrilMaxMercenaries();
		// Goddard
		_controlTowers.put(7, List.of(siege().getGoddardControlTower1(), //
			siege().getGoddardControlTower2(), //
			siege().getGoddardControlTower3()));
		_flameTowers.put(7, List.of(siege().getGoddardFlameTower1(), //
			siege().getGoddardFlameTower1()));
		MercTicketManager.MERCS_MAX_PER_CASTLE[6] = siege().getGoddardMaxMercenaries();
		// Rune
		_controlTowers.put(8, List.of(siege().getRuneControlTower1(), //
			siege().getRuneControlTower2(), //
			siege().getRuneControlTower3()));
		_flameTowers.put(8, List.of(siege().getRuneFlameTower1(), //
			siege().getRuneFlameTower1()));
		MercTicketManager.MERCS_MAX_PER_CASTLE[7] = siege().getRuneMaxMercenaries();
		// Schuttgart
		_controlTowers.put(9, List.of(siege().getSchuttgartControlTower1(), //
			siege().getSchuttgartControlTower2(), //
			siege().getSchuttgartControlTower3()));
		_flameTowers.put(9, List.of(siege().getSchuttgartFlameTower1(), //
			siege().getSchuttgartFlameTower1()));
		MercTicketManager.MERCS_MAX_PER_CASTLE[8] = siege().getSchuttgartMaxMercenaries();
		
		for (Castle castle : CastleManager.getInstance().getCastles()) {
			if (castle.getOwnerId() != 0) {
				loadTrapUpgrade(castle.getResidenceId());
			}
		}
	}
	
	public final List<TowerSpawn> getControlTowers(int castleId) {
		return _controlTowers.get(castleId);
	}
	
	public final List<TowerSpawn> getFlameTowers(int castleId) {
		return _flameTowers.get(castleId);
	}
	
	public final int getAttackerMaxClans() {
		return _attackerMaxClans;
	}
	
	public final int getAttackerRespawnDelay() {
		return _attackerRespawnDelay;
	}
	
	public final int getDefenderMaxClans() {
		return _defenderMaxClans;
	}
	
	public final int getFlagMaxCount() {
		return _flagMaxCount;
	}
	
	public final Siege getSiege(ILocational loc) {
		return getSiege(loc.getX(), loc.getY(), loc.getZ());
	}
	
	public final Siege getSiege(L2Object activeObject) {
		return getSiege(activeObject.getX(), activeObject.getY(), activeObject.getZ());
	}
	
	public final Siege getSiege(int x, int y, int z) {
		for (Castle castle : CastleManager.getInstance().getCastles()) {
			if (castle.getSiege().checkIfInZone(x, y, z)) {
				return castle.getSiege();
			}
		}
		return null;
	}
	
	public final int getSiegeClanMinLevel() {
		return _siegeClanMinLevel;
	}
	
	public final int getSiegeLength() {
		return _siegeLength;
	}
	
	public final int getBloodAllianceReward() {
		return _bloodAllianceReward;
	}
	
	public final List<Siege> getSieges() {
		List<Siege> sieges = new ArrayList<>();
		for (Castle castle : CastleManager.getInstance().getCastles()) {
			sieges.add(castle.getSiege());
		}
		return sieges;
	}
	
	private final void loadTrapUpgrade(int castleId) {
		try (var con = ConnectionFactory.getInstance().getConnection();
			var ps = con.prepareStatement("SELECT * FROM castle_trapupgrade WHERE castleId=?")) {
			ps.setInt(1, castleId);
			try (var rs = ps.executeQuery()) {
				while (rs.next()) {
					_flameTowers.get(castleId).get(rs.getInt("towerIndex")).setUpgradeLevel(rs.getInt("level"));
				}
			}
		} catch (Exception ex) {
			LOG.warn("There has been an error loading trap upgrade!", ex);
		}
	}
	
	public static final SiegeManager getInstance() {
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder {
		protected static final SiegeManager _instance = new SiegeManager();
	}
}